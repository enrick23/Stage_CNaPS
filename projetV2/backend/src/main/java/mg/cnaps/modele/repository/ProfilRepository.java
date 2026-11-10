package mg.cnaps.modele.repository;

import mg.cnaps.modele.dto.ProfilDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProfilRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProfilRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ProfilDTO> MAPPER = (rs, rowNum) -> {
        ProfilDTO dto = new ProfilDTO();
        dto.setMatricule(rs.getString("AGENT_MATRICULE"));
        dto.setCodeService(rs.getString("CODE_SERVICE"));
        dto.setCodeFonction(rs.getString("CODE_FONCTION"));
        dto.setLibelleFonction(rs.getString("LIBELLE_FONCTION"));
        return dto;
    };

    public ProfilDTO getAgentByMatricule(String matricule) {
        String sql = "SELECT rf.AGENT_MATRICULE, rf.CODE_SERVICE, rf.CODE_FONCTION, f.LIBELLE_FONCTION "
                   + "FROM DRH.DRH_RENS_FONCTION rf "
                   + "JOIN DRH.DRH_FONCTION f ON rf.CODE_FONCTION = f.CODE_FONCTION "
                   + "WHERE rf.AGENT_MATRICULE = ? "
                   + "  AND rf.DATE_FIN_ESSAI IS NULL";

        List<ProfilDTO> resultats = jdbcTemplate.query(sql, MAPPER, matricule);
        return resultats.isEmpty() ? null : resultats.get(0);
    }

    public boolean estUnCodePrivilegie(String codeFonction) {
        String sql = "SELECT COUNT(*) FROM IAM.PARAM_PRIVILEGE WHERE CODE_FONCTION = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, codeFonction);
        return count != null && count > 0;
    }

    public boolean estChefDeService(String matricule) {
    String sql = "SELECT COUNT(*) FROM DRH.DRH_RENS_FONCTION rf "
               + "JOIN IAM.PARAM_PRIVILEGE p ON rf.CODE_FONCTION = p.CODE_FONCTION "
               + "WHERE rf.AGENT_MATRICULE = ? "
               + "  AND rf.DATE_FIN_ESSAI IS NULL";
               
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, matricule);
    return count != null && count > 0;
}
}