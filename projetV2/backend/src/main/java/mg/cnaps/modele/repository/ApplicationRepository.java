package mg.cnaps.modele.repository;
import mg.cnaps.modele.dto.Observation;
import mg.cnaps.modele.dto.ServiceSimple;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApplicationRepository {

    private final JdbcTemplate jdbcTemplate;

    public ApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ServiceSimple> SERVICE_MAPPER = (rs,
            rowNum) -> new ServiceSimple(
                    rs.getString("CODE_SERVICE"),
                    rs.getString("NOM_SERVICE"));

    private static final RowMapper<Observation> OBS_MAPPER = (rs,
            rowNum) -> new Observation(
                    rs.getString("ID_MODIF_OBS"),
                    rs.getString("CODE_SERVICE"),
                    rs.getString("SECTION"),
                    rs.getString("APPLICATION"),
                    rs.getString("OBSERVATION"),
                    rs.getTimestamp("DATE_OBSERVATION"),
                    rs.getString("SERVICE_PROPRIETE"),
                    rs.getString("FONCTIONNEMENT")

            );

    /**
     * Récupère le nom d'une application.
     */
    public String findNomApplication(String menuCode) {
        String sql = "SELECT TRIM(MENU_LIBELLE) FROM TRESO.CPT_MENU WHERE MENU_CODE = ?";
        List<String> resultats = jdbcTemplate.queryForList(sql, String.class, menuCode);
        return resultats.isEmpty() ? null : resultats.get(0);
    }

    /**
     * Liste les services qui utilisent une application donnée.
     */
    public List<ServiceSimple> findServicesByApplication(String menuCode) {
        String sql = "SELECT DISTINCT "
                + "       TRIM(rf.CODE_SERVICE) AS CODE_SERVICE, "
                + "       TRIM(s.LIBELLE_SERVICE) AS NOM_SERVICE "
                + "FROM TRESO.CPT_MENU m "
                + "JOIN TRESO.CPT_MENU_SECTION_USERS u "
                + "     ON u.SECTION = m.SECTION "
                + "JOIN TRESO.CNAPS_USER cu "
                + "     ON cu.USERNAME = u.SECTION_USER "
                + "JOIN DRH.DRH_RENS_FONCTION rf "
                + "     ON rf.AGENT_MATRICULE = cu.USER_MATRICULE "
                + "JOIN DRH.DRH_SERVICE s "
                + "     ON rf.CODE_SERVICE = s.CODE_SERVICE "
                + "WHERE m.MENU_CODE = ? "
                + "  AND rf.DATE_FIN_ESSAI IS NULL "
                + "ORDER BY NOM_SERVICE";

        return jdbcTemplate.query(sql, SERVICE_MAPPER, menuCode);
    }

    /**
     * Liste les observations d'une application.
     */
    public List<Observation> findObservationsByApplication(String menuCode) {
        String sql = "SELECT ID_MODIF_OBS, CODE_SERVICE, SECTION, APPLICATION, "
                + "       OBSERVATION, DATE_OBSERVATION, SERVICE_PROPRIETE, FONCTIONNEMENT "
                + "FROM IAM.MODIF_OBS_RV "
                + "WHERE APPLICATION = ? "
                + "ORDER BY DATE_OBSERVATION DESC";

        return jdbcTemplate.query(sql, OBS_MAPPER, menuCode);
    }

    public void insereObservation(String codeService, String section, String application,
            String observation, String servicePropriete, String fonctionnement) {
        String sql = "INSERT INTO IAM.MODIF_OBS_RV (CODE_SERVICE, SECTION, APPLICATION, OBSERVATION, DATE_OBSERVATION, SERVICE_PROPRIETE, FONCTIONNEMENT) "
                + "VALUES (?, ?, ?, ?, SYSTIMESTAMP, ?, ?)";

        String serviceProprieteValue = (servicePropriete == null || servicePropriete.trim().isEmpty())
                ? "N/A"
                : servicePropriete.trim();
        String fonctionnementValue = (fonctionnement == null || fonctionnement.trim().isEmpty())
                ? "N/A"
                : fonctionnement.trim();

        jdbcTemplate.update(sql, codeService, section, application, observation,
                serviceProprieteValue, fonctionnementValue);
    }

    public void supprimerObservation(String idModifObs) {
        String sql = "DELETE FROM IAM.MODIF_OBS_RV WHERE ID_MODIF_OBS = ?";
        jdbcTemplate.update(sql, idModifObs);
    }

    public void modifierObservation(String idModifObs, String observation) {
        String sql = "UPDATE IAM.MODIF_OBS_RV SET OBSERVATION = ?, DATE_OBSERVATION = SYSTIMESTAMP WHERE ID_MODIF_OBS = ?";
        jdbcTemplate.update(sql, observation, idModifObs);
    }

    

}