package mg.cnaps.modele.repository;

import mg.cnaps.modele.dto.BesoinServiceDTO;
import mg.cnaps.modele.dto.RevuDTO;
import mg.cnaps.modele.dto.TypeActionDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository 
public class RevuRepository {
    private final JdbcTemplate jdbcTemplate;

    public RevuRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<RevuDTO> MAPPER = (rs, rowNum) -> new RevuDTO(
            rs.getString("ID_ACC_DETAIL"),
            rs.getString("AGENT_MATRICULE"),
            rs.getString("CODE_FONCTION"),
            rs.getString("CODE_SERVICE"),
            rs.getString("SECTION"),
            rs.getString("APPLICATION"),
            rs.getString("CATEGORIE"),
            rs.getString("TYPE_CATEGORIE"),
                getNullableInteger(rs, "ID_BESOIN_SERVICE"),
            rs.getString("MOTIF_SUPPRESSION"),
            rs.getTimestamp("DATE_ACTION")
    );

            private static final RowMapper<BesoinServiceDTO> BESOIN_SERVICE_MAPPER = (rs, rowNum) ->
                new BesoinServiceDTO(rs.getInt("ID_BESOIN_SERVICE"), rs.getString("LIBELLE"));

                private static final RowMapper<TypeActionDTO> TYPE_ACTION_MAPPER = (rs, rowNum) ->
                    new TypeActionDTO(rs.getInt("ID_TYPE_ACTION"), rs.getString("LIBELLE"));

            private static Integer getNullableInteger(ResultSet rs, String columnName) throws SQLException {
            int value = rs.getInt(columnName);
            return rs.wasNull() ? null : value;
            }

    public RevuDTO findByIdAccDetail(String idAccDetail) {
        String sql = "SELECT * FROM IAM.RV_ACC WHERE ID_ACC_DETAIL = ?";
        return jdbcTemplate.queryForObject(sql, MAPPER, idAccDetail);
    }

    public List<BesoinServiceDTO> findAllBesoinsService() {
        String sql = "SELECT ID_BESOIN_SERVICE, LIBELLE FROM IAM.BESOIN_SERVICE ORDER BY ID_BESOIN_SERVICE";
        return jdbcTemplate.query(sql, BESOIN_SERVICE_MAPPER);
    }

    public List<TypeActionDTO> findAllTypesAction() {
        String sql = "SELECT ID_TYPE_ACTION, LIBELLE FROM IAM.TYPE_ACTION ORDER BY ID_TYPE_ACTION";
        return jdbcTemplate.query(sql, TYPE_ACTION_MAPPER);
    }

    public int insertRevuAcc(String agentMatricule, String codeFonction, String codeService, String section, String application, String categorie, String typeCategorie, Integer idBesoinService, String motifSuppression) {
        String sql = "INSERT INTO IAM.RV_ACC (AGENT_MATRICULE, CODE_FONCTION, CODE_SERVICE, SECTION, APPLICATION, CATEGORIE, TYPE_CATEGORIE, ID_BESOIN_SERVICE, MOTIF_SUPPRESSION) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                agentMatricule,
                codeFonction,
                codeService,
                section,
                application,
                categorie,
                typeCategorie,
                idBesoinService,
                motifSuppression);
    }

}
