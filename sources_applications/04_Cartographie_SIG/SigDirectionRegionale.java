package mg.cnaps.sig.entity;

import javax.persistence.*;

/**
 * Table référentielle contenant les représentations régionales de la CNaPS.
 * Correspondance exacte avec le fichier modèle DICO DE DONNES.xlsx officiel de la CNaPS.
 * Application : Système d'Information Géographique (Cartographie SIG)
 * Schéma Oracle : SIG
 */
@Entity
@Table(name = "SIG_DR", schema = "SIG")
public class SigDirectionRegionale {

    @Id
    @Column(name = "DR_CODE", length = 2, nullable = false)
    private String drCode;

    @Column(name = "DR_NOM", length = 50, nullable = false)
    private String drNom;

    @Column(name = "DR_ABREVIATION", length = 5)
    private String drAbreviation;

    @Column(name = "DR_LIEU", length = 250)
    private String drLieu;

    @Column(name = "NUM_MOBILE", length = 15, nullable = false)
    private String numMobile;

    @Column(name = "CODE_ZONE", length = 3, nullable = false)
    private String codeZone;

    public SigDirectionRegionale() {
    }

    public String getDrCode() {
        return drCode;
    }

    public void setDrCode(String drCode) {
        this.drCode = drCode;
    }

    public String getDrNom() {
        return drNom;
    }

    public void setDrNom(String drNom) {
        this.drNom = drNom;
    }

    public String getDrAbreviation() {
        return drAbreviation;
    }

    public void setDrAbreviation(String drAbreviation) {
        this.drAbreviation = drAbreviation;
    }

    public String getDrLieu() {
        return drLieu;
    }

    public void setDrLieu(String drLieu) {
        this.drLieu = drLieu;
    }

    public String getNumMobile() {
        return numMobile;
    }

    public void setNumMobile(String numMobile) {
        this.numMobile = numMobile;
    }

    public String getCodeZone() {
        return codeZone;
    }

    public void setCodeZone(String codeZone) {
        this.codeZone = codeZone;
    }
}
