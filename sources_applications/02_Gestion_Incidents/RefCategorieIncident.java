package mg.cnaps.incident.entity;

import javax.persistence.*;

/**
 * Table référentielle des catégories d'incidents informatiques et matériels CNaPS.
 * Application : Gestion des Incidents (Helpdesk)
 * Schéma Oracle : SIG
 */
@Entity
@Table(name = "REF_CATEGORIE_INCIDENT", schema = "SIG")
public class RefCategorieIncident {

    @Id
    @Column(name = "CODE_CATEGORIE", length = 20, nullable = false)
    private String codeCategorie;

    @Column(name = "LIBELLE_CATEGORIE", length = 100, nullable = false)
    private String libelleCategorie;

    @Column(name = "DELAI_RESOLUTION_HEURES")
    private Integer delaiResolutionHeures;

    @Column(name = "FLAG_ACTIF", length = 1, nullable = false)
    private String flagActif;

    public RefCategorieIncident() {
    }

    public String getCodeCategorie() {
        return codeCategorie;
    }

    public void setCodeCategorie(String codeCategorie) {
        this.codeCategorie = codeCategorie;
    }

    public String getLibelleCategorie() {
        return libelleCategorie;
    }

    public void setLibelleCategorie(String libelleCategorie) {
        this.libelleCategorie = libelleCategorie;
    }

    public Integer getDelaiResolutionHeures() {
        return delaiResolutionHeures;
    }

    public void setDelaiResolutionHeures(Integer delaiResolutionHeures) {
        this.delaiResolutionHeures = delaiResolutionHeures;
    }

    public String getFlagActif() {
        return flagActif;
    }

    public void setFlagActif(String flagActif) {
        this.flagActif = flagActif;
    }
}
