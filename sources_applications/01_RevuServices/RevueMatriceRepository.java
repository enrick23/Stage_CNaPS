package mg.cnaps.modele.repository;

import mg.cnaps.modele.dto.RevueMatriceDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RevueMatriceRepository {

    private final JdbcTemplate jdbcTemplate;

    public RevueMatriceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<RevueMatriceDTO> MAPPER = (rs, rowNum) -> {
        RevueMatriceDTO dto = new RevueMatriceDTO();
        dto.setIdRevueMatrice(rs.getString("ID_REVUE_MATRICE"));
        dto.setCodeDirection(rs.getString("CODE_DIRECTION"));
        dto.setCodeService(rs.getString("CODE_SERVICE"));
        dto.setCodeFonction(rs.getString("CODE_FONCTION"));
        dto.setSection(rs.getString("SECTION"));
        dto.setNomSection(rs.getString("NOM_SECTION"));
        dto.setApplication(rs.getString("APPLICATION"));
        dto.setNomApplication(rs.getString("NOM_APPLICATION"));

        dto.setServiceCible(rs.getString("SERVICE_CIBLE"));
        dto.setFonctionCible(rs.getString("FONCTION_CIBLE"));
        dto.setFonctionnement(rs.getString("FONCTIONNEMENT"));

        dto.setDecisionRevue(rs.getString("DECISION_REVUE"));
        dto.setNiveauAcces(rs.getString("NIVEAU_ACCES"));
        dto.setActionMouvement(rs.getString("ACTION_MOUVEMENT"));
        dto.setRegleHabilitation(rs.getString("REGLE_HABILITATION"));

        dto.setControleAuto(rs.getString("CONTROLE_AUTO"));
        dto.setConformite(rs.getString("CONFORMITE"));

        dto.setValidationAudit(rs.getString("VALIDATION_AUDIT"));
        dto.setAuditeurInfo(rs.getString("AUDITEUR_INFO"));
        dto.setDateValidationAudit(rs.getTimestamp("DATE_VALIDATION_AUDIT"));
        dto.setAvisAudit(rs.getString("AVIS_AUDIT"));

        dto.setAutorisationExecution(rs.getString("AUTORISATION_EXECUTION"));
        dto.setDateExecution(rs.getTimestamp("DATE_EXECUTION"));
        dto.setExecutePar(rs.getString("EXECUTE_PAR"));
        dto.setStatutExecution(rs.getString("STATUT_EXECUTION"));
        dto.setObservation(rs.getString("OBSERVATION"));
        dto.setDateModification(rs.getTimestamp("DATE_MODIFICATION"));
        return dto;
    };

    private static final RowMapper<RevueMatriceDTO> OBSERVATION_MAPPER = (rs, rowNum) -> {
        RevueMatriceDTO dto = new RevueMatriceDTO();
        dto.setServiceCible(rs.getString("SERVICE_PROPRIETE"));
        dto.setFonctionnement(rs.getString("FONCTIONNEMENT"));
        dto.setObservation(rs.getString("OBSERVATION"));
        return dto;
    };

    public List<RevueMatriceDTO> findMatriceByServiceAndFonction(String codeService, String codeFonction) {
        String sql = "SELECT DISTINCT "
                + "       rf.CODE_SERVICE AS CODE_SERVICE, "
                + "       rf.CODE_FONCTION AS CODE_FONCTION, "
                + "       TRIM(sec.SECTION) AS SECTION, "
                + "       TRIM(sec.SECTION_LIB) AS NOM_SECTION, "
                + "       TRIM(m.MENU_CODE) AS APPLICATION, "
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
                + "ORDER BY NOM_APPLICATION, NOM_SECTION";

        List<RevueMatriceDTO> lignes = jdbcTemplate.query(sql, (rs, rowNum) -> {
            RevueMatriceDTO dto = new RevueMatriceDTO();
            dto.setCodeService(rs.getString("CODE_SERVICE"));
            dto.setCodeFonction(rs.getString("CODE_FONCTION"));
            dto.setSection(rs.getString("SECTION"));
            dto.setNomSection(rs.getString("NOM_SECTION"));
            dto.setApplication(rs.getString("APPLICATION"));
            dto.setNomApplication(rs.getString("NOM_APPLICATION"));
            dto.setCodeDirection("DPS");
            dto.setValidationAudit("En attente");
            dto.setControleAuto("À COMPLÉTER");
            dto.setConformite("À VÉRIFIER");
            dto.setStatutExecution("BLOQUÉE");
            return dto;
        }, codeService, codeFonction);

        for (RevueMatriceDTO dto : lignes) {
            RevueMatriceDTO observation = findLatestObservation(dto.getCodeService(), dto.getSection(), dto.getApplication());
            if (observation != null) {
                dto.setServiceCible(observation.getServiceCible());
                dto.setFonctionnement(observation.getFonctionnement());
                dto.setObservation(observation.getObservation());
            }

            RevueMatriceDTO revue = findByServiceAndFonctionAndApplication(dto.getCodeService(), dto.getCodeFonction(), dto.getSection(), dto.getApplication());
            if (revue != null) {
                dto.setIdRevueMatrice(revue.getIdRevueMatrice());
                dto.setCodeDirection(revue.getCodeDirection() != null ? revue.getCodeDirection() : dto.getCodeDirection());
                dto.setServiceCible(revue.getServiceCible() != null ? revue.getServiceCible() : dto.getServiceCible());
                dto.setFonctionCible(revue.getFonctionCible());
                dto.setFonctionnement(revue.getFonctionnement() != null ? revue.getFonctionnement() : dto.getFonctionnement());
                dto.setDecisionRevue(revue.getDecisionRevue());
                dto.setNiveauAcces(revue.getNiveauAcces());
                dto.setActionMouvement(revue.getActionMouvement());
                dto.setRegleHabilitation(revue.getRegleHabilitation());
                dto.setControleAuto(revue.getControleAuto() != null ? revue.getControleAuto() : dto.getControleAuto());
                dto.setConformite(revue.getConformite() != null ? revue.getConformite() : dto.getConformite());
                dto.setValidationAudit(revue.getValidationAudit() != null ? revue.getValidationAudit() : dto.getValidationAudit());
                dto.setAuditeurInfo(revue.getAuditeurInfo());
                dto.setDateValidationAudit(revue.getDateValidationAudit());
                dto.setAvisAudit(revue.getAvisAudit());
                dto.setAutorisationExecution(revue.getAutorisationExecution());
                dto.setDateExecution(revue.getDateExecution());
                dto.setExecutePar(revue.getExecutePar());
                dto.setStatutExecution(revue.getStatutExecution() != null ? revue.getStatutExecution() : dto.getStatutExecution());
                dto.setObservation(revue.getObservation() != null ? revue.getObservation() : dto.getObservation());
                dto.setDateModification(revue.getDateModification());
            }
        }

        return lignes;
    }

    private RevueMatriceDTO findLatestObservation(String codeService, String section, String application) {
        String sql = "SELECT * FROM ( "
                + "   SELECT * FROM IAM.MODIF_OBS_RV "
                + "   WHERE CODE_SERVICE = ? AND SECTION = ? AND APPLICATION = ? "
                + "   ORDER BY DATE_OBSERVATION DESC, ID_MODIF_OBS DESC "
                + ") WHERE ROWNUM = 1";

        List<RevueMatriceDTO> rows = jdbcTemplate.query(sql, OBSERVATION_MAPPER, codeService, section, application);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private RevueMatriceDTO findByServiceAndFonctionAndApplication(String codeService, String codeFonction, String section, String application) {
        String sql = "SELECT * FROM IAM.REVUE_MATRICE "
                + "WHERE CODE_SERVICE = ? AND CODE_FONCTION = ? AND SECTION = ? AND APPLICATION = ?";

        List<RevueMatriceDTO> rows = jdbcTemplate.query(sql, MAPPER, codeService, codeFonction, section, application);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public RevueMatriceDTO findById(String idRevueMatrice) {
        String sql = "SELECT rm.*, "
                + "       sec.SECTION_LIB AS NOM_SECTION, "
                + "       m.MENU_LIBELLE AS NOM_APPLICATION "
                + "FROM IAM.REVUE_MATRICE rm "
                + "LEFT JOIN TRESO.CPT_MENU_SECTION sec ON sec.SECTION = rm.SECTION "
                + "LEFT JOIN TRESO.CPT_MENU m ON m.MENU_CODE = rm.APPLICATION AND m.SECTION = rm.SECTION "
                + "WHERE rm.ID_REVUE_MATRICE = ?";
        List<RevueMatriceDTO> list = jdbcTemplate.query(sql, MAPPER, idRevueMatrice);
        return list.isEmpty() ? null : list.get(0);
    }

    public String findExistingId(String codeService, String codeFonction, String section, String application) {
        String sql = "SELECT ID_REVUE_MATRICE FROM IAM.REVUE_MATRICE "
                + "WHERE CODE_SERVICE = ? AND CODE_FONCTION = ? AND SECTION = ? AND APPLICATION = ?";
        List<String> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("ID_REVUE_MATRICE"),
                codeService, codeFonction, section, application);
        return results.isEmpty() ? null : results.get(0);
    }

    public int insert(RevueMatriceDTO dto) {
        String sql = "INSERT INTO IAM.REVUE_MATRICE ( "
                + "  CODE_DIRECTION, CODE_SERVICE, CODE_FONCTION, SECTION, APPLICATION, "
                + "  SERVICE_CIBLE, FONCTION_CIBLE, FONCTIONNEMENT, "
                + "  DECISION_REVUE, NIVEAU_ACCES, ACTION_MOUVEMENT, REGLE_HABILITATION, "
                + "  CONTROLE_AUTO, CONFORMITE, VALIDATION_AUDIT, AUDITEUR_INFO, "
                + "  DATE_VALIDATION_AUDIT, AVIS_AUDIT, AUTORISATION_EXECUTION, DATE_EXECUTION, "
                + "  EXECUTE_PAR, STATUT_EXECUTION, OBSERVATION "
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.update(sql,
                dto.getCodeDirection(),
                dto.getCodeService(),
                dto.getCodeFonction(),
                dto.getSection(),
                dto.getApplication(),
                dto.getServiceCible(),
                dto.getFonctionCible(),
                dto.getFonctionnement(),
                dto.getDecisionRevue(),
                dto.getNiveauAcces(),
                dto.getActionMouvement(),
                dto.getRegleHabilitation(),
                dto.getControleAuto(),
                dto.getConformite(),
                dto.getValidationAudit(),
                dto.getAuditeurInfo(),
                dto.getDateValidationAudit(),
                dto.getAvisAudit(),
                dto.getAutorisationExecution(),
                dto.getDateExecution(),
                dto.getExecutePar(),
                dto.getStatutExecution(),
                dto.getObservation()
        );
    }

    public int update(RevueMatriceDTO dto) {
        String sql = "UPDATE IAM.REVUE_MATRICE SET "
                + "  SERVICE_CIBLE = ?, "
                + "  FONCTION_CIBLE = ?, "
                + "  FONCTIONNEMENT = ?, "
                + "  DECISION_REVUE = ?, "
                + "  NIVEAU_ACCES = ?, "
                + "  ACTION_MOUVEMENT = ?, "
                + "  REGLE_HABILITATION = ?, "
                + "  CONTROLE_AUTO = ?, "
                + "  CONFORMITE = ?, "
                + "  VALIDATION_AUDIT = ?, "
                + "  AUDITEUR_INFO = ?, "
                + "  DATE_VALIDATION_AUDIT = ?, "
                + "  AVIS_AUDIT = ?, "
                + "  AUTORISATION_EXECUTION = ?, "
                + "  DATE_EXECUTION = ?, "
                + "  EXECUTE_PAR = ?, "
                + "  STATUT_EXECUTION = ?, "
                + "  OBSERVATION = ? "
                + "WHERE ID_REVUE_MATRICE = ?";

        return jdbcTemplate.update(sql,
                dto.getServiceCible(),
                dto.getFonctionCible(),
                dto.getFonctionnement(),
                dto.getDecisionRevue(),
                dto.getNiveauAcces(),
                dto.getActionMouvement(),
                dto.getRegleHabilitation(),
                dto.getControleAuto(),
                dto.getConformite(),
                dto.getValidationAudit(),
                dto.getAuditeurInfo(),
                dto.getDateValidationAudit(),
                dto.getAvisAudit(),
                dto.getAutorisationExecution(),
                dto.getDateExecution(),
                dto.getExecutePar(),
                dto.getStatutExecution(),
                dto.getObservation(),
                dto.getIdRevueMatrice()
        );
    }

    public int updateValidationAudit(String idRevueMatrice, String validationAudit, String avisAudit, String auditeurInfo, String statutExecution) {
        String sql = "UPDATE IAM.REVUE_MATRICE SET "
                + "  VALIDATION_AUDIT = ?, "
                + "  AVIS_AUDIT = ?, "
                + "  AUDITEUR_INFO = ?, "
                + "  DATE_VALIDATION_AUDIT = SYSDATE, "
                + "  STATUT_EXECUTION = ? "
                + "WHERE ID_REVUE_MATRICE = ?";

        return jdbcTemplate.update(sql, validationAudit, avisAudit, auditeurInfo, statutExecution, idRevueMatrice);
    }
}

