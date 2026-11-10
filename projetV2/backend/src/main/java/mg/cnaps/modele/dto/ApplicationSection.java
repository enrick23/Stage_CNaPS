package mg.cnaps.modele.dto;

import java.sql.Timestamp;

public class ApplicationSection {

    private String menuCode;
    private String nomApplication;
    private String section;
    private String nomSection;
    private String idModifObs;
    private String observation;
    private Timestamp dateObservation;
    private String servicePropriete;
    private String fonctionnement;

    public ApplicationSection() {
    }

    public ApplicationSection(String menuCode, String nomApplication, String section, String nomSection,
            String observation, Timestamp dateObservation) {
        this(menuCode, nomApplication, section, nomSection, null, observation,
            dateObservation, null, null);
        }

        public ApplicationSection(String menuCode, String nomApplication, String section, String nomSection,
            String idModifObs, String observation, Timestamp dateObservation,
            String servicePropriete, String fonctionnement) {
        this.menuCode = menuCode;
        this.nomApplication = nomApplication;
        this.section = section;
        this.nomSection = nomSection;
        this.idModifObs = idModifObs;
        this.observation = observation;
        this.dateObservation = dateObservation;
        this.servicePropriete = servicePropriete;
        this.fonctionnement = fonctionnement;
    }

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    public String getNomApplication() {
        return nomApplication;
    }

    public void setNomApplication(String nomApplication) {
        this.nomApplication = nomApplication;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getNomSection() {
        return nomSection;
    }

    public void setNomSection(String nomSection) {
        this.nomSection = nomSection;
    }

    public String getObservation() {
        return observation;
    }

    public String getIdModifObs() {
        return idModifObs;
    }

    public void setIdModifObs(String idModifObs) {
        this.idModifObs = idModifObs;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Timestamp getDateObservation() {
        return dateObservation;
    }

    public void setDateObservation(Timestamp dateObservation) {
        this.dateObservation = dateObservation;
    }

    public String getServicePropriete() {
        return servicePropriete;
    }

    public void setServicePropriete(String servicePropriete) {
        this.servicePropriete = servicePropriete;
    }

    public String getFonctionnement() {
        return fonctionnement;
    }

    public void setFonctionnement(String fonctionnement) {
        this.fonctionnement = fonctionnement;
    }

}


