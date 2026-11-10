package mg.cnaps.modele.dto;

import java.util.ArrayList;
import java.util.List;

public class SectionGroupeDTO {

    private String section;
    private String nomSection;
    private List<ApplicationSection> applications = new ArrayList<>(); 

    public SectionGroupeDTO() {
    }

    public SectionGroupeDTO(String section, String nomSection) {
        this.section = section;
        this.nomSection = nomSection;
    }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getNomSection() { return nomSection; }
    public void setNomSection(String nomSection) { this.nomSection = nomSection; }

    public List<ApplicationSection> getApplications() { return applications; }
    public void setApplications(List<ApplicationSection> applications) { this.applications = applications; }
}
