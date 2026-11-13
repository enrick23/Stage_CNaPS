package mg.cnaps.incident.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Table des échanges et commentaires techniques sur un ticket d'incident.
 * Application : Gestion des Incidents (Helpdesk)
 * Schéma Oracle : SIG
 */
@Entity
@Table(name = "INCIDENT_COMMENTAIRE", schema = "SIG")
public class IncidentCommentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_COMMENTAIRE", nullable = false)
    private Long idCommentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_INCIDENT", nullable = false)
    private IncidentTicket incident;

    @Column(name = "MATRICULE_AUTEUR", length = 15, nullable = false)
    private String matriculeAuteur;

    @Column(name = "MESSAGE_TEXTE", length = 2000, nullable = false)
    private String messageTexte;

    @Column(name = "DATE_CREATION", nullable = false)
    private LocalDateTime dateCreation;

    public IncidentCommentaire() {
    }

    public Long getIdCommentaire() {
        return idCommentaire;
    }

    public void setIdCommentaire(Long idCommentaire) {
        this.idCommentaire = idCommentaire;
    }

    public IncidentTicket getIncident() {
        return incident;
    }

    public void setIncident(IncidentTicket incident) {
        this.incident = incident;
    }

    public String getMatriculeAuteur() {
        return matriculeAuteur;
    }

    public void setMatriculeAuteur(String matriculeAuteur) {
        this.matriculeAuteur = matriculeAuteur;
    }

    public String getMessageTexte() {
        return messageTexte;
    }

    public void setMessageTexte(String messageTexte) {
        this.messageTexte = messageTexte;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
