package mg.cnaps.modele.dto;

import java.util.List;

public class ApplicationDTO {

    private String menuCode;
    private String nomApplication;
    private List<ServiceSimple> services;
    private List<Observation> observations;

    public ApplicationDTO() {
    }

    public ApplicationDTO(String menuCode, String nomApplication,
            List<ServiceSimple> services,
            List<Observation> observations) {
        this.menuCode = menuCode;
        this.nomApplication = nomApplication;
        this.services = services;
        this.observations = observations;
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

    public List<ServiceSimple> getServices() {
        return services;
    }

    public void setServices(List<ServiceSimple> services) {
        this.services = services;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public void setObservations(List<Observation> observations) {
        this.observations = observations;
    }
}
