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
            if (code == null) continue;
            String lib = e.getValue() == null ? "" : e.getValue().trim();
            libellesRef.merge(code, lib, (ancien, nouveau) -> ancien.isEmpty() ? nouveau : ancien);
        }

        Map<String, String> libellesDoc = new LinkedHashMap<>();
        for (Application a : documentees) {
            String code = normaliserCode(a.code);
            if (code != null) libellesDoc.putIfAbsent(code, a.libelle == null ? "" : a.libelle.trim());
        }

        Map<String, List<Objet>> objetsParApp = new LinkedHashMap<>();
        for (Objet o : objets) {
            String code = normaliserCode(o.appCode);
            if (code != null) objetsParApp.computeIfAbsent(code, k -> new ArrayList<>()).add(o);
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
