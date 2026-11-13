package mg.cnaps.iam.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité d'historisation et d'audit des modifications de privilèges lors de la revue.
 * Application : RevuServices (Revue des Habilitations CNaPS)
 * Schéma Oracle : IAM
 */
@Entity
@Table(name = "APP_DMD_PRIVILEGE_HISTO", schema = "IAM")
public class PrivilegeHisto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HISTO", nullable = false)
    private Long idHisto;

    @Column(name = "ID_APP_DMD_PRIVILEGE", nullable = false)
    private Long idAppDmdPrivilege;

    @Column(name = "ID_DMD_AGENT_CONCERNE", nullable = false)
    private Long idDmdAgentConcerne;

    @Column(name = "AGENT_MATRICULE", length = 10, nullable = false)
    private String agentMatricule;

    @Column(name = "LIBELLE_FONCTION", length = 100)
    private String libelleFonction;

    @Column(name = "CODE_SERVICE", length = 8)
    private String codeService;

    @Column(name = "SECTION", length = 20)
    private String section;

    @Column(name = "APPLICATION", length = 50)
    private String application;

    @Column(name = "CATEGORIE", length = 50)
    private String categorie;

    @Column(name = "TYPE_CATEGORIE", length = 50)
    private String typeCategorie;

    @Column(name = "BESOIN_SERVICE", length = 20)
    private String besoinService;

    @Column(name = "MOTIF_SUPPRESSION", length = 500)
    private String motifSuppression;

    @Column(name = "DATE_ACTION")
    private LocalDateTime dateAction;

    @Column(name = "TYPE_ACTION", length = 20)
    private String typeAction;

    @Column(name = "ACTION_PAR", length = 100)
    private String actionPar;

    public PrivilegeHisto() {
    }

    public Long getIdHisto() {
        return idHisto;
    }

    public void setIdHisto(Long idHisto) {
        this.idHisto = idHisto;
    }

    public Long getIdAppDmdPrivilege() {
        return idAppDmdPrivilege;
    }

    public void setIdAppDmdPrivilege(Long idAppDmdPrivilege) {
        this.idAppDmdPrivilege = idAppDmdPrivilege;
    }

    public Long getIdDmdAgentConcerne() {
        return idDmdAgentConcerne;
    }

    public void setIdDmdAgentConcerne(Long idDmdAgentConcerne) {
        this.idDmdAgentConcerne = idDmdAgentConcerne;
    }

    public String getAgentMatricule() {
        return agentMatricule;
    }

    public void setAgentMatricule(String agentMatricule) {
        this.agentMatricule = agentMatricule;
    }

    public String getLibelleFonction() {
        return libelleFonction;
    }

    public void setLibelleFonction(String libelleFonction) {
        this.libelleFonction = libelleFonction;
    }

    public String getCodeService() {
        return codeService;
    }

    public void setCodeService(String codeService) {
        this.codeService = codeService;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getTypeCategorie() {
        return typeCategorie;
    }

    public void setTypeCategorie(String typeCategorie) {
        this.typeCategorie = typeCategorie;
    }

    public String getBesoinService() {
        return besoinService;
    }

    public void setBesoinService(String besoinService) {
        this.besoinService = besoinService;
    }

    public String getMotifSuppression() {
        return motifSuppression;
    }

    public void setMotifSuppression(String motifSuppression) {
        this.motifSuppression = motifSuppression;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    public String getTypeAction() {
        return typeAction;
    }

    public void setTypeAction(String typeAction) {
        this.typeAction = typeAction;
    }

    public String getActionPar() {
        return actionPar;
    }

    public void setActionPar(String actionPar) {
        this.actionPar = actionPar;
    }
}
