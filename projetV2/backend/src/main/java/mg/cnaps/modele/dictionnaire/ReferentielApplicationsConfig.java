package mg.cnaps.modele.dictionnaire;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Configuration de la source du référentiel officiel des applications existantes de la CNaPS.
 *
 * Les noms de table et de colonnes proviennent UNIQUEMENT de application.properties
 * (jamais d'une requête HTTP). Comme ils sont concaténés dans le SQL (un nom d'objet ne peut pas
 * être un paramètre lié), chacun est validé par une expression régulière stricte avant utilisation.
 *
 * À VALIDER avec la base (RJ45) : la table source réelle, et le sens exact de ses colonnes.
 */
@Component
public class ReferentielApplicationsConfig {

    /** Table, éventuellement qualifiée par son schéma : MA_TABLE ou SCHEMA.MA_TABLE. */
    private static final Pattern TABLE = Pattern.compile("^[A-Za-z0-9_$]+(\\.[A-Za-z0-9_$]+)?$");
    /** Colonne : jamais qualifiée. */
    private static final Pattern COLONNE = Pattern.compile("^[A-Za-z0-9_$]+$");

    private final String table;
    private final String colonneCode;
    private final String colonneLibelle;

    // À VALIDER avec la base (RJ45) : valeurs par défaut de CPT_MENU déduites de sql/local_treso.sql (copie locale),
    // à confirmer sur la base réelle (CPT_MENU décrit peut-être des menus et non des applications).
    public ReferentielApplicationsConfig(
            @Value("${dictionnaire.referentiel.table:TRESO.CPT_MENU}") String table,
            @Value("${dictionnaire.referentiel.colonne-code:MENU_CODE}") String colonneCode,
            @Value("${dictionnaire.referentiel.colonne-libelle:MENU_LIBELLE}") String colonneLibelle) {
        this.table = table;
        this.colonneCode = colonneCode;
        this.colonneLibelle = colonneLibelle;
    }

    /**
     * Requête de lecture du référentiel. Validation faite à l'appel (et non au démarrage) : une
     * configuration erronée désactive uniquement le catalogue, pas le reste de l'application.
     *
     * @throws IllegalStateException si l'un des trois noms n'est pas un identifiant Oracle sûr
     */
    public String requete() {
        return construireRequete(table, colonneCode, colonneLibelle);
    }

    /** Pure et statique pour être testable sans contexte Spring. */
    static String construireRequete(String table, String colonneCode, String colonneLibelle) {
        verifier("dictionnaire.referentiel.table", table, TABLE);
        verifier("dictionnaire.referentiel.colonne-code", colonneCode, COLONNE);
        verifier("dictionnaire.referentiel.colonne-libelle", colonneLibelle, COLONNE);
        return "SELECT UPPER(TRIM(" + colonneCode + ")), " + colonneLibelle
                + " FROM " + table
                + " WHERE " + colonneCode + " IS NOT NULL";
    }

    private static void verifier(String propriete, String valeur, Pattern motif) {
        if (valeur == null || !motif.matcher(valeur.trim()).matches() || !valeur.equals(valeur.trim())) {
            throw new IllegalStateException("Propriété « " + propriete + " » invalide : « " + valeur
                    + " » (caractères autorisés : lettres, chiffres, _ et $"
                    + (motif == TABLE ? ", et un seul . pour le schéma" : "") + ").");
        }
    }
}
