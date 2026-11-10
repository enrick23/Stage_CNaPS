package mg.cnaps.modele.dto;

import java.sql.Timestamp;

public class Observation {

    private String idModifObs;
    private String codeService;
    private String section;
    private String application;
    private String observation;
    private Timestamp dateObservation;
    private String servicePropriete;
    private String fonctionnement;

    public Observation() {
    }

    public Observation(String idModifObs, String codeService, String section,
            String application, String observation, Timestamp dateObservation,
            String servicePropriete, String fonctionnement) {
        this.idModifObs = idModifObs;
        this.codeService = codeService;
        this.section = section;
        this.application = application;
        this.observation = observation;
        this.dateObservation = dateObservation;
        this.servicePropriete = servicePropriete;
        this.fonctionnement = fonctionnement;
    }

    public String getIdModifObs() {
        return idModifObs;
    }

    public void setIdModifObs(String idModifObs) {
        this.idModifObs = idModifObs;
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

    public String getObservation() {
        return observation;
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
