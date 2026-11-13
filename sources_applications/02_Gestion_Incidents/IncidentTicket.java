package mg.cnaps.incident.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Table transactionnelle des tickets d'incidents déclarés par les agents CNaPS.
 * Application : Gestion des Incidents (Helpdesk)
 * Schéma Oracle : SIG
 */
@Entity
@Table(name = "INCIDENT_TICKET", schema = "SIG")
public class IncidentTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_incident")
    @SequenceGenerator(name = "seq_incident", sequenceName = "SIG.SEQ_INCIDENT", allocationSize = 1)
    @Column(name = "ID_INCIDENT", nullable = false)
    private Long idIncident;

    @Column(name = "CODE_CATEGORIE", length = 20, nullable = false)
    private String codeCategorie;

    @Column(name = "MATRICULE_DECLARANT", length = 15, nullable = false)
    private String matriculeDeclarant;

    @Column(name = "TITRE_INCIDENT", length = 150, nullable = false)
    private String titreIncident;

    @Column(name = "DESCRIPTION_DETAIL", length = 1000)
    private String descriptionDetail;

    @Column(name = "PRIORITE", length = 10, nullable = false)
    private String priorite; // BASSE, MOYENNE, HAUTE, URGENTE

    @Column(name = "STATUT_INCIDENT", length = 20, nullable = false)
    private String statutIncident; // OUVERT, EN_COURS, RESOLU, FERME

    @Column(name = "DATE_DECLARATION", nullable = false)
    private LocalDateTime dateDeclaration;

    @Column(name = "DATE_CLOTURE")
    private LocalDateTime dateCloture;

    public IncidentTicket() {
    }

    public Long getIdIncident() {
        return idIncident;
    }

    public void setIdIncident(Long idIncident) {
        this.idIncident = idIncident;
    }

    public String getCodeCategorie() {
        return codeCategorie;
    }

    public void setCodeCategorie(String codeCategorie) {
        this.codeCategorie = codeCategorie;
    }

    public String getMatriculeDeclarant() {
        return matriculeDeclarant;
    }

    public void setMatriculeDeclarant(String matriculeDeclarant) {
        this.matriculeDeclarant = matriculeDeclarant;
    }

    public String getTitreIncident() {
        return titreIncident;
    }

    public void setTitreIncident(String titreIncident) {
        this.titreIncident = titreIncident;
    }

    public String getDescriptionDetail() {
        return descriptionDetail;
    }

    public void setDescriptionDetail(String descriptionDetail) {
        this.descriptionDetail = descriptionDetail;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getStatutIncident() {
        return statutIncident;
    }

    public void setStatutIncident(String statutIncident) {
        this.statutIncident = statutIncident;
    }

    public LocalDateTime getDateDeclaration() {
        return dateDeclaration;
    }

    public void setDateDeclaration(LocalDateTime dateDeclaration) {
        this.dateDeclaration = dateDeclaration;
    }

    public LocalDateTime getDateCloture() {
        return dateCloture;
    }

    public void setDateCloture(LocalDateTime dateCloture) {
        this.dateCloture = dateCloture;
    }
}
