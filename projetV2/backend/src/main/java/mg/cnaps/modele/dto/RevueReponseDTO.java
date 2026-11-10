package mg.cnaps.modele.dto;

import java.util.List;

public class RevueReponseDTO {
    private List<DirectionDTO> directions;
    private List<ServiceDTO> services;
    private List<FonctionDTO> fonctions;

    private List<FonctionGroupeDTO> fonctionsGroupees;

    public RevueReponseDTO() {}

    public List<DirectionDTO> getDirections() { return directions; }
    public void setDirections(List<DirectionDTO> directions) { this.directions = directions; }

    public List<ServiceDTO> getServices() { return services; }
    public void setServices(List<ServiceDTO> services) { this.services = services; }

    public List<FonctionDTO> getFonctions() { return fonctions; }
    public void setFonctions(List<FonctionDTO> fonctions) { this.fonctions = fonctions; }

    public List<FonctionGroupeDTO> getFonctionsGroupees() { return fonctionsGroupees; }
    public void setFonctionsGroupees(List<FonctionGroupeDTO> fonctionsGroupees) { this.fonctionsGroupees = fonctionsGroupees; }
}