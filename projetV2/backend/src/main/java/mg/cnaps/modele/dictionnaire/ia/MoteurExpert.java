package mg.cnaps.modele.dictionnaire.ia;

import mg.cnaps.modele.dictionnaire.ia.IaModeles.ColonneAnalyse;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.ObjetAnalyse;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.TriggerAnalyse;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mg.cnaps.modele.dictionnaire.ia.IaModeles.vide;

/**
 * Étape 4 (secours) — Moteur expert « CNaPS / Oracle ».
 *
 * <p>Moteur sémantique à base de règles et de vocabulaire métier, utilisé :</p>
 * <ul>
 *   <li>quand aucun LLM n'est configuré ou joignable (démonstration hors-ligne, réseau isolé) ;</li>
 *   <li>pour compléter les champs que le LLM aurait laissés vides.</li>
 * </ul>
 * Il ne remplace jamais une valeur déjà renseignée (code, commentaire Oracle ou LLM).
 */
@Component
public class MoteurExpert {

    // ------------------------------------------------------------------ Vocabulaire (abréviations → français)

    private static final Map<String, String> VOCABULAIRE = new HashMap<>();

    static {
        String[][] mots = {
                {"ID", "identifiant"}, {"IDENT", "identifiant"}, {"CD", "code"}, {"CODE", "code"}, {"COD", "code"},
                {"LIB", "libellé"}, {"LIBELLE", "libellé"}, {"LBL", "libellé"}, {"DESC", "description"},
                {"DESCRIPTION", "description"}, {"DT", "date"}, {"DATE", "date"}, {"DAT", "date"},
                {"MT", "montant"}, {"MNT", "montant"}, {"MONTANT", "montant"}, {"NUM", "numéro"}, {"NO", "numéro"},
                {"NUMERO", "numéro"}, {"NB", "nombre"}, {"NBR", "nombre"}, {"NOMBRE", "nombre"}, {"QTE", "quantité"},
                {"TX", "taux"}, {"TAUX", "taux"}, {"AGT", "agent"}, {"AGENT", "agent"}, {"EMP", "employé"},
                {"PRET", "prêt"}, {"PRETS", "prêts"}, {"AVANCE", "avance"}, {"DEM", "demande"}, {"DEMANDE", "demande"},
                {"DMD", "demande"}, {"PRIV", "privilège"}, {"PRIVILEGE", "privilège"}, {"CONCERNE", "concerné"},
                {"ORIGINE", "d'origine"}, {"LIGNE", "ligne"}, {"CORRIGE", "corrigé"}, {"CORRECTION", "correction"},
                {"SUPPRESSION", "suppression"}, {"SUPPR", "suppression"}, {"BESOIN", "besoin"}, {"SECTION", "section"},
                {"ACTION", "action"}, {"DECLARANT", "déclarant"}, {"DECLARATION", "déclaration"},
                {"CLOTURE", "clôture"}, {"PRIORITE", "priorité"}, {"INCIDENT", "incident"}, {"TICKET", "ticket"},
                {"DELAI", "délai"}, {"RESOLUTION", "résolution"}, {"HEURES", "heures"}, {"AUTEUR", "auteur"},
                {"MESSAGE", "message"}, {"TEXTE", "texte"}, {"TITRE", "titre"}, {"DETAIL", "détaillé"},
                {"ABREVIATION", "abréviation"}, {"ABREV", "abréviation"}, {"LIEU", "lieu"}, {"ZONE", "zone"},
                {"AGENCE", "agence"}, {"ANTENNE", "antenne"}, {"VILLE", "ville"}, {"PHYSIQUE", "physique"},
                {"MOBILE", "mobile"}, {"RETENUE", "retenue"}, {"MAX", "maximal"}, {"MIN", "minimal"},
                {"TYPE", "type"}, {"TYP", "type"}, {"REF", "référence"}, {"STATUT", "statut"}, {"STAT", "statut"},
                {"ETAT", "état"}, {"ETA", "état"}, {"DIR", "direction"}, {"DIRECTION", "direction"}, {"SCE", "service"},
                {"SERVICE", "service"}, {"DR", "direction régionale"}, {"HIST", "historique"}, {"HISTO", "historique"},
                {"LOG", "journal"}, {"TRACE", "trace"}, {"PARAM", "paramètre"}, {"CONF", "configuration"},
                {"USER", "utilisateur"}, {"USR", "utilisateur"}, {"UTIL", "utilisateur"}, {"UTILISATEUR", "utilisateur"},
                {"CREATION", "création"}, {"CREA", "création"}, {"MODIF", "modification"}, {"MAJ", "mise à jour"},
                {"MODIFICATION", "modification"}, {"VALID", "validation"}, {"VALIDATION", "validation"},
                {"DEBUT", "début"}, {"DEB", "début"}, {"FIN", "fin"}, {"NAISS", "naissance"}, {"NAISSANCE", "naissance"},
                {"ADR", "adresse"}, {"ADRESSE", "adresse"}, {"TEL", "téléphone"}, {"TELEPHONE", "téléphone"},
                {"MAIL", "e-mail"}, {"EMAIL", "e-mail"}, {"MATRICULE", "matricule"}, {"MAT", "matricule"},
                {"NOM", "nom"}, {"PRENOM", "prénom"}, {"PRENOMS", "prénoms"}, {"SEXE", "sexe"}, {"CIN", "CIN"},
                {"SAL", "salaire"}, {"SALAIRE", "salaire"}, {"COTIS", "cotisation"}, {"COTISATION", "cotisation"},
                {"EMPL", "employeur"}, {"EMPLOYEUR", "employeur"}, {"TRAV", "travailleur"}, {"TRAVAILLEUR", "travailleur"},
                {"ASSURE", "assuré"}, {"AFFIL", "affiliation"}, {"AFFILIE", "affilié"}, {"PENSION", "pension"},
                {"PRESTATION", "prestation"}, {"PREST", "prestation"}, {"COMPTE", "compte"}, {"CPT", "compte"},
                {"BANQUE", "banque"}, {"BQ", "banque"}, {"RIB", "RIB"}, {"DUREE", "durée"}, {"MOIS", "mois"},
                {"ANNEE", "année"}, {"AN", "année"}, {"PERIODE", "période"}, {"OBS", "observation"},
                {"OBSERVATION", "observation"}, {"COMMENTAIRE", "commentaire"}, {"MOTIF", "motif"},
                {"REGION", "région"}, {"DISTRICT", "district"}, {"COMMUNE", "commune"}, {"FOKONTANY", "fokontany"},
                {"SIG", "SIG"}, {"GEO", "géographique"}, {"LAT", "latitude"}, {"LON", "longitude"}, {"LNG", "longitude"},
                {"FICHIER", "fichier"}, {"DOC", "document"}, {"PIECE", "pièce"}, {"ECHEANCE", "échéance"},
                {"REMB", "remboursement"}, {"REMBOURSEMENT", "remboursement"}, {"SOLDE", "solde"}, {"ORDRE", "ordre"},
                {"RANG", "rang"}, {"ACTIF", "actif"}, {"FLAG", "indicateur"}, {"MENU", "menu"}, {"APP", "application"},
                {"ROLE", "rôle"}, {"PROFIL", "profil"}, {"GRADE", "grade"}, {"FONCTION", "fonction"}, {"POSTE", "poste"},
                {"CATEGORIE", "catégorie"}, {"CAT", "catégorie"}, {"NATURE", "nature"}, {"ACCORDE", "accordé"},
                {"MENSUALITE", "mensualité"}, {"MENS", "mensualité"}, {"PLAFOND", "plafond"},
                {"SUIVI", "suivi"}, {"V", "vue"}, {"VW", "vue"}, {"TB", ""}, {"T", ""}, {"TBL", ""},
        };
        for (String[] m : mots) VOCABULAIRE.put(m[0], m[1]);
    }

    private static final Set<String> SECRET = new HashSet<>(Arrays.asList(
            "MONTANT", "MT", "MNT", "SALAIRE", "SAL", "SOLDE", "RIB", "IBAN", "BIC", "PASSWORD", "PASSWD", "PWD", "MDP",
            "MOT_DE_PASSE", "TOKEN", "SECRET", "HASH", "PENSION", "REMUNERATION", "REMUN", "PRIME", "INDEMNITE",
            "NET", "BRUT", "MENSUALITE", "CARTE_BANCAIRE", "CVV", "CLE_API", "APIKEY", "SALT"));
    private static final Set<String> CONFIDENTIEL = new HashSet<>(Arrays.asList(
            "MATRICULE", "MAT", "PRENOM", "PRENOMS", "CIN", "NIF", "STAT_NIF", "ADRESSE", "ADR", "TEL", "TELEPHONE",
            "PHONE", "MOBILE", "EMAIL", "MAIL", "NAISSANCE", "NAISS", "SEXE", "PHOTO", "SIGNATURE", "PASSEPORT",
            "AFFILIE", "ASSURE", "TRAVAILLEUR", "BENEFICIAIRE", "CONJOINT", "ENFANT", "SITUATION_FAMILIALE", "COMPTE",
            "CPT", "NUM_COMPTE", "BANQUE", "LOGIN", "USERNAME", "IP", "LATITUDE", "LONGITUDE", "SANTE", "MALADIE"));
    private static final Set<String> INTERNE = new HashSet<>(Arrays.asList(
            "DATE", "DT", "STATUT", "ETAT", "FLAG", "USER", "USR", "UTILISATEUR", "CREATION", "MODIFICATION", "MAJ",
            "VALIDATION", "OBSERVATION", "OBS", "COMMENTAIRE", "MOTIF", "TAUX", "TX", "DUREE", "DELAI", "PLAFOND",
            "NB", "NOMBRE", "ORDRE", "RANG", "VERSION", "ID", "SEQ", "PRIORITE", "ZONE", "SECTION", "BESOIN"));

    private static final Pattern PREFIXES_REF_EXPLICITES = Pattern.compile(
            "^(REF|TYPE|TYP|TR|STATUT|STAT|PARAM|PRM|NOMENCL|LISTE|LST|CODIF)_.*"
          + "|.*_(TYPE|TYPES|STATUT|REF|PARAM|NOMENCLATURE)$"
          + "|^SIG_DR$");
    private static final Pattern PREFIXES_HISTO = Pattern.compile("^(HIST|HISTO|LOG|AUDIT|TRACE|JOURNAL)_.*|.*_(HIST|HISTO|LOG|AUDIT)$");

    // ================================================================== API

    public void enrichir(Collection<ObjetAnalyse> objets) {
        for (ObjetAnalyse o : objets) enrichir(o);
    }

    public void enrichir(ObjetAnalyse o) {
        if (o.flagRef == null) o.flagRef = estReferentiel(o);
        if (vide(o.description)) o.description = descriptionObjet(o);
        for (ColonneAnalyse c : o.colonnes.values()) {
            if (vide(c.description)) c.description = descriptionColonne(c, o);
            if (c.niveauSensibilite == null) c.niveauSensibilite = sensibilite(c, Boolean.TRUE.equals(o.flagRef));
            if (vide(c.observation) && c.fk && !vide(c.reference)) c.observation = "Référence " + c.reference;
        }
        for (TriggerAnalyse t : o.triggers.values()) {
            if (vide(t.description)) t.description = expliquerTrigger(t, o);
        }
    }

    public String descriptionApplication(String libelle, Collection<ObjetAnalyse> objets) {
        long tables = objets.stream().filter(o -> !"VIEW".equals(o.type)).count();
        long vues = objets.size() - tables;
        long refs = objets.stream().filter(o -> Boolean.TRUE.equals(o.flagRef)).count();
        Set<String> schemas = new java.util.TreeSet<>();
        objets.forEach(o -> { if (o.schema != null) schemas.add(o.schema); });
        String nomApp = vide(libelle) ? "Application CNaPS" : libelle.trim();
        return "Application « " + nomApp + " » s'appuyant sur " + tables
                + " table(s) dont " + refs + " référentielle(s) et " + vues + " vue(s)"
                + (schemas.isEmpty() ? "" : " dans le(s) schéma(s) " + String.join(", ", schemas)) + ".";
    }

    // ================================================================== Règles

    boolean estReferentiel(ObjetAnalyse o) {
        if (!"TABLE".equals(o.type)) return false;
        String n = sansSchemaNiPrefixeVue(o.nom);
        if (PREFIXES_HISTO.matcher(n).matches()) return false;
        if (PREFIXES_REF_EXPLICITES.matcher(n).matches()) return true;
        // Petite table « code / libellé » sans clé étrangère ni montant : nomenclature probable
        int nb = o.colonnes.size();
        boolean aLibelle = o.colonnes.keySet().stream().anyMatch(c -> c.contains("LIBELLE") || c.startsWith("LIB"));
        boolean aFk = o.colonnes.values().stream().anyMatch(c -> c.fk);
        boolean aDonneesMetier = o.colonnes.keySet().stream().anyMatch(c -> {
            Set<String> t = jetons(c);
            return t.stream().anyMatch(SECRET::contains) || t.contains("MATRICULE") || t.contains("DATE") || t.contains("DT");
        });
        return nb > 0 && nb <= 6 && aLibelle && !aFk && !aDonneesMetier;
    }

    String descriptionObjet(ObjetAnalyse o) {
        String n = o.nom;
        boolean vue = "VIEW".equals(o.type);
        String base = humaniser(sansSchemaNiPrefixeVue(n).replaceFirst("^(REF|TYPE|TYP|PARAM|HIST|HISTO|LOG|APP)_", ""));
        String nb = o.colonnes.isEmpty() ? "" : " (" + o.colonnes.size() + " colonnes)";
        if (vue) {
            return "Vue de synthèse « " + base + " » consolidant les données issues des tables sources" + nb + ".";
        }
        if (Boolean.TRUE.equals(o.flagRef)) {
            String objet = n.matches("^(TYPE|TYP)_.*|.*_TYPE$") ? "types de " + base.replaceFirst("^types? de ", "")
                    : base;
            return "Table référentielle (nomenclature) des " + objet + ", utilisée pour codifier les données de l'application" + nb + ".";
        }
        if (PREFIXES_HISTO.matcher(n).matches()) {
            return "Table d'historique et d'audit des " + base + " (traçabilité des opérations)" + nb + ".";
        }
        return "Table métier de gestion des " + base + " : enregistre les opérations et informations correspondantes" + nb + ".";
    }

    String descriptionColonne(ColonneAnalyse c, ObjetAnalyse o) {
        String n = c.nom;
        Set<String> t = jetons(n);
        String table = humaniser(sansSchemaNiPrefixeVue(o.nom).replaceFirst("^APP_", ""));

        // Cas spécifiques fréquents dans les applications CNaPS
        switch (n) {
            case "DR_CODE":
                return c.fk ? "Code de la direction régionale de rattachement (clé étrangère vers SIG.SIG_DR)."
                            : "Code unique de représentation régionale (clé primaire).";
            case "DR_NOM":
                return "Nom complet de la direction régionale.";
            case "DR_ABREVIATION":
                return "Sigle ou abréviation de la direction régionale.";
            case "DR_LIEU":
                return "Lieu d'implantation géographique de la direction régionale.";
            case "NUM_MOBILE":
                return "Numéro de téléphone mobile (flotte) du responsable.";
            case "CODE_ZONE":
                return "Code de la zone géographique de représentation.";
            case "PLAFOND_MONTANT":
                return "Montant plafond autorisé en Ariary pour cette catégorie.";
            case "DUREE_MAX_MOIS":
                return "Durée maximale de remboursement autorisée (en mois).";
            case "MENSUALITE_RETENUE":
                return "Montant de la retenue mensuelle prélevée sur salaire (en Ariary).";
            case "DELAI_RESOLUTION_HEURES":
                return "Délai cible de résolution de l'incident (en heures).";
            case "PRIORITE":
                return "Niveau de priorité de traitement (BASSE, MOYENNE, HAUTE, URGENTE).";
            case "MESSAGE_TEXTE":
                return "Contenu textuel du commentaire ou message de suivi.";
            case "TITRE_INCIDENT":
                return "Titre synthétique décrivant l'incident déclaré.";
            case "DESCRIPTION_DETAIL":
                return "Description détaillée du problème ou de l'incident.";
            case "BESOIN_SERVICE":
                return "Décision du service sur l'habilitation (CONSERVER, MODIFIER, SUPPRIMER).";
            case "MOTIF_SUPPRESSION":
                return "Motif justifiant la demande de suppression ou de modification.";
            case "CORRIGE_PAR":
                return "Matricule ou identifiant de l'utilisateur ayant corrigé l'enregistrement.";
            case "ACTION_PAR":
                return "Matricule ou identifiant de l'utilisateur à l'origine de l'action.";
            case "VILLE":
                return "Ville d'implantation.";
            case "ADRESSE_PHYSIQUE":
                return "Adresse géographique et localisation physique.";
            default:
                break;
        }

        String reste = humaniser(n
                .replaceFirst("^(ID|CD|CODE|LIB|LIBELLE|DT|DATE|MT|MNT|MONTANT|NUM|NUMERO|NB|NBR|NOMBRE|FLAG|IS|TX|TAUX|STATUT|STAT|ETAT|ETA|MOTIF|DUREE|DELAI|NOM|PRENOM|TITRE)_", "")
                .replaceFirst("_(ID|CODE|CD|LIB|LIBELLE|DT|DATE|MT|MONTANT|NUM|NB|FLAG|STATUT|ETAT)$", ""));

        if (c.pk && (n.equals("ID") || n.startsWith("ID_") || n.endsWith("_ID"))) {
            return "Identifiant unique (clé primaire) de l'enregistrement « " + table + " ».";
        }
        if (c.fk && !vide(c.reference)) {
            return "Clé étrangère vers " + c.reference.replaceAll("\\(.*", "") + (reste.isEmpty() ? "" : " (" + reste + ")") + ".";
        }
        if (c.pk) return "Clé primaire identifiant " + (reste.isEmpty() ? humaniser(n) : reste) + ".";
        if (n.matches("^(USER|USR|UTILISATEUR|AGENT)_(CREATION|CREA|CRE)$|^(CREATED_BY|CREE_PAR)$")) return "Utilisateur ayant créé l'enregistrement (traçabilité).";
        if (n.matches("^(USER|USR|UTILISATEUR)_(MODIF|MODIFICATION|MAJ)$|^(UPDATED_BY|MODIFIE_PAR)$")) return "Utilisateur ayant effectué la dernière modification (traçabilité).";
        if (n.matches("^(DATE|DT)_(CREATION|CREA|CRE)$|^CREATED_(AT|DATE)$")) return "Date de création de l'enregistrement.";
        if (n.matches("^(DATE|DT)_(MODIF|MODIFICATION|MAJ)$|^UPDATED_(AT|DATE)$")) return "Date de dernière modification de l'enregistrement.";
        if (t.contains("MATRICULE") || t.contains("MAT")) {
            return n.contains("DECLARANT") ? "Matricule de l'agent ayant déclaré l'incident."
                 : n.contains("AUTEUR") ? "Matricule de l'agent auteur du commentaire."
                 : "Matricule de l'agent CNaPS concerné.";
        }
        if (t.contains("CIN")) return "Numéro de la carte d'identité nationale (CIN).";
        if (n.matches("^(NOM|NOMS)(_.*)?$")) {
            if (estNomEntiteOrganisationnelle(n)) {
                return "Intitulé / nom " + deOuD(reste) + ".";
            }
            return "Nom de famille " + (reste.isEmpty() ? "de la personne" : "(" + reste + ")") + ".";
        }
        if (n.matches("^PRENOMS?(_.*)?$")) return "Prénom(s) de la personne.";
        if (t.contains("TEL") || t.contains("TELEPHONE") || t.contains("MOBILE")) return "Numéro de téléphone de contact.";
        if (t.contains("EMAIL") || t.contains("MAIL")) return "Adresse électronique (e-mail).";
        if (t.contains("ADRESSE") || t.contains("ADR")) return "Adresse postale ou géographique.";
        if (t.contains("CODE") || t.contains("CD") || t.contains("COD")) return "Code identifiant " + (reste.isEmpty() ? "de référence" : reste) + ".";
        if (t.contains("LIBELLE") || t.contains("LIB") || t.contains("LBL")) return "Intitulé / libellé " + (reste.isEmpty() ? "descriptif" : deOuD(reste)) + ".";
        if (t.contains("DATE") || t.contains("DT")) return "Date " + (reste.isEmpty() ? "" : deOuD(reste)) + ".";
        if (t.contains("MONTANT") || t.contains("MT") || t.contains("MNT")) return "Montant " + (reste.isEmpty() ? "" : deOuD(reste) + " ") + "en Ariary (MGA).";
        if (t.contains("TAUX") || t.contains("TX")) return "Taux " + (reste.isEmpty() ? "" : deOuD(reste) + " ") + "(en pourcentage).";
        if (t.contains("NB") || t.contains("NBR") || t.contains("NOMBRE")) return "Nombre " + (reste.isEmpty() ? "" : deOuD(reste)) + ".";
        if (t.contains("NUM") || t.contains("NUMERO") || t.contains("NO")) return "Numéro " + (reste.isEmpty() ? "" : deOuD(reste)) + ".";
        if (t.contains("STATUT") || t.contains("ETAT")) return "Statut d'avancement " + (reste.isEmpty() ? "de l'enregistrement" : "(" + reste + ")") + " dans le circuit de traitement.";
        if (t.contains("FLAG") || n.startsWith("IS_") || n.startsWith("EST_") || t.contains("ACTIF")) return "Indicateur logique (O/N) : " + (reste.isEmpty() ? "actif" : reste) + ".";
        if (t.contains("DESCRIPTION") || t.contains("DESC")) return "Description détaillée.";
        if (t.contains("OBSERVATION") || t.contains("OBS") || t.contains("COMMENTAIRE")) return "Observation ou commentaire complémentaire.";
        if (t.contains("MOTIF")) return "Motif " + (reste.isEmpty() ? "de l'opération" : deOuD(reste)) + ".";
        if (t.contains("DUREE")) return "Durée " + (reste.isEmpty() ? "" : reste) + ".";
        if (n.equals("ID") || t.contains("ID")) return "Identifiant technique " + (reste.isEmpty() ? "" : "(" + reste + ")") + ".";
        return humaniserPhrase(n) + ".";
    }

    int sensibilite(ColonneAnalyse c, boolean tableReferentielle) {
        Set<String> t = jetons(c.nom);
        String n = c.nom;
        // Un barème/plafond dans une table référentielle est une règle interne (Niveau 2), pas un salaire personnel (Niveau 4)
        if (t.contains("PLAFOND") || t.contains("DUREE") || t.contains("DELAI") || t.contains("ZONE")) {
            return 2;
        }
        if (t.stream().anyMatch(SECRET::contains) || n.contains("PASSWORD") || n.contains("MOT_DE_PASSE")) return 4;
        if (t.stream().anyMatch(CONFIDENTIEL::contains)) return 3;
        if (t.contains("NOM")) {
            // Seul le nom d'une personne physique est de niveau 3 (Confidentiel)
            return estNomEntiteOrganisationnelle(n) ? 2 : 3;
        }
        if (tableReferentielle) {
            return 1;
        }
        if (t.contains("CODE") || t.contains("LIBELLE") || t.contains("LIB") || t.contains("CD") || t.contains("ACTIF")) {
            return c.fk ? 2 : 1;
        }
        if (c.pk || c.fk || t.stream().anyMatch(INTERNE::contains)) return 2;
        return 2; // par prudence : interne par défaut
    }

    private static boolean estNomEntiteOrganisationnelle(String nomColonne) {
        return nomColonne.contains("DR_") || nomColonne.contains("_DR")
                || nomColonne.contains("AGENCE") || nomColonne.contains("SERVICE")
                || nomColonne.contains("DIRECTION") || nomColonne.contains("TABLE")
                || nomColonne.contains("VIEW") || nomColonne.contains("INDEX")
                || nomColonne.contains("TRIGGER") || nomColonne.contains("FUNCTION")
                || nomColonne.contains("APP") || nomColonne.contains("CATEGORIE")
                || nomColonne.contains("TYPE") || nomColonne.contains("REGION")
                || nomColonne.contains("VILLE") || nomColonne.contains("ZONE");
    }

    private static String deOuD(String texte) {
        if (vide(texte)) return "";
        String t = texte.trim();
        char c = Character.toLowerCase(t.charAt(0));
        if ("aeéèêiîoôuùûyh".indexOf(c) >= 0) {
            return "d'" + t;
        }
        return "de " + t;
    }

    String expliquerTrigger(TriggerAnalyse t, ObjetAnalyse o) {
        String ev = t.evenement == null ? "" : t.evenement.toUpperCase(Locale.ROOT);
        String moment = ev.startsWith("BEFORE") ? "avant" : ev.startsWith("AFTER") ? "après"
                : ev.startsWith("INSTEAD") ? "à la place de" : "lors de";
        List<String> ops = new ArrayList<>();
        if (ev.contains("INSERT")) ops.add("insertion");
        if (ev.contains("UPDATE")) ops.add("modification");
        if (ev.contains("DELETE")) ops.add("suppression");
        String quand = "Déclencheur exécuté " + moment + " " + (ops.isEmpty() ? "chaque opération" : String.join(" / ", ops))
                + " sur " + o.nom;

        List<String> actions = new ArrayList<>();
        String corps = t.corps == null ? "" : t.corps.toUpperCase(Locale.ROOT);
        Matcher seq = Pattern.compile("([\\w$#.]+)\\s*\\.\\s*NEXTVAL").matcher(corps);
        if (seq.find()) actions.add("attribue automatiquement l'identifiant via la séquence " + seq.group(1));
        if (corps.contains("SYSDATE") || corps.contains("SYSTIMESTAMP") || corps.contains("CURRENT_DATE")) actions.add("horodate l'enregistrement avec la date système");
        if (corps.matches("(?s).*:NEW\\.\\w+\\s*:=\\s*(USER|SYS_CONTEXT).*")) actions.add("trace l'utilisateur Oracle à l'origine de l'opération");
        if (corps.contains("RAISE_APPLICATION_ERROR")) actions.add("bloque l'opération si une règle de gestion n'est pas respectée");
        Matcher histo = Pattern.compile("INSERT\\s+INTO\\s+([\\w$#.]+)").matcher(corps);
        if (histo.find()) actions.add("alimente la table " + histo.group(1) + " (historisation / journalisation)");
        if (corps.matches("(?s).*\\bUPPER\\s*\\(.*") || corps.contains("TRIM(")) actions.add("formate et normalise les codes saisis en majuscules");
        Matcher upd = Pattern.compile("\\bUPDATE\\s+([\\w$#.]+)\\s+SET\\b").matcher(corps);
        if (upd.find()) actions.add("met à jour automatiquement la table liée " + upd.group(1));
        if (actions.isEmpty()) actions.add("applique une règle de gestion automatique (logique à préciser)");
        return quand + " : " + String.join(", ", actions) + ".";
    }

    // ================================================================== Outils texte

    private static String sansSchemaNiPrefixeVue(String nom) {
        return nom.replaceFirst("^(V|VW|VUE)_", "");
    }

    private static Set<String> jetons(String nom) {
        Set<String> t = new HashSet<>(Arrays.asList(nom.toUpperCase(Locale.ROOT).split("_+")));
        t.add(nom.toUpperCase(Locale.ROOT));
        return t;
    }

    static String humaniser(String nom) {
        if (vide(nom)) return "";
        List<String> mots = new ArrayList<>();
        for (String j : nom.toUpperCase(Locale.ROOT).split("_+")) {
            if (j.isEmpty()) continue;
            String m = VOCABULAIRE.getOrDefault(j, j.toLowerCase(Locale.ROOT));
            if (!m.isEmpty()) mots.add(m);
        }
        return String.join(" ", mots);
    }

    private static String humaniserPhrase(String nom) {
        String h = humaniser(nom);
        return h.isEmpty() ? nom : Character.toUpperCase(h.charAt(0)) + h.substring(1);
    }
}
