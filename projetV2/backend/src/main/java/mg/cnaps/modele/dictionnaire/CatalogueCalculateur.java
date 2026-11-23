package mg.cnaps.modele.dictionnaire;

import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Application;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.ApplicationCatalogue;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Objet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.ObjetCatalogue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Calcul PUR (aucun accès base) du catalogue unique : fusion du référentiel officiel et des
 * applications documentées, puis calcul des statuts. Testable sans Oracle.
 *
 * Règles :
 * - dans le référentiel, sans objet documenté        -> EXISTANTE_NON_DOCUMENTEE
 * - dans le référentiel, avec au moins un objet      -> EXISTANTE_DOCUMENTEE
 * - hors référentiel, avec au moins un objet         -> EN_CONCEPTION
 * - hors référentiel et sans objet                   -> ignorée (ne peut pas se produire en pratique)
 *
 * Code d'une application : APP_ID du dictionnaire est un UUID généré (modèle ApplicationLucy), il ne vaut donc
 * jamais un code du référentiel. Quand l'objet porte un APP_CODE_REF, c'est ce code de l'application existante
 * qui identifie l'application au catalogue ; sinon, le code reste l'APP_ID (application en conception).
 */
public final class CatalogueCalculateur {

    public static final String EXISTANTE_NON_DOCUMENTEE = "EXISTANTE_NON_DOCUMENTEE";
    public static final String EXISTANTE_DOCUMENTEE = "EXISTANTE_DOCUMENTEE";
    public static final String EN_CONCEPTION = "EN_CONCEPTION";
    public static final String CREE = "CREE";
    public static final String NON_CREE = "NON_CREE";

    private static final Set<String> STATUTS = new LinkedHashSet<>(
            java.util.Arrays.asList(EXISTANTE_NON_DOCUMENTEE, EXISTANTE_DOCUMENTEE, EN_CONCEPTION));

    private CatalogueCalculateur() { }

    public static boolean estStatutValide(String statut) {
        return statut != null && STATUTS.contains(statut);
    }

    /** Même normalisation que le SQL existant : UPPER(TRIM(...)). Retourne null si vide. */
    public static String normaliserCode(String code) {
        if (code == null) return null;
        String c = code.trim().toUpperCase(Locale.ROOT);
        return c.isEmpty() ? null : c;
    }

    /**
     * Un code du référentiel est exploitable s'il compte au moins 2 caractères dont au moins une lettre ou un
     * chiffre. Écarte les valeurs parasites de CPT_MENU (« - », « A »). Appliqué au référentiel officiel
     * uniquement : un objet déjà documenté n'est jamais masqué par ce filtre.
     */
    public static boolean estCodeReferentielExploitable(String codeNormalise) {
        if (codeNormalise == null || codeNormalise.length() < 2) return false;
        for (int i = 0; i < codeNormalise.length(); i++) {
            if (Character.isLetterOrDigit(codeNormalise.charAt(i))) return true;
        }
        return false;
    }

    /** Code d'application au catalogue : APP_CODE_REF s'il est renseigné, sinon APP_ID (normalisés). Null si aucun. */
    public static String codeEffectif(Objet o) {
        String ref = normaliserCode(o.appCodeRef);
        return ref != null ? ref : normaliserCode(o.appCode);
    }

    /** Statut d'une application, ou null si elle ne doit pas figurer au catalogue. */
    public static String statut(boolean dansReferentiel, int nbObjetsDocumentes) {
        if (dansReferentiel) {
            return nbObjetsDocumentes > 0 ? EXISTANTE_DOCUMENTEE : EXISTANTE_NON_DOCUMENTEE;
        }
        return nbObjetsDocumentes > 0 ? EN_CONCEPTION : null;
    }

    /** Clé d'un objet pour comparer avec l'ensemble des objets présents dans Oracle : SCHEMA.NOM (majuscules). */
    public static String cleObjet(String schema, String nom) {
        return (schema == null ? "" : schema.trim().toUpperCase(Locale.ROOT)) + "."
                + (nom == null ? "" : nom.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * @param referentiel     code (déjà normalisé ou non) -> libellé ; vide si le référentiel est indisponible
     * @param documentees     résultat de DictionnaireRepository.applications()
     * @param objets          résultat de DictionnaireRepository.objets(null, null, null)
     * @param existantsOracle clés SCHEMA.NOM présentes dans Oracle ; null si le catalogue Oracle est illisible
     *                        (tous les objets sont alors comptés « non créés »)
     * @param statutFiltre    statut à conserver, ou null pour tout conserver
     */
    public static List<ApplicationCatalogue> fusionner(Map<String, String> referentiel,
                                                       List<Application> documentees,
                                                       List<Objet> objets,
                                                       Set<String> existantsOracle,
                                                       String statutFiltre) {
        Map<String, String> libellesRef = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : referentiel.entrySet()) {
            String code = normaliserCode(e.getKey());
            if (!estCodeReferentielExploitable(code)) continue;
            String lib = e.getValue() == null ? "" : e.getValue().trim();
            libellesRef.merge(code, lib, (ancien, nouveau) -> ancien.isEmpty() ? nouveau : ancien);
        }

        // APP_ID (UUID) -> code effectif, pour rattacher les applications documentées à leur code du référentiel.
        Map<String, String> codeParAppId = new LinkedHashMap<>();
        Map<String, List<Objet>> objetsParApp = new LinkedHashMap<>();
        for (Objet o : objets) {
            String code = codeEffectif(o);
            if (code == null) continue;
            String appId = normaliserCode(o.appCode);
            if (appId != null) codeParAppId.putIfAbsent(appId, code);
            objetsParApp.computeIfAbsent(code, k -> new ArrayList<>()).add(o);
        }

        Map<String, String> libellesDoc = new LinkedHashMap<>();
        for (Application a : documentees) {
            String appId = normaliserCode(a.code);
            if (appId == null) continue;
            String code = codeParAppId.getOrDefault(appId, appId);
            libellesDoc.putIfAbsent(code, a.libelle == null ? "" : a.libelle.trim());
        }

        Set<String> codes = new LinkedHashSet<>(libellesRef.keySet());
        codes.addAll(libellesDoc.keySet());
        codes.addAll(objetsParApp.keySet());

        List<ApplicationCatalogue> resultat = new ArrayList<>();
        for (String code : codes) {
            List<Objet> deLApp = objetsParApp.getOrDefault(code, new ArrayList<>());
            String statut = statut(libellesRef.containsKey(code), deLApp.size());
            if (statut == null || (statutFiltre != null && !statutFiltre.equals(statut))) continue;

            String libelle = premierNonVide(libellesRef.get(code), libellesDoc.get(code), code);
            Set<String> schemas = new TreeSet<>();
            int crees = 0;
            for (Objet o : deLApp) {
                if (o.schema != null && !o.schema.trim().isEmpty()) schemas.add(o.schema.trim().toUpperCase(Locale.ROOT));
                if (existeDansOracle(o, existantsOracle)) crees++;
            }
            resultat.add(new ApplicationCatalogue(code, libelle, statut, new ArrayList<>(schemas), deLApp.size(), crees));
        }
        resultat.sort(Comparator.comparing((ApplicationCatalogue a) -> a.libelle, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(a -> a.code));
        return resultat;
    }

    public static ObjetCatalogue versObjetCatalogue(Objet o, Set<String> existantsOracle) {
        String type = "VIEW".equalsIgnoreCase(o.type) ? "VIEW" : "TABLE";
        return new ObjetCatalogue(o.schema, o.nom, type,
                existeDansOracle(o, existantsOracle) ? CREE : NON_CREE,
                o.description == null ? "" : o.description);
    }

    private static boolean existeDansOracle(Objet o, Set<String> existantsOracle) {
        return existantsOracle != null && existantsOracle.contains(cleObjet(o.schema, o.nom));
    }

    private static String premierNonVide(String... valeurs) {
        for (String v : valeurs) {
            if (v != null && !v.trim().isEmpty()) return v;
        }
        return "";
    }
}
