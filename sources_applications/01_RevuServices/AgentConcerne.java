package mg.cnaps.iam.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un agent CNaPS concerné par une demande de revue des habilitations.
 * Application : RevuServices (Revue des Habilitations CNaPS)
 * Schéma Oracle : IAM
 */
@Entity
@Table(name = "APP_DMD_AGENT_CONCERNE", schema = "IAM")
public class AgentConcerne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DMD_AGENT_CONCERNE", nullable = false)
    private Long idDmdAgentConcerne;

    @Column(name = "CODE_DMD", length = 100, nullable = false)
    private String codeDmd;

    @Column(name = "MATRICULE", length = 10, nullable = false)
    private String matricule;

    @Column(name = "DATE_CREATION")
    private LocalDateTime dateCreation;

    public AgentConcerne() {
    }

    public Long getIdDmdAgentConcerne() {
        return idDmdAgentConcerne;
    }

    public void setIdDmdAgentConcerne(Long idDmdAgentConcerne) {
        this.idDmdAgentConcerne = idDmdAgentConcerne;
    }

    public String getCodeDmd() {
        return codeDmd;
    }

    public void setCodeDmd(String codeDmd) {
        this.codeDmd = codeDmd;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
