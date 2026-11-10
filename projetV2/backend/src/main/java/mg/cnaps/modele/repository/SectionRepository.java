package mg.cnaps.modele.repository;

import mg.cnaps.modele.dto.SectionApplicationProgrammeDTO;
import mg.cnaps.modele.dto.SectionDTO;

import mg.cnaps.modele.dto.SectionApplicationProgrammeDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository = uniquement les requêtes SQL.
 * Aucune logique métier.
 */
@Repository
public class SectionRepository {

    private final JdbcTemplate jdbcTemplate;

    public SectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ---------------------------------------------------------------
    // RowMappers
    // ---------------------------------------------------------------

    private static final RowMapper<SectionDTO> SECTION_MAPPER = (rs, rowNum) -> new SectionDTO(
            rs.getString("SECTION"),
            rs.getString("NOM_SECTION")
    );

    private static final RowMapper<SectionApplicationProgrammeDTO> SECTION_APP_MAPPER = (rs, rowNum) -> new SectionApplicationProgrammeDTO(
            rs.getString("SECTION"),
            rs.getString("NOM_SECTION"),
            rs.getString("MENU_CODE"),
            rs.getString("NOM_APPLICATION"),
            rs.getString("PROGRAMME")
    );

    // ---------------------------------------------------------------
    // Requêtes SQL
    // ---------------------------------------------------------------

    /**
     * Liste les sections d'un service (sans les applications).
     * Filtre : agents actifs du service.
     */
    public List<SectionDTO> findSectionsByService(String codeService) {
        String sql =
            "SELECT DISTINCT "
          + "       TRIM(sec.SECTION) AS SECTION, "
          + "       TRIM(sec.SECTION_LIB) AS NOM_SECTION "
          + "FROM DRH.DRH_RENS_FONCTION rf "
          + "JOIN TRESO.CNAPS_USER cu "
          + "     ON cu.USER_MATRICULE = rf.AGENT_MATRICULE "
          + "JOIN TRESO.CPT_MENU_SECTION_USERS u "
          + "     ON u.SECTION_USER = cu.USERNAME "
          + "JOIN TRESO.CPT_MENU_SECTION sec "
          + "     ON u.SECTION = sec.SECTION "
          + "WHERE rf.CODE_SERVICE = ? "
          + "  AND rf.DATE_FIN_ESSAI IS NULL "
          + "ORDER BY NOM_SECTION";

        return jdbcTemplate.query(sql, SECTION_MAPPER, codeService);
    }

    /**
     * Liste les sections d'un service AVEC leurs applications.
     * Chaque ligne = 1 (section, application).
     */
    public List<SectionApplicationProgrammeDTO> findSectionsAvecApplications(String codeService) {
        String sql =
            "SELECT DISTINCT "
          + "       TRIM(sec.SECTION) AS SECTION, "
          + "       TRIM(sec.SECTION_LIB) AS NOM_SECTION, "
          + "       TRIM(m.MENU_CODE) AS MENU_CODE, "
          + "       TRIM(m.MENU_LIBELLE) AS NOM_APPLICATION, "
          + "       TRIM(m.MENU_FMX) AS PROGRAMME "
          + "FROM DRH.DRH_RENS_FONCTION rf "
          + "JOIN TRESO.CNAPS_USER cu "
          + "     ON cu.USER_MATRICULE = rf.AGENT_MATRICULE "
          + "JOIN TRESO.CPT_MENU_SECTION_USERS u "
          + "     ON u.SECTION_USER = cu.USERNAME "
          + "JOIN TRESO.CPT_MENU_SECTION sec "
          + "     ON u.SECTION = sec.SECTION "
          + "JOIN TRESO.CPT_MENU m "
          + "     ON sec.SECTION = m.SECTION "
          + "WHERE rf.CODE_SERVICE = ? "
          + "  AND rf.DATE_FIN_ESSAI IS NULL "
          + "ORDER BY NOM_SECTION, NOM_APPLICATION";

        return jdbcTemplate.query(sql, SECTION_APP_MAPPER, codeService);
    }

    /**
     * Récupère une section par son code.
     */
    public SectionDTO findByCode(String section) {
        String sql =
            "SELECT TRIM(SECTION) AS SECTION, "
          + "       TRIM(SECTION_LIB) AS NOM_SECTION "
          + "FROM TRESO.CPT_MENU_SECTION "
          + "WHERE SECTION = ?";

        List<SectionDTO> resultats = jdbcTemplate.query(sql, SECTION_MAPPER, section);
        return resultats.isEmpty() ? null : resultats.get(0);
    }

}