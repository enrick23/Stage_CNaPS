package mg.cnaps.modele.repository;

import mg.cnaps.modele.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FiltreRepository {

    private final JdbcTemplate jdbcTemplate;

    public FiltreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

        public List<DirectionDTO> findAllDirections() {
        String sql = "SELECT CODE_DIRECTION, LIBELLE_DIRECTION, CODE_ABREV, FLAG_ACTIF "
                   + "FROM DRH.DRH_DIRECTION ORDER BY LIBELLE_DIRECTION";
                   
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DirectionDTO(
                rs.getString("CODE_DIRECTION"),
                rs.getString("LIBELLE_DIRECTION"),
                rs.getString("CODE_ABREV"),
                rs.getString("FLAG_ACTIF") != null ? rs.getString("FLAG_ACTIF").charAt(0) : null
        ));
    }

    public List<ServiceDTO> findAllServices() {
        return jdbcTemplate.query("SELECT CODE_SERVICE, LIBELLE_SERVICE FROM DRH.DRH_SERVICE ORDER BY LIBELLE_SERVICE",
                (rs, rowNum) -> new ServiceDTO(rs.getString("CODE_SERVICE"), rs.getString("LIBELLE_SERVICE")));
    }

    public List<FonctionDTO> findAllFonctions() {
        return jdbcTemplate.query("SELECT CODE_FONCTION, LIBELLE_FONCTION FROM DRH.DRH_FONCTION ORDER BY LIBELLE_FONCTION",
                (rs, rowNum) -> new FonctionDTO(rs.getString("CODE_FONCTION"), rs.getString("LIBELLE_FONCTION")));
    }

    public List<AgentDetailDTO> findDonneesBrutes(String codeDirection, String codeService, String codeFonction) {
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT " +
            "TRIM(rf.AGENT_MATRICULE) AS AGENT_MATRICULE, " +
            "TRIM(a.NOM) AS NOM, " +
            "TRIM(a.PRENOMS) AS PRENOMS, " +
            "TRIM(f.LIBELLE_FONCTION) AS LIBELLE_FONCTION, " +
            "TRIM(rf.CODE_FONCTION) AS CODE_FONCTION, " +
            "TRIM(sec.SECTION) AS SECTION, " +
            "TRIM(sec.SECTION_LIB) AS NOM_SECTION, " +
            "TRIM(m.MENU_CODE) AS MENU_CODE, " +
            "TRIM(m.MENU_LIBELLE) AS NOM_APPLICATION, " +
            "TRIM(m.MENU_FMX) AS PROGRAMME " +
            "FROM DRH.DRH_RENS_FONCTION rf " +
            "JOIN DRH.DRH_RENSEIGNEMENT_AGENT a ON rf.AGENT_MATRICULE = a.AGENT_MATRICULE " +
            "JOIN DRH.DRH_FONCTION f ON rf.CODE_FONCTION = f.CODE_FONCTION " +
            "JOIN TRESO.CNAPS_USER cu ON cu.USER_MATRICULE = rf.AGENT_MATRICULE " +
            "JOIN TRESO.CPT_MENU_SECTION_USERS u ON u.SECTION_USER = cu.USERNAME " +
            "JOIN TRESO.CPT_MENU_SECTION sec ON u.SECTION = sec.SECTION " +
            "JOIN TRESO.CPT_MENU m ON sec.SECTION = m.SECTION " +
            "WHERE rf.DATE_FIN_ESSAI IS NULL "
        );

        List<Object> params = new ArrayList<>();

        if (codeDirection != null && !codeDirection.trim().isEmpty()) {
            sql.append(" AND rf.CODE_DIRECTION = ? ");
            params.add(codeDirection);
        }
        if (codeService != null && !codeService.trim().isEmpty()) {
            sql.append(" AND rf.CODE_SERVICE = ? ");
            params.add(codeService);
        }
        if (codeFonction != null && !codeFonction.trim().isEmpty()) {
            sql.append(" AND rf.CODE_FONCTION = ? ");
            params.add(codeFonction);
        }

        sql.append(" ORDER BY LIBELLE_FONCTION, NOM, PRENOMS, NOM_SECTION, NOM_APPLICATION");

                return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new AgentDetailDTO(
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
        ), params.toArray());
    }
}