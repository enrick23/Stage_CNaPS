package mg.cnaps.modele.dictionnaire.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import mg.cnaps.modele.dictionnaire.DictionnaireDTO.FormulaireColonne;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.FormulaireObjet;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.AnalyseResultat;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.ColonneAnalyse;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.FichierSource;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.ObjetAnalyse;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.ObjetPropose;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.StatutIa;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.TriggerAnalyse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static mg.cnaps.modele.dictionnaire.ia.IaModeles.normaliser;
import static mg.cnaps.modele.dictionnaire.ia.IaModeles.vide;

/**
 * Orchestrateur du module IA — enchaîne les étapes du plan de réalisation :
 * <ol>
 *   <li>Analyse statique du code ({@link CodeAnalyzer}) ;</li>
 *   <li>Croisement avec le catalogue Oracle ({@link CatalogueOracle}) ;</li>
 *   <li>IA — Mission 1 « compréhension du code » : le LLM relit le code et complète les objets manqués ;</li>
 *   <li>IA — Mission 2 « enrichissement sémantique » : descriptions, FLAG_REF, sensibilité 1 à 4, triggers ;</li>
 *   <li>Moteur expert ({@link MoteurExpert}) : complète tout champ resté vide (fonctionne sans LLM) ;</li>
 *   <li>Production des propositions ({@link ObjetPropose}) soumises à la validation humaine.</li>
 * </ol>
 */
@Service
public class DictionnaireAiService {

    private static final Logger LOG = LoggerFactory.getLogger(DictionnaireAiService.class);
    private static final int OBJETS_PAR_LOT = 6;

    private static final String CONSIGNE_DECOUVERTE =
            "Tu es un expert Oracle, Java/Spring (JPA, Hibernate, JdbcTemplate) et PL/SQL chargé du reverse engineering "
          + "du dictionnaire de données de la CNaPS (Caisse Nationale de Prévoyance Sociale de Madagascar).\n"
          + "On te fournit le code source d'une application et la liste des objets Oracle déjà détectés par un analyseur "
          + "déterministe. Ta mission : identifier les tables et vues Oracle réellement utilisées par ce code que l'analyseur "
          + "a MANQUÉES (requêtes SQL dynamiques ou concaténées, requêtes natives, héritage, DAO, procédures appelées...), "
          + "ainsi que les colonnes manquantes d'objets déjà détectés.\n"
          + "Règles : ne jamais inventer un objet non justifié par le code ; noms Oracle en MAJUSCULES ; types Oracle "
          + "(VARCHAR2, NUMBER, DATE, TIMESTAMP, CLOB, BLOB, CHAR) ; contrainte parmi « PK », « FK », « UK », « Not null » "
          + "(séparées par des virgules) ; reference au format SCHEMA.TABLE(COLONNE) pour une FK.\n"
          + "Réponds UNIQUEMENT par un objet JSON de la forme :\n"
          + "{\"objets\":[{\"schema\":\"DRH\",\"nom\":\"AGENT\",\"type\":\"TABLE\",\"justification\":\"cité dans ...\","
          + "\"colonnes\":[{\"nom\":\"MATRICULE\",\"typeBase\":\"VARCHAR2\",\"taille\":\"20\",\"contrainte\":\"PK\",\"reference\":null}],"
          + "\"sequences\":[]}],\"avertissements\":[\"...\"]}\n"
          + "Si rien ne manque, renvoie {\"objets\":[],\"avertissements\":[]}.";

    private static final String CONSIGNE_ENRICHISSEMENT =
            "Tu es l'assistant de gouvernance des données de la DSI de la CNaPS (Caisse Nationale de Prévoyance Sociale, "
          + "Madagascar). Tu rédiges le dictionnaire de données Oracle à partir de la structure technique fournie "
          + "(noms, types, contraintes, triggers, script des vues). Rédige en FRANÇAIS, de façon concise et métier.\n"
          + "Pour chaque objet, produis :\n"
          + "1. description : finalité fonctionnelle de la table ou de la vue (1 à 2 phrases, 300 caractères max) ;\n"
          + "2. flagRef : true si table référentielle (nomenclature / paramétrage : types, statuts, codes, listes de "
          + "valeurs, découpage géographique), false si table transactionnelle (dossiers, mouvements, historiques) ;\n"
          + "3. pour chaque colonne : description métier (200 caractères max), niveauSensibilite et observation "
          + "éventuelle (format, unité, référence) ;\n"
          + "4. pour chaque trigger : description en langage clair de la règle de gestion appliquée (300 caractères max).\n"
          + "Matrice de sensibilité CNaPS :\n"
          + "- 1 Public : codes et libellés de nomenclature, paramètres généraux non nominatifs ;\n"
          + "- 2 Interne : dates de traitement, statuts, flags, identifiants techniques, traçabilité ;\n"
          + "- 3 Confidentiel : données personnelles (matricule, nom, prénom, CIN, adresse, téléphone, e-mail, date de "
          + "naissance, numéro d'affilié / assuré, compte) ;\n"
          + "- 4 Secret : données financières ou critiques (montants, salaires, pensions, soldes, RIB, mots de passe, jetons).\n"
          + "Conserve exactement les noms d'objets, de colonnes et de triggers fournis. Ne renvoie que des objets fournis.\n"
          + "Réponds UNIQUEMENT par un objet JSON de la forme :\n"
          + "{\"applicationDescription\":\"...\",\"objets\":[{\"schema\":\"DRH\",\"nom\":\"DEMANDE_PRET\",\"description\":\"...\","
          + "\"flagRef\":false,\"colonnes\":[{\"nom\":\"MATRICULE_AGENT\",\"description\":\"...\",\"niveauSensibilite\":3,"
          + "\"observation\":null}],\"triggers\":[{\"nom\":\"TRG_X\",\"description\":\"...\"}]}]}";

    private final CodeAnalyzer analyseur;
    private final CatalogueOracle catalogue;
    private final MoteurLlm llm;
    private final MoteurExpert expert;
    private final ObjectMapper json = new ObjectMapper();
    private final boolean envoyerCode;
    private final int maxCaracteresCode;

    public DictionnaireAiService(CodeAnalyzer analyseur, CatalogueOracle catalogue, MoteurLlm llm, MoteurExpert expert,
                                 @Value("${dico.ai.send-source-code:true}") boolean envoyerCode,
                                 @Value("${dico.ai.max-code-chars:60000}") int maxCaracteresCode) {
        this.analyseur = analyseur;
        this.catalogue = catalogue;
        this.llm = llm;
        this.expert = expert;
        this.envoyerCode = envoyerCode;
        this.maxCaracteresCode = maxCaracteresCode;
    }

    public StatutIa statut() {
        StatutIa s = new StatutIa();
        s.fournisseur = llm.actif() ? llm.fournisseur() : "expert";
        s.modele = llm.modele();
        s.llmActif = llm.actif();
        s.envoiCodeAutorise = envoyerCode;
        return s;
    }

    // ================================================================== Analyse de code source

    public AnalyseResultat analyserCode(List<FichierSource> fichiers, String appCode, String appLibelle,
                                        String appDescription, String schemaDefaut) {
        long debut = System.currentTimeMillis();
        AnalyseResultat res = new AnalyseResultat();
        res.appCode = vide(appCode) ? null : appCode.trim();
        res.appLibelle = vide(appLibelle) ? null : appLibelle.trim();

        // Étape 2 — analyse statique
        CodeAnalyzer.Resultat brut = analyseur.analyser(fichiers, schemaDefaut);
        res.fichiersAnalyses = brut.fichiersAnalyses;
        res.avertissements.addAll(brut.avertissements);
        Map<String, ObjetAnalyse> objets = brut.objets;

        // Étape 3 — croisement Oracle
        boolean oracleOk = croiserTous(objets.values(), res);

        // Étape 4 — Mission 1 : compréhension du code par le LLM (objets manqués)
        boolean llmUtilise = false;
        if (llm.actif() && envoyerCode) {
            try {
                List<ObjetAnalyse> nouveaux = missionDecouverte(fichiers, objets, normaliser(schemaDefaut), res);
                llmUtilise = true;
                if (oracleOk) croiserTous(nouveaux, res);
            } catch (IOException | RuntimeException e) {
                LOG.warn("Mission découverte LLM en échec : {}", e.getMessage());
                res.avertissements.add("IA (compréhension du code) indisponible : " + resume(e) + ". Analyse déterministe conservée.");
            }
        }

        // Étape 4 — Mission 2 : enrichissement sémantique + secours expert
        String descApp = enrichir(objets.values(), res.appLibelle, appDescription, res);
        llmUtilise |= descApp != null;
        expert.enrichir(objets.values());
        res.appDescription = !vide(appDescription) ? appDescription.trim()
                : descApp != null ? descApp : expert.descriptionApplication(res.appLibelle, objets.values());
        res.moteur = llmUtilise ? llm.libelle() + " + moteur expert CNaPS" : "Moteur expert CNaPS (local)";

        if (objets.isEmpty()) {
            res.avertissements.add("Aucun objet Oracle détecté dans le code fourni.");
        }
        for (ObjetAnalyse o : objets.values()) {
            o.dejaDocumente = catalogue.dejaDocumente(o.schema, o.nom);
            res.objets.add(proposer(o, res));
        }
        res.dureeMs = System.currentTimeMillis() - debut;
        return res;
    }

    // ================================================================== Enrichissement d'une table Oracle existante

    public AnalyseResultat enrichirTableOracle(String schema, String nom, String appCode, String appLibelle,
                                               String appDescription) {
        long debut = System.currentTimeMillis();
        AnalyseResultat res = new AnalyseResultat();
        res.appCode = vide(appCode) ? null : appCode.trim();
        res.appLibelle = vide(appLibelle) ? null : appLibelle.trim();
        res.fichiersAnalyses = 0;

        ObjetAnalyse o = new ObjetAnalyse(schema, nom);
        catalogue.croiser(o);
        if (!o.existeEnBase) {
            res.avertissements.add("L'objet " + o.cle() + " n'existe pas dans Oracle.");
        }
        List<ObjetAnalyse> liste = new ArrayList<>();
        liste.add(o);
        String descApp = enrichir(liste, res.appLibelle, appDescription, res);
        expert.enrichir(o);
        res.appDescription = !vide(appDescription) ? appDescription.trim() : descApp;
        res.moteur = descApp != null || llm.actif() ? llm.libelle() + " + moteur expert CNaPS" : "Moteur expert CNaPS (local)";
        o.dejaDocumente = catalogue.dejaDocumente(o.schema, o.nom);
        res.objets.add(proposer(o, res));
        res.dureeMs = System.currentTimeMillis() - debut;
        return res;
    }

    // ================================================================== Étapes internes

    private boolean croiserTous(Collection<ObjetAnalyse> objets, AnalyseResultat res) {
        for (ObjetAnalyse o : objets) {
            if (!catalogue.croiser(o)) {
                res.avertissements.add("Catalogue Oracle inaccessible : les structures n'ont pas pu être vérifiées en base.");
                return false;
            }
        }
        return true;
    }

    /** Mission 1 : le LLM lit le code et signale les objets / colonnes manqués par l'analyseur déterministe. */
    private List<ObjetAnalyse> missionDecouverte(List<FichierSource> fichiers, Map<String, ObjetAnalyse> objets,
                                                 String schemaDefaut, AnalyseResultat res) throws IOException {
        StringBuilder code = new StringBuilder();
        boolean tronque = false;
        for (FichierSource f : fichiers) {
            if (f == null || vide(f.contenu)) continue;
            String bloc = "\n===== Fichier : " + (vide(f.nom) ? "extrait" : f.nom) + " =====\n" + f.contenu + "\n";
            if (code.length() + bloc.length() > maxCaracteresCode) {
                code.append(bloc, 0, Math.max(0, maxCaracteresCode - code.length()));
                tronque = true;
                break;
            }
            code.append(bloc);
        }
        if (tronque) {
            res.avertissements.add("Code source volumineux : seuls les " + maxCaracteresCode
                    + " premiers caractères ont été transmis à l'IA pour la compréhension du code.");
        }

        ObjectNode entree = json.createObjectNode();
        entree.put("schemaParDefaut", schemaDefaut);
        ArrayNode deja = entree.putArray("objetsDejaDetectes");
        for (ObjetAnalyse o : objets.values()) {
            ObjectNode n = deja.addObject();
            n.put("schema", o.schema);
            n.put("nom", o.nom);
            n.put("type", o.type);
            n.put("colonnes", String.join(", ", o.colonnes.keySet()));
        }
        String message = "Objets déjà détectés :\n" + json.writerWithDefaultPrettyPrinter().writeValueAsString(entree)
                + "\n\nCode source de l'application :\n" + code;

        JsonNode rep = llm.interrogerJson(CONSIGNE_DECOUVERTE, message);
        for (JsonNode a : rep.path("avertissements")) {
            if (!vide(a.asText())) res.avertissements.add("IA : " + a.asText());
        }
        List<ObjetAnalyse> nouveaux = new ArrayList<>();
        for (JsonNode n : rep.path("objets")) {
            String nom = normaliser(n.path("nom").asText(null));
            if (nom == null || !nom.matches("[A-Z][A-Z0-9_$#]{0,127}")) continue;
            String schema = normaliser(n.path("schema").asText(null));
            if (schema == null) schema = schemaDefaut;
            String cle = (schema == null ? "?" : schema) + "." + nom;
            ObjetAnalyse o = objets.get(cle);
            boolean nouveau = o == null;
            if (nouveau) {
                o = new ObjetAnalyse(schema, nom);
                o.type = "VIEW".equalsIgnoreCase(n.path("type").asText()) ? "VIEW" : "TABLE";
                objets.put(cle, o);
                nouveaux.add(o);
            }
            String justif = n.path("justification").asText("");
            o.sources.add("Détecté par l'IA" + (justif.isEmpty() ? "" : " : " + CodeAnalyzer.tronquer(justif, 150)));
            if (!o.existeEnBase) { // les colonnes proposées par l'IA ne remplacent jamais la structure Oracle
                for (JsonNode c : n.path("colonnes")) {
                    String nomCol = normaliser(c.path("nom").asText(null));
                    if (nomCol == null || !nomCol.matches("[A-Z][A-Z0-9_$#]{0,127}")) continue;
                    boolean existait = o.colonnes.containsKey(nomCol);
                    ColonneAnalyse col = o.colonne(nomCol);
                    if (existait) continue;
                    col.typeBase = normaliser(c.path("typeBase").asText(null));
                    col.taille = texte(c, "taille");
                    String ct = c.path("contrainte").asText("").toUpperCase(Locale.ROOT);
                    col.pk = ct.contains("PK");
                    col.fk = ct.contains("FK");
                    col.uk = ct.contains("UK");
                    if (ct.contains("NOT NULL") || col.pk) col.nullable = false;
                    col.reference = texte(c, "reference");
                }
            }
            for (JsonNode s : n.path("sequences")) {
                String seq = normaliser(s.asText(null));
                if (seq != null) o.sequences.add(seq);
            }
        }
        return nouveaux;
    }

    /** Mission 2 : enrichissement sémantique par lots. Retourne la description d'application proposée par l'IA, ou null. */
    private String enrichir(Collection<ObjetAnalyse> objets, String appLibelle, String appDescription, AnalyseResultat res) {
        if (!llm.actif() || objets.isEmpty()) return null;
        String descApp = null;
        List<ObjetAnalyse> liste = new ArrayList<>(objets);
        for (int i = 0; i < liste.size(); i += OBJETS_PAR_LOT) {
            List<ObjetAnalyse> lot = liste.subList(i, Math.min(liste.size(), i + OBJETS_PAR_LOT));
            try {
                JsonNode rep = llm.interrogerJson(CONSIGNE_ENRICHISSEMENT, messageEnrichissement(lot, appLibelle, appDescription, liste));
                if (descApp == null && !vide(rep.path("applicationDescription").asText(null))) {
                    descApp = rep.path("applicationDescription").asText().trim();
                }
                appliquerEnrichissement(lot, rep);
            } catch (IOException | RuntimeException e) {
                LOG.warn("Enrichissement LLM en échec : {}", e.getMessage());
                res.avertissements.add("IA (enrichissement sémantique) indisponible pour "
                        + lot.stream().map(o -> o.nom).collect(Collectors.joining(", "))
                        + " : " + resume(e) + ". Le moteur expert CNaPS a pris le relais.");
            }
        }
        return descApp;
    }

    private String messageEnrichissement(List<ObjetAnalyse> lot, String appLibelle, String appDescription,
                                         List<ObjetAnalyse> tous) throws IOException {
        ObjectNode entree = json.createObjectNode();
        ObjectNode app = entree.putObject("application");
        app.put("libelle", appLibelle);
        app.put("description", appDescription);
        app.put("objetsDeLApplication", tous.stream().map(ObjetAnalyse::cle).collect(Collectors.joining(", ")));
        ArrayNode arr = entree.putArray("objets");
        for (ObjetAnalyse o : lot) {
            ObjectNode n = arr.addObject();
            n.put("schema", o.schema);
            n.put("nom", o.nom);
            n.put("type", o.type);
            n.put("descriptionActuelle", o.description);
            n.put("existeEnBase", o.existeEnBase);
            if (o.viewScript != null) n.put("scriptVue", CodeAnalyzer.tronquer(o.viewScript, 1500));
            ArrayNode cols = n.putArray("colonnes");
            for (ColonneAnalyse c : o.colonnes.values()) {
                ObjectNode cn = cols.addObject();
                cn.put("nom", c.nom);
                cn.put("type", c.typeBase == null ? null : c.typeBase + (vide(c.taille) ? "" : "(" + c.taille + ")"));
                cn.put("contrainte", c.contrainte());
                if (c.reference != null) cn.put("reference", c.reference);
                if (!vide(c.description)) cn.put("descriptionActuelle", c.description);
            }
            ArrayNode trgs = n.putArray("triggers");
            for (TriggerAnalyse t : o.triggers.values()) {
                ObjectNode tn = trgs.addObject();
                tn.put("nom", t.nom);
                tn.put("evenement", t.evenement);
                if (t.corps != null) tn.put("corps", CodeAnalyzer.tronquer(t.corps, 1500));
            }
        }
        return json.writerWithDefaultPrettyPrinter().writeValueAsString(entree);
    }

    private void appliquerEnrichissement(List<ObjetAnalyse> lot, JsonNode rep) {
        Map<String, ObjetAnalyse> parNom = new LinkedHashMap<>();
        for (ObjetAnalyse o : lot) {
            parNom.put(o.cle(), o);
            parNom.putIfAbsent(o.nom, o);
        }
        for (JsonNode n : rep.path("objets")) {
            String nom = normaliser(n.path("nom").asText(null));
            String schema = normaliser(n.path("schema").asText(null));
            ObjetAnalyse o = parNom.get((schema == null ? "?" : schema) + "." + nom);
            if (o == null) o = parNom.get(nom);
            if (o == null) continue;
            if (!vide(n.path("description").asText(null))) o.description = n.path("description").asText().trim();
            if (n.path("flagRef").isBoolean()) o.flagRef = n.path("flagRef").asBoolean();
            for (JsonNode c : n.path("colonnes")) {
                ColonneAnalyse col = o.colonnes.get(normaliser(c.path("nom").asText("")));
                if (col == null) continue;
                if (!vide(c.path("description").asText(null))) col.description = c.path("description").asText().trim();
                if (c.path("niveauSensibilite").canConvertToInt()) {
                    int niv = c.path("niveauSensibilite").asInt();
                    if (niv >= 1 && niv <= 4) col.niveauSensibilite = niv;
                }
                String obs = texte(c, "observation");
                if (obs != null && vide(col.observation)) col.observation = obs;
            }
            for (JsonNode t : n.path("triggers")) {
                TriggerAnalyse trg = o.triggers.get(normaliser(t.path("nom").asText("")));
                if (trg != null && !vide(t.path("description").asText(null))) trg.description = t.path("description").asText().trim();
            }
        }
    }

    // ================================================================== Conversion vers le formulaire du dictionnaire

    private ObjetPropose proposer(ObjetAnalyse o, AnalyseResultat res) {
        boolean vue = "VIEW".equals(o.type);
        FormulaireObjet f = new FormulaireObjet();
        f.appCode = res.appCode;
        f.appLibelle = res.appLibelle;
        f.appDescription = octets(res.appDescription, 500);
        f.schema = o.schema;
        f.nomObjet = o.nom;
        f.typeObjet = o.type;
        f.flagRef = Boolean.TRUE.equals(o.flagRef);
        f.description = octets(o.description, vue ? 250 : 500); // DICO_DATA_VIEW.DESCRIPTION = 250 octets
        f.sequences = octets(String.join(", ", o.sequences), 128);
        f.triggers = o.triggers.values().stream()
                .map(t -> t.nom + " : " + octets(vide(t.description) ? t.evenement : t.description, 480))
                .collect(Collectors.joining("\n"));
        f.viewScript = vue ? octets(o.viewScript, 4000) : null;

        int documentees = 0;
        for (ColonneAnalyse c : o.colonnes.values()) {
            FormulaireColonne fc = new FormulaireColonne();
            fc.nom = c.nom;
            fc.typeBase = octets(vide(c.typeBase) ? "VARCHAR2" : c.typeBase, 40);
            fc.taille = octets(c.taille, 20);
            fc.contrainte = octets(c.contrainte(), 50);
            fc.niveauSensibilite = c.niveauSensibilite;
            fc.defaut = octets(c.defaut, 100);
            fc.description = octets(c.description, 500);
            fc.index = String.join(", ", c.index);
            fc.observation = octets(c.observation, 500);
            f.colonnes.add(fc);
            if (!vide(c.description) && c.niveauSensibilite != null) documentees++;
        }

        ObjetPropose p = new ObjetPropose();
        p.formulaire = f;
        p.existeEnBase = o.existeEnBase;
        p.dejaDocumente = o.dejaDocumente;
        p.sources.addAll(o.sources);
        p.alertes.addAll(o.alertes);
        if (o.schema == null) p.alertes.add("Schéma Oracle à renseigner avant l'enregistrement.");
        if (o.colonnes.isEmpty()) p.alertes.add("Aucune colonne identifiée : à compléter manuellement.");
        p.nombreColonnes = o.colonnes.size();
        p.completude = o.colonnes.isEmpty() ? (vide(o.description) ? 0 : 50)
                : (int) Math.round(100.0 * documentees / o.colonnes.size());
        return p;
    }

    /** Tronque une chaîne pour respecter une limite en OCTETS UTF-8 (colonnes VARCHAR2(n BYTE)). */
    static String octets(String s, int maxOctets) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        if (t.getBytes(StandardCharsets.UTF_8).length <= maxOctets) return t;
        StringBuilder sb = new StringBuilder();
        int taille = 0;
        int limite = maxOctets - 3; // place pour « … »
        for (int i = 0; i < t.length(); ) {
            int cp = t.codePointAt(i);
            int n = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (taille + n > limite) break;
            sb.appendCodePoint(cp);
            taille += n;
            i += Character.charCount(cp);
        }
        return sb.append('…').toString();
    }

    private static String texte(JsonNode n, String champ) {
        JsonNode v = n.path(champ);
        if (v.isMissingNode() || v.isNull()) return null;
        String t = v.asText("").trim();
        return t.isEmpty() || "null".equalsIgnoreCase(t) ? null : t;
    }

    private static String resume(Throwable e) {
        String m = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return m.length() > 160 ? m.substring(0, 160) + "…" : m;
    }
}
