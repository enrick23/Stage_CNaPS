package mg.cnaps.modele.service;
import mg.cnaps.modele.dto.ColonneDTO;
import mg.cnaps.modele.dto.DatabaseDTO;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DatabaseDTO> listerTables() {
        String sql = "SELECT owner, table_name FROM all_tables "
                   + "WHERE owner NOT IN ('SYS', 'SYSTEM', 'SYSAUX', 'OUTLN', "
                   + "                    'DBSNMP', 'APPQOSSYS', 'XDB', 'MDSYS') "
                   + "ORDER BY owner, table_name";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        List<DatabaseDTO> resultat = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String owner = (String) row.get("OWNER");
            String table = (String) row.get("TABLE_NAME");
            String nomComplet = owner + "." + table;

            DatabaseDTO dto = new DatabaseDTO(nomComplet);
            try {
                Integer nbCol = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM all_tab_columns "
                        + "WHERE owner = ? AND table_name = ?",
                        Integer.class, owner, table);
                dto.setNombreColonnes(nbCol != null ? nbCol : 0);

                Integer nbLig = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM " + owner + "." + table,
                        Integer.class);
                dto.setNombreLignes(nbLig != null ? nbLig : 0);
            } catch (Exception e) {
                dto.setNombreColonnes(0);
                dto.setNombreLignes(0);
            }
            resultat.add(dto);
        }
        return resultat;
    }

    public List<ColonneDTO> getColonnes(String nomTable) {
        String owner;
        String table;

        if (nomTable.contains(".")) {
            String[] parts = nomTable.split("\\.", 2);
            owner = parts[0].toUpperCase();
            table = parts[1].toUpperCase();
        } else {
            owner = null;
            table = nomTable.toUpperCase();
        }

        String sql;
        List<Map<String, Object>> rows;

        if (owner != null) {
            sql = "SELECT column_name, data_type, data_length, nullable "
                + "FROM all_tab_columns "
                + "WHERE owner = ? AND table_name = ? "
                + "ORDER BY column_id";
            rows = jdbcTemplate.queryForList(sql, owner, table);
        } else {
            sql = "SELECT column_name, data_type, data_length, nullable "
                + "FROM all_tab_columns "
                + "WHERE table_name = ? "
                + "ORDER BY column_id";
            rows = jdbcTemplate.queryForList(sql, table);
        }

        List<ColonneDTO> colonnes = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            colonnes.add(new ColonneDTO(
                    (String) row.get("COLUMN_NAME"),
                    (String) row.get("DATA_TYPE"),
                    row.get("DATA_LENGTH") != null
                            ? ((Number) row.get("DATA_LENGTH")).intValue()
                            : null,
                    "Y".equals(row.get("NULLABLE"))
            ));
        }
        return colonnes;
    }

    public List<Map<String, Object>> getContenu(String nomTable) {
        if (nomTable == null || !nomTable.matches("^[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)?$")) {
            throw new IllegalArgumentException("Nom de table invalide : " + nomTable);
        }
        String sql = "SELECT * FROM " + nomTable.toUpperCase()
                   + " FETCH FIRST 100 ROWS ONLY";
        return jdbcTemplate.queryForList(sql);
    }
}