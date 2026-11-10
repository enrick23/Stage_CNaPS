package mg.cnaps.modele.repository;

import mg.cnaps.modele.dto.MenuDTO;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MenuRepository {

    private final JdbcTemplate jdbcTemplate;

    public MenuRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<MenuDTO> MAPPER = (rs, rowNum) -> new MenuDTO(
            rs.getString("MENU_CODE"),
            rs.getString("MENU_LIBELLE"),
            rs.getString("MENU_DESCRIPTION")
    );

    public List<MenuDTO> findAll() {
        String sql = "SELECT MENU_CODE, MENU_LIBELLE, MENU_DESCRIPTION "
                   + "FROM TRESO.CPT_MENU "
                   + "ORDER BY MENU_CODE";
        return jdbcTemplate.query(sql, MAPPER);
    }

    
    public MenuDTO findByCode(String code) {
        String sql = "SELECT MENU_CODE, MENU_LIBELLE, MENU_DESCRIPTION "
                   + "FROM TRESO.CPT_MENU "
                   + "WHERE MENU_CODE = ?";
        List<MenuDTO> resultats = jdbcTemplate.query(sql, MAPPER, code);
        return resultats.isEmpty() ? null : resultats.get(0);
    }


    public boolean existsByCode(String code) {
        String sql = "SELECT COUNT(*) FROM TRESO.CPT_MENU WHERE MENU_CODE = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code);
        return count != null && count > 0;
    }


    public int save(MenuDTO menu) {
        String sql = "INSERT INTO TRESO.CPT_MENU (MENU_CODE, MENU_LIBELLE, MENU_DESCRIPTION) "
                   + "VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql,
                menu.getCode(),
                menu.getLibelle(),
                menu.getDescription());
    }


    public int update(String code, MenuDTO menu) {
        String sql = "UPDATE TRESO.CPT_MENU "
                   + "SET MENU_LIBELLE = ?, MENU_DESCRIPTION = ? "
                   + "WHERE MENU_CODE = ?";
        return jdbcTemplate.update(sql,
                menu.getLibelle(),
                menu.getDescription(),
                code);
    }


    public int delete(String code) {
        String sql = "DELETE FROM TRESO.CPT_MENU WHERE MENU_CODE = ?";
        return jdbcTemplate.update(sql, code);
    }
}