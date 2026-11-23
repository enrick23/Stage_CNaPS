package mg.cnaps.modele.dictionnaire;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Configuration de la source du référentiel officiel des applications existantes de la CNaPS.
 *
 * Sources validées : TRESO.CPT_MENU, TRESO.CPT_MENU_NEW et TRESO.CPT_MENU_OLD (même schéma de colonnes,
 * union de 1945 codes distincts constatée sur la base). Limite connue : ces tables décrivent des menus
 * et écrans Oracle Forms (MENU_FMX) que le portail désigne comme des « applications ».
 * Les tables sont listées par ordre de priorité : le libellé de la première table qui connaît un code l'emporte.
 *
 * Les noms de table et de colonnes proviennent UNIQUEMENT de application.properties
 * (jamais d'une requête HTTP). Comme ils sont concaténés dans le SQL (un nom d'objet ne peut pas
 * être un paramètre lié), chacun est validé par une expression régulière stricte avant utilisation.
 */
@Component
public class ReferentielApplicationsConfig {

    /** Table, éventuellement qualifiée par son schéma : MA_TABLE ou SCHEMA.MA_TABLE. */
    private static final Pattern TABLE = Pattern.compile("^[A-Za-z0-9_$]+(\\.[A-Za-z0-9_$]+)?$");
    /** Colonne : jamais qualifiée. */
    private static final Pattern COLONNE = Pattern.compile("^[A-Za-z0-9_$]+$");

    private final String tables;
    private final String colonneCode;
    private final String colonneLibelle;

    public ReferentielApplicationsConfig(
            @Value("${dictionnaire.referentiel.tables:TRESO.CPT_MENU,TRESO.CPT_MENU_NEW,TRESO.CPT_MENU_OLD}") String tables,
            @Value("${dictionnaire.referentiel.colonne-code:MENU_CODE}") String colonneCode,
            @Value("${dictionnaire.referentiel.colonne-libelle:MENU_LIBELLE}") String colonneLibelle) {
        this.tables = tables;
        this.colonneCode = colonneCode;
        this.colonneLibelle = colonneLibelle;
    }

    /**
     * Requête de lecture du référentiel. Validation faite à l'appel (et non au démarrage) : une
     * configuration erronée désactive uniquement le catalogue, pas le reste de l'application.
     *
     * @throws IllegalStateException si l'un des noms n'est pas un identifiant Oracle sûr
     */
    public String requete() {
        return construireRequete(tables, colonneCode, colonneLibelle);
    }

    /**
     * Pure et statique pour être testable sans contexte Spring.
     * Colonnes retournées : 1 = code normalisé (UPPER(TRIM)), 2 = libellé. Le résultat est trié par
     * priorité de table (UNION ALL : les doublons sont fusionnés côté Java, le premier libellé non vide gagne).
     *
     * @param tables liste de tables séparées par des virgules, par ordre de priorité
     */
    static String construireRequete(String tables, String colonneCode, String colonneLibelle) {
        verifier("dictionnaire.referentiel.colonne-code", colonneCode, COLONNE);
        verifier("dictionnaire.referentiel.colonne-libelle", colonneLibelle, COLONNE);
        if (tables == null || tables.trim().isEmpty()) {
            throw new IllegalStateException("Propriété « dictionnaire.referentiel.tables » vide : au moins une table est requise.");
        }
        StringBuilder sql = new StringBuilder("SELECT REF_CODE, REF_LIBELLE FROM (");
        int rang = 0;
        for (String brut : tables.split(",", -1)) {
            String table = brut.trim();
            verifier("dictionnaire.referentiel.tables", table, TABLE);
            if (rang > 0) sql.append(" UNION ALL ");
            rang++;
            sql.append("SELECT UPPER(TRIM(").append(colonneCode).append(")) AS REF_CODE, ")
               .append(colonneLibelle).append(" AS REF_LIBELLE, ").append(rang).append(" AS REF_RANG")
               .append(" FROM ").append(table)
               .append(" WHERE ").append(colonneCode).append(" IS NOT NULL");
        }
        return sql.append(") ORDER BY REF_RANG").toString();
    }

    private static void verifier(String propriete, String valeur, Pattern motif) {
        if (valeur == null || !motif.matcher(valeur).matches()) {
            throw new IllegalStateException("Propriété « " + propriete + " » invalide : « " + valeur
                    + " » (caractères autorisés : lettres, chiffres, _ et $"
                    + (motif == TABLE ? ", et un seul . pour le schéma" : "") + ").");
        }
    }
}
