package mg.cnaps.iam.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité détaillant un privilège d'accès attribué à un agent lors d'une campagne de revue.
 * Application : RevuServices (Revue des Habilitations CNaPS)
 * Schéma Oracle : IAM
 */
@Entity
@Table(name = "APP_DMD_PRIVILEGE_DETAIL", schema = "IAM")
public class PrivilegeDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_APP_DMD_PRIVILEGE", nullable = false)
    private Long idAppDmdPrivilege;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DMD_AGENT_CONCERNE", nullable = false)
    private AgentConcerne agentConcerne;

    @Column(name = "AGENT_MATRICULE", length = 10, nullable = false)
    private String agentMatricule;

    @Column(name = "LIBELLE_FONCTION", length = 100)
    private String libelleFonction;

    @Column(name = "CODE_SERVICE", length = 8)
    private String codeService;

    @Column(name = "SECTION", length = 20, nullable = false)
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

    @Column(name = "ID_LIGNE_ORIGINE")
    private Long idLigneOrigine;

    @Column(name = "DATE_CORRECTION")
    private LocalDateTime dateCorrection;

    @Column(name = "CORRIGE_PAR", length = 100)
    private String corrigePar;

    public PrivilegeDetail() {
    }

    public Long getIdAppDmdPrivilege() {
        return idAppDmdPrivilege;
    }

    public void setIdAppDmdPrivilege(Long idAppDmdPrivilege) {
        this.idAppDmdPrivilege = idAppDmdPrivilege;
    }

    public AgentConcerne getAgentConcerne() {
        return agentConcerne;
    }

    public void setAgentConcerne(AgentConcerne agentConcerne) {
        this.agentConcerne = agentConcerne;
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

    public Long getIdLigneOrigine() {
        return idLigneOrigine;
    }

    public void setIdLigneOrigine(Long idLigneOrigine) {
        this.idLigneOrigine = idLigneOrigine;
    }

    public LocalDateTime getDateCorrection() {
        return dateCorrection;
    }

    public void setDateCorrection(LocalDateTime dateCorrection) {
        this.dateCorrection = dateCorrection;
    }

    public String getCorrigePar() {
        return corrigePar;
    }

    public void setCorrigePar(String corrigePar) {
        this.corrigePar = corrigePar;
    }
}
