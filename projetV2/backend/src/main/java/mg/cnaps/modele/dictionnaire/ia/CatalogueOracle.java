package mg.cnaps.modele.dictionnaire.ia;

import mg.cnaps.modele.dictionnaire.ia.IaModeles.ColonneAnalyse;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.ObjetAnalyse;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.TriggerAnalyse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static mg.cnaps.modele.dictionnaire.ia.IaModeles.vide;

/**
 * Étape 3 du plan — Croisement avec le catalogue système Oracle.
 *
 * <p>Pour chaque objet découvert dans le code :</p>
 * <ul>
 *   <li>vérifie son existence (ALL_OBJECTS) et retrouve son schéma s'il n'était pas qualifié ;</li>
 *   <li>si l'objet existe : impose les types physiques réels (ALL_TAB_COLUMNS), les contraintes déclarées
 *       (ALL_CONSTRAINTS), les index (ALL_IND_COLUMNS), les triggers (ALL_TRIGGERS), le script de vue (ALL_VIEWS)
 *       et les commentaires (ALL_TAB_COMMENTS / ALL_COL_COMMENTS) ;</li>
 *   <li>sinon : conserve la structure issue du code (application en conception).</li>
 * </ul>
 *
 * <p><b>Sécurité :</b> uniquement des vues de catalogue (métadonnées). Aucune requête n'interroge le contenu
 * des tables métier, aucun comptage de lignes.</p>
 */
@Component
public class CatalogueOracle {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogueOracle.class);

    private final JdbcTemplate jdbc;

    public CatalogueOracle(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Croise un objet avec Oracle. Retourne false si le catalogue est inaccessible. */
    public boolean croiser(ObjetAnalyse o) {
        try {
            if (o.schema == null) {
                List<String> proprietaires = jdbc.query(
                        "SELECT owner FROM all_objects WHERE object_name = ? AND object_type IN ('TABLE','VIEW') "
                      + " AND owner NOT IN ('SYS','SYSTEM','PUBLIC') ORDER BY owner",
                        (rs, i) -> rs.getString(1), o.nom);
                if (proprietaires.isEmpty()) {
                    o.alertes.add("Schéma non précisé et objet introuvable dans Oracle : schéma à renseigner.");
                    return true;
                }
                o.schema = proprietaires.get(0);
                if (proprietaires.size() > 1) {
                    o.alertes.add("Objet présent dans plusieurs schémas " + proprietaires + " : " + o.schema + " retenu, à vérifier.");
                }
            }

            List<String> types = jdbc.query(
                    "SELECT object_type FROM all_objects WHERE owner = ? AND object_name = ? AND object_type IN ('TABLE','VIEW')",
                    (rs, i) -> rs.getString(1), o.schema, o.nom);
            o.existeEnBase = !types.isEmpty();
            if (!o.existeEnBase) {
                o.alertes.add("Objet absent de la base Oracle : structure déduite du code source (application en conception).");
                return true;
            }
            o.type = types.contains("VIEW") && !types.contains("TABLE") ? "VIEW" : "TABLE";
            o.sources.add("Catalogue Oracle (ALL_*)");

            commentaireTable(o);
            colonnes(o);
            contraintes(o);
            index(o);
            triggers(o);
            if ("VIEW".equals(o.type)) scriptVue(o);
            return true;
        } catch (DataAccessException e) {
            LOG.warn("Catalogue Oracle inaccessible pour {} : {}", o.cle(), e.getMessage());
            o.alertes.add("Catalogue Oracle inaccessible : structure non vérifiée.");
            return false;
        }
    }

    /** L'objet est-il déjà documenté dans IAM.DICO_DATA_TABLE ? */
    public boolean dejaDocumente(String schema, String nom) {
        if (schema == null || nom == null) return false;
        try {
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM IAM.DICO_DATA_TABLE WHERE UPPER(TRIM(SCHEMAS)) = ? AND UPPER(TRIM(NOM_TABLE)) = ?",
                    Integer.class, schema, nom);
            return n != null && n > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }

    private void commentaireTable(ObjetAnalyse o) {
        List<String> c = jdbc.query(
                "SELECT comments FROM all_tab_comments WHERE owner = ? AND table_name = ? AND comments IS NOT NULL",
                (rs, i) -> rs.getString(1), o.schema, o.nom);
        if (!c.isEmpty() && vide(o.description)) o.description = c.get(0).trim();
    }

    private void colonnes(ObjetAnalyse o) {
        Map<String, ColonneAnalyse> ordonnees = new LinkedHashMap<>();
        // DATA_DEFAULT est de type LONG : il doit être lu en dernier.
        jdbc.query(
                "SELECT c.column_name, c.data_type, c.data_length, c.data_precision, c.data_scale, c.char_length, "
              + "       c.char_used, c.nullable, cc.comments, c.data_default "
              + "  FROM all_tab_columns c "
              + "  LEFT JOIN all_col_comments cc ON cc.owner = c.owner AND cc.table_name = c.table_name "
              + "       AND cc.column_name = c.column_name "
              + " WHERE c.owner = ? AND c.table_name = ? ORDER BY c.column_id",
                rs -> {
                    String nom = rs.getString(1);
                    ColonneAnalyse col = o.colonnes.containsKey(nom) ? o.colonnes.get(nom) : new ColonneAnalyse(nom);
                    String type = rs.getString(2);
                    col.typeBase = type;
                    col.taille = taille(type, rs.getObject(3), rs.getObject(4), rs.getObject(5), rs.getObject(6));
                    col.nullable = !"N".equals(rs.getString(8));
                    String comm = rs.getString(9);
                    if (!vide(comm) && vide(col.description)) col.description = comm.trim();
                    String defaut = rs.getString(10);
                    col.defaut = vide(defaut) ? null : defaut.trim();
                    col.typeFiable = true;
                    ordonnees.put(nom, col);
                },
                o.schema, o.nom);
        if (ordonnees.isEmpty()) return;

        // Colonnes du code absentes d'Oracle : signalées (écart code / base)
        for (ColonneAnalyse c : o.colonnes.values()) {
            if (!ordonnees.containsKey(c.nom)) {
                o.alertes.add("Colonne " + c.nom + " présente dans le code mais absente d'Oracle (écart à vérifier).");
            }
        }
        o.colonnes.clear();
        o.colonnes.putAll(ordonnees);
    }

    private static String taille(String type, Object longueur, Object precision, Object echelle, Object longueurCar) {
        String t = type == null ? "" : type.toUpperCase(Locale.ROOT);
        if (t.contains("CHAR") || t.equals("RAW")) {
            Object l = longueurCar != null && !"0".equals(String.valueOf(longueurCar)) ? longueurCar : longueur;
            return l == null ? null : String.valueOf(l);
        }
        if (t.equals("NUMBER") || t.equals("FLOAT")) {
            if (precision == null) return null;
            String e = echelle == null ? "0" : String.valueOf(echelle);
            return "0".equals(e) ? String.valueOf(precision) : precision + "," + e;
        }
        return null;
    }

    private void contraintes(ObjetAnalyse o) {
        jdbc.query(
                "SELECT k.constraint_type, kc.column_name, r.owner, r.table_name, rc.column_name "
              + "  FROM all_constraints k "
              + "  JOIN all_cons_columns kc ON kc.owner = k.owner AND kc.constraint_name = k.constraint_name "
              + "  LEFT JOIN all_constraints r ON r.owner = k.r_owner AND r.constraint_name = k.r_constraint_name "
              + "  LEFT JOIN all_cons_columns rc ON rc.owner = r.owner AND rc.constraint_name = r.constraint_name "
              + "       AND rc.position = kc.position "
              + " WHERE k.owner = ? AND k.table_name = ? AND k.constraint_type IN ('P','U','R') "
              + " ORDER BY k.constraint_name, kc.position",
                rs -> {
                    ColonneAnalyse col = o.colonnes.get(rs.getString(2));
                    if (col == null) return;
                    switch (rs.getString(1)) {
                        case "P":
                            col.pk = true;
                            break;
                        case "U":
                            col.uk = true;
                            break;
                        default:
                            col.fk = true;
                            col.reference = rs.getString(3) + "." + rs.getString(4) + "(" + rs.getString(5) + ")";
                    }
                },
                o.schema, o.nom);
    }

    private void index(ObjetAnalyse o) {
        jdbc.query(
                "SELECT index_name, column_name FROM all_ind_columns "
              + " WHERE table_owner = ? AND table_name = ? ORDER BY index_name, column_position",
                rs -> {
                    ColonneAnalyse col = o.colonnes.get(rs.getString(2));
                    if (col != null) col.index.add(rs.getString(1));
                },
                o.schema, o.nom);
    }

    private void triggers(ObjetAnalyse o) {
        // TRIGGER_BODY est de type LONG : lu en dernier. Il s'agit de code (métadonnée), pas de données métier.
        jdbc.query(
                "SELECT t.trigger_name, t.trigger_type, t.triggering_event, t.trigger_body "
              + "  FROM all_triggers t WHERE t.table_owner = ? AND t.table_name = ? ORDER BY t.trigger_name",
                rs -> {
                    TriggerAnalyse t = o.trigger(rs.getString(1));
                    String type = rs.getString(2) == null ? "" : rs.getString(2).replace(" EACH ROW", "").replace(" STATEMENT", "");
                    t.evenement = (type + " " + (rs.getString(3) == null ? "" : rs.getString(3))).trim();
                    String corps = rs.getString(4);
                    if (!vide(corps)) t.corps = CodeAnalyzer.tronquer(corps.trim(), 4000);
                },
                o.schema, o.nom);

        // Séquences utilisées par les triggers (dépendances déclarées)
        jdbc.query(
                "SELECT DISTINCT d.referenced_owner, d.referenced_name FROM all_dependencies d "
              + "  JOIN all_triggers t ON t.owner = d.owner AND t.trigger_name = d.name "
              + " WHERE d.type = 'TRIGGER' AND d.referenced_type = 'SEQUENCE' "
              + "   AND t.table_owner = ? AND t.table_name = ?",
                rs -> {
                    String refOwner = rs.getString(1);
                    String refName = rs.getString(2);
                    String seq = refOwner + "." + refName;
                    boolean present = o.sequences.stream()
                            .anyMatch(s -> s.equals(seq) || s.equals(refName));
                    if (!present) o.sequences.add(seq);
                },
                o.schema, o.nom);
    }

    private void scriptVue(ObjetAnalyse o) {
        List<String> txt = jdbc.query(
                "SELECT text FROM all_views WHERE owner = ? AND view_name = ?",
                (rs, i) -> rs.getString(1), o.schema, o.nom);
        if (!txt.isEmpty() && !vide(txt.get(0))) o.viewScript = txt.get(0).trim();
    }
}
