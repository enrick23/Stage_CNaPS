package mg.cnaps.drh.prets.entity;

import javax.persistence.*;

/**
 * Table référentielle listant les catégories de prêts et avances accordés aux agents CNaPS.
 * Application : Gestion des Prêts et Avances
 * Schéma Oracle : DRH
 */
@Entity
@Table(name = "REF_TYPE_PRET", schema = "DRH")
public class RefTypePret {

    @Id
    @Column(name = "CODE_TYPE_PRET", length = 10, nullable = false)
    private String codeTypePret;

    @Column(name = "LIBELLE_TYPE_PRET", length = 100, nullable = false)
    private String libelleTypePret;

    @Column(name = "DUREE_MAX_MOIS", nullable = false)
    private Integer dureeMaxMois;

    @Column(name = "PLAFOND_MONTANT", precision = 15, scale = 2, nullable = false)
    private Double plafondMontant;

    @Column(name = "ACTIF", length = 1, nullable = false)
    private String actif;

    public RefTypePret() {
    }

    public String getCodeTypePret() {
        return codeTypePret;
    }

    public void setCodeTypePret(String codeTypePret) {
        this.codeTypePret = codeTypePret;
    }

    public String getLibelleTypePret() {
        return libelleTypePret;
    }

    public void setLibelleTypePret(String libelleTypePret) {
        this.libelleTypePret = libelleTypePret;
    }

    public Integer getDureeMaxMois() {
        return dureeMaxMois;
    }

    public void setDureeMaxMois(Integer dureeMaxMois) {
        this.dureeMaxMois = dureeMaxMois;
    }

    public Double getPlafondMontant() {
        return plafondMontant;
    }

    public void setPlafondMontant(Double plafondMontant) {
        this.plafondMontant = plafondMontant;
    }

    public String getActif() {
        return actif;
    }

    public void setActif(String actif) {
        this.actif = actif;
    }
}
