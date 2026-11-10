package mg.cnaps.modele.repository;
import mg.cnaps.modele.dto.DirectionDTO;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import mg.cnaps.modele.dto.ServiceDTO;

@Repository
public class DirectionRepository {
    private final JdbcTemplate jdbcTemplate;

    public DirectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<DirectionDTO> DIRECTION_MAPPER = (rs, rowNum) -> {
        String flagActif = rs.getString("FLAG_ACTIF");
        return new DirectionDTO(
                rs.getString("CODE_DIRECTION"),
                rs.getString("LIBELLE_DIRECTION"),
                rs.getString("CODE_ABREV"),
                flagActif == null || flagActif.isEmpty() ? null : flagActif.charAt(0)
        );
    };

    public List<DirectionDTO> findAll() {
        String sql = "SELECT CODE_DIRECTION, LIBELLE_DIRECTION, CODE_ABREV, FLAG_ACTIF "
                + "FROM DRH.DRH_DIRECTION "
                + "ORDER BY LIBELLE_DIRECTION";
        return jdbcTemplate.query(sql, DIRECTION_MAPPER);
    }

    public List<ServiceDTO> findServicesByDirection(String codeDirection) {
        String sql = "SELECT DISTINCT "
                   + "       TRIM(rf.CODE_SERVICE) AS CODE_SERVICE, "
                   + "       TRIM(s.LIBELLE_SERVICE) AS LIBELLE_SERVICE "
                   + "FROM DRH.DRH_RENS_FONCTION rf "
                   + "JOIN DRH.DRH_SERVICE s ON rf.CODE_SERVICE = s.CODE_SERVICE "
                   + "WHERE rf.CODE_DIRECTION = ? "
                   + "  AND rf.DATE_FIN_ESSAI IS NULL "
                   + "ORDER BY LIBELLE_SERVICE";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ServiceDTO(
                rs.getString("CODE_SERVICE"),
                rs.getString("LIBELLE_SERVICE")
        ), codeDirection);
    }
}
