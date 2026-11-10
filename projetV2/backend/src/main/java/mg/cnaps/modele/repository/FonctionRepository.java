package mg.cnaps.modele.repository;
import mg.cnaps.modele.dto.FonctionDTO;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import mg.cnaps.modele.dto.ApplicationSection;
import mg.cnaps.modele.dto.SectionApplicationDTO;

@Repository
public class FonctionRepository {

    private final JdbcTemplate jdbcTemplate;

    public FonctionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<FonctionDTO> FONCTION_MAPPER = (rs, rowNum) ->
            new FonctionDTO(
                    rs.getString("CODE_FONCTION"),
                    rs.getString("LIBELLE_FONCTION")
            );

    private static final RowMapper<SectionApplicationDTO> SECTION_APP_MAPPER = (rs, rowNum) ->
            new SectionApplicationDTO(
                    rs.getString("SECTION"),
                    rs.getString("NOM_SECTION"),
                    rs.getString("MENU_CODE"),
                    rs.getString("NOM_APPLICATION")
            );

    private static final RowMapper<ApplicationSection> APP_SECTION_MAPPER = (rs, rowNum) ->
            new ApplicationSection(
                    rs.getString("MENU_CODE"),
                    rs.getString("NOM_APPLICATION"),
                    rs.getString("SECTION"),
                    rs.getString("NOM_SECTION"),
                    rs.getString("ID_MODIF_OBS"),
                    rs.getString("OBSERVATION"),
                    rs.getTimestamp("DATE_OBSERVATION"),
                    rs.getString("SERVICE_PROPRIETE"),
                    rs.getString("FONCTIONNEMENT")
            );

    public List<FonctionDTO> findFonctionsByService(String codeService) {
        String sql = "SELECT DISTINCT "
                + "       TRIM(rf.CODE_FONCTION) AS CODE_FONCTION, "
                + "       TRIM(f.LIBELLE_FONCTION) AS LIBELLE_FONCTION "
                + "FROM DRH.DRH_RENS_FONCTION rf "
                + "JOIN DRH.DRH_FONCTION f ON rf.CODE_FONCTION = f.CODE_FONCTION "
                + "WHERE rf.CODE_SERVICE = ? "
                + "  AND rf.DATE_FIN_ESSAI IS NULL "
                + "ORDER BY LIBELLE_FONCTION";

        return jdbcTemplate.query(sql, FONCTION_MAPPER, codeService);
    }

    public List<SectionApplicationDTO> findApplicationsByServiceAndFonction(
            String codeService, String codeFonction) {
        String sql = "SELECT DISTINCT "
                + "       TRIM(sec.SECTION) AS SECTION, "
                + "       TRIM(sec.SECTION_LIB) AS NOM_SECTION, "
                + "       TRIM(m.MENU_CODE) AS MENU_CODE, "
                + "       TRIM(m.MENU_LIBELLE) AS NOM_APPLICATION "
                + "FROM DRH.DRH_RENS_FONCTION rf "
                + "JOIN DRH.DRH_FONCTION f ON rf.CODE_FONCTION = f.CODE_FONCTION "
                + "JOIN TRESO.CNAPS_USER cu ON cu.USER_MATRICULE = rf.AGENT_MATRICULE "
                + "JOIN TRESO.CPT_MENU_SECTION_USERS u ON u.SECTION_USER = cu.USERNAME "
                + "JOIN TRESO.CPT_MENU_SECTION sec ON u.SECTION = sec.SECTION "
                + "JOIN TRESO.CPT_MENU m ON sec.SECTION = m.SECTION "
                + "WHERE rf.CODE_SERVICE = ? "
                + "  AND rf.CODE_FONCTION = ? "
                + "  AND rf.DATE_FIN_ESSAI IS NULL "
                + "ORDER BY NOM_SECTION, NOM_APPLICATION";

        return jdbcTemplate.query(sql, SECTION_APP_MAPPER, codeService, codeFonction);
    }

    public List<ApplicationSection> findApplicationsAvecSections(
            String codeService, String codeFonction) {
        String sql = "SELECT DISTINCT "
                + "       TRIM(m.MENU_CODE) AS MENU_CODE, "
                + "       TRIM(m.MENU_LIBELLE) AS NOM_APPLICATION, "
                + "       TRIM(sec.SECTION) AS SECTION, "
                + "       TRIM(sec.SECTION_LIB) AS NOM_SECTION, "
                + "       o.ID_MODIF_OBS AS ID_MODIF_OBS, "
                + "       TRIM(o.OBSERVATION) AS OBSERVATION, "
                + "       o.DATE_OBSERVATION AS DATE_OBSERVATION, "
                + "       TRIM(o.SERVICE_PROPRIETE) AS SERVICE_PROPRIETE, "
                + "       TRIM(o.FONCTIONNEMENT) AS FONCTIONNEMENT "
                + "FROM DRH.DRH_RENS_FONCTION rf "
                + "JOIN DRH.DRH_FONCTION f ON rf.CODE_FONCTION = f.CODE_FONCTION "
                + "JOIN TRESO.CNAPS_USER cu ON cu.USER_MATRICULE = rf.AGENT_MATRICULE "
                + "JOIN TRESO.CPT_MENU_SECTION_USERS u ON u.SECTION_USER = cu.USERNAME "
                + "JOIN TRESO.CPT_MENU_SECTION sec ON u.SECTION = sec.SECTION "
                + "JOIN TRESO.CPT_MENU m ON sec.SECTION = m.SECTION "
                + "LEFT JOIN ( "
                + "     SELECT ranked_obs.* "
                + "     FROM ( "
                + "         SELECT obs.*, ROW_NUMBER() OVER ( "
                + "             PARTITION BY obs.CODE_SERVICE, obs.SECTION, obs.APPLICATION "
                + "             ORDER BY obs.DATE_OBSERVATION DESC NULLS LAST, obs.ID_MODIF_OBS DESC "
                + "         ) AS OBS_RANK "
                + "         FROM IAM.MODIF_OBS_RV obs "
                + "     ) ranked_obs "
                + "     WHERE ranked_obs.OBS_RANK = 1 "
                + ") o "
                + "     ON o.APPLICATION = m.MENU_CODE "
                + "    AND o.SECTION = sec.SECTION "
                + "    AND o.CODE_SERVICE = rf.CODE_SERVICE "
                + "WHERE rf.CODE_SERVICE = ? "
                + "  AND rf.CODE_FONCTION = ? "
                + "  AND rf.DATE_FIN_ESSAI IS NULL "
                + "ORDER BY NOM_APPLICATION, NOM_SECTION";

        return jdbcTemplate.query(sql, APP_SECTION_MAPPER, codeService, codeFonction);
    }
}
