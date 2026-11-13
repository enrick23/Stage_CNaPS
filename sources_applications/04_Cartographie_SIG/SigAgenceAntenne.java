package mg.cnaps.sig.entity;

import javax.persistence.*;

/**
 * Table des agences et antennes locales rattachées aux directions régionales.
 * Application : Système d'Information Géographique (Cartographie SIG)
 * Schéma Oracle : SIG
 */
@Entity
@Table(name = "SIG_AGENCE", schema = "SIG")
public class SigAgenceAntenne {

    @Id
    @Column(name = "CODE_AGENCE", length = 10, nullable = false)
    private String codeAgence;

    @Column(name = "NOM_AGENCE", length = 100, nullable = false)
    private String nomAgence;

    @Column(name = "DR_CODE", length = 2, nullable = false)
    private String drCode;

    @Column(name = "ADRESSE_PHYSIQUE", length = 250)
    private String adressePhysique;

    @Column(name = "VILLE", length = 100, nullable = false)
    private String ville;

    @Column(name = "TELEPHONE", length = 20)
    private String telephone;

    @Column(name = "FLAG_ACTIF", length = 1, nullable = false)
    private String flagActif;

    public SigAgenceAntenne() {
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getNomAgence() {
        return nomAgence;
    }

    public void setNomAgence(String nomAgence) {
        this.nomAgence = nomAgence;
    }

    public String getDrCode() {
        return drCode;
    }

    public void setDrCode(String drCode) {
        this.drCode = drCode;
    }

    public String getAdressePhysique() {
        return adressePhysique;
    }

    public void setAdressePhysique(String adressePhysique) {
        this.adressePhysique = adressePhysique;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getFlagActif() {
        return flagActif;
    }

    public void setFlagActif(String flagActif) {
        this.flagActif = flagActif;
    }
}
