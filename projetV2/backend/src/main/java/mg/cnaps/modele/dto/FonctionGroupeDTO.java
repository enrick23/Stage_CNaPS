package mg.cnaps.modele.dto;

import java.util.ArrayList;
import java.util.List;

public class FonctionGroupeDTO {
    private String libelleFonction;
    private List<AgentGroupeDTO> agents = new ArrayList<>();

    public FonctionGroupeDTO() {}

    public FonctionGroupeDTO(String libelleFonction, List<AgentGroupeDTO> agents) {
        this.libelleFonction = libelleFonction;
        this.agents = agents;
    }

    public String getLibelleFonction() { return libelleFonction; }
    public void setLibelleFonction(String libelleFonction) { this.libelleFonction = libelleFonction; }

    public List<AgentGroupeDTO> getAgents() { return agents; }
    public void setAgents(List<AgentGroupeDTO> agents) { this.agents = agents; }
}