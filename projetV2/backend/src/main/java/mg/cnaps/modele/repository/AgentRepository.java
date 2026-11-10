package mg.cnaps.modele.repository;
import mg.cnaps.modele.dto.AgentDetailDTO;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AgentRepository {

    private final JdbcTemplate jdbcTemplate;

    public AgentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<AgentDetailDTO> DETAIL_MAPPER = (rs, rowNum) -> new AgentDetailDTO(
            rs.getString("SECTION"),
            rs.getString("NOM_SECTION"),
            rs.getString("AGENT_MATRICULE"),
            rs.getString("NOM"),
            rs.getString("PRENOMS"),
            rs.getString("LIBELLE_FONCTION"),
            rs.getString("CODE_FONCTION"),
            rs.getString("MENU_CODE"),
            rs.getString("NOM_APPLICATION"),
            rs.getString("PROGRAMME")
    );

    /**
     * Détails des agents d'un service (avec sections + applications).
     */
    public List<AgentDetailDTO> findDetailsByService(String codeService) {
        String sql =
            "SELECT DISTINCT "
          + "       TRIM(sec.SECTION) AS SECTION, "
          + "       TRIM(sec.SECTION_LIB) AS NOM_SECTION, "
          + "       TRIM(rf.AGENT_MATRICULE) AS AGENT_MATRICULE, "
          + "       TRIM(a.NOM) AS NOM, "
          + "       TRIM(a.PRENOMS) AS PRENOMS, "
          + "       TRIM(f.LIBELLE_FONCTION) AS LIBELLE_FONCTION, "
          + "       TRIM(rf.CODE_FONCTION) AS CODE_FONCTION, "
          + "       TRIM(m.MENU_CODE) AS MENU_CODE, "
          + "       TRIM(m.MENU_LIBELLE) AS NOM_APPLICATION, "
          + "       TRIM(m.MENU_FMX) AS PROGRAMME "
          + "FROM DRH.DRH_RENS_FONCTION rf "
          + "JOIN DRH.DRH_RENSEIGNEMENT_AGENT a ON rf.AGENT_MATRICULE = a.AGENT_MATRICULE "
          + "JOIN DRH.DRH_FONCTION f ON rf.CODE_FONCTION = f.CODE_FONCTION "
          + "JOIN TRESO.CNAPS_USER cu ON cu.USER_MATRICULE = rf.AGENT_MATRICULE "
          + "JOIN TRESO.CPT_MENU_SECTION_USERS u ON u.SECTION_USER = cu.USERNAME "
          + "JOIN TRESO.CPT_MENU_SECTION sec ON u.SECTION = sec.SECTION "
          + "JOIN TRESO.CPT_MENU m ON sec.SECTION = m.SECTION "
          + "WHERE rf.CODE_SERVICE = ? "
          + "  AND rf.DATE_FIN_ESSAI IS NULL "
          + "ORDER BY NOM_SECTION, NOM, PRENOMS, NOM_APPLICATION";

        return jdbcTemplate.query(sql, DETAIL_MAPPER, codeService);
    }
}