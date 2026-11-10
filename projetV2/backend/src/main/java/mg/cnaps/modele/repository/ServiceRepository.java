package mg.cnaps.modele.repository;

import mg.cnaps.modele.dto.ServiceDTO;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ServiceRepository {

    private final JdbcTemplate jdbcTemplate;

    public ServiceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ServiceDTO> MAPPER = (rs, rowNum) -> new ServiceDTO(
            rs.getString("CODE_SERVICE"),
            rs.getString("LIBELLE_SERVICE")
    );

    public List<ServiceDTO> findAll() {
        String sql = "SELECT CODE_SERVICE, LIBELLE_SERVICE "
                   + "FROM DRH.DRH_SERVICE "
                   + "WHERE CODE_ACTIF = '0' OR CODE_ACTIF IS NULL "
                   + "ORDER BY LIBELLE_SERVICE";
        return jdbcTemplate.query(sql, MAPPER);
    }
}