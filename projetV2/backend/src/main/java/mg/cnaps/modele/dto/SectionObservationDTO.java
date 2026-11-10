package mg.cnaps.modele.dto;

import java.sql.Timestamp;

public class SectionObservationDTO {

    private String idModifObs;
    private String code;
    private String nom;
    private String observation;
    private Timestamp dateObservation;
    private String servicePropriete;
    private String fonctionnement;

    public SectionObservationDTO() {
    }

    public SectionObservationDTO(String idModifObs, String code, String nom,
                                 String observation, Timestamp dateObservation,
                                 String servicePropriete, String fonctionnement) {
        this.idModifObs = idModifObs;
        this.code = code;
        this.nom = nom;
        this.observation = observation;
        this.dateObservation = dateObservation;
        this.servicePropriete = servicePropriete;
        this.fonctionnement = fonctionnement;
    }

    public String getIdModifObs() { return idModifObs; }
    public void setIdModifObs(String idModifObs) { this.idModifObs = idModifObs; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public Timestamp getDateObservation() { return dateObservation; }
    public void setDateObservation(Timestamp dateObservation) { this.dateObservation = dateObservation; }

    public String getServicePropriete() { return servicePropriete; }
    public void setServicePropriete(String servicePropriete) { this.servicePropriete = servicePropriete; }

    public String getFonctionnement() { return fonctionnement; }
    public void setFonctionnement(String fonctionnement) { this.fonctionnement = fonctionnement; }
}
