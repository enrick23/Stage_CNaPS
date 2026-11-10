package mg.cnaps.modele.dto;

import java.util.ArrayList;
import java.util.List;

public class AgentGroupeDTO {

    private String matricule;
    private String nom;
    private String prenoms;
    private String libelleFonction;
    private String codeFonction;
    private List<SectionGroupeDTO> sections = new ArrayList<>();

    public AgentGroupeDTO() {
    }

    public AgentGroupeDTO(String matricule, String nom, String prenoms,
                          String libelleFonction, String codeFonction) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenoms = prenoms;
        this.libelleFonction = libelleFonction;
        this.codeFonction = codeFonction;
    }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenoms() { return prenoms; }
    public void setPrenoms(String prenoms) { this.prenoms = prenoms; }

    public String getLibelleFonction() { return libelleFonction; }
    public void setLibelleFonction(String libelleFonction) { this.libelleFonction = libelleFonction; }

    public String getCodeFonction() { return codeFonction; }
    public void setCodeFonction(String codeFonction) { this.codeFonction = codeFonction; }

    public List<SectionGroupeDTO> getSections() { return sections; }
    public void setSections(List<SectionGroupeDTO> sections) { this.sections = sections; }
}
