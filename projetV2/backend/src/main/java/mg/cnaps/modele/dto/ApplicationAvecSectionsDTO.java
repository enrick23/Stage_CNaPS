package mg.cnaps.modele.dto;

import java.util.ArrayList;
import java.util.List;

public class ApplicationAvecSectionsDTO {

    private String menuCode;
    private String nomApplication;
    private List<SectionObservationDTO> sections = new ArrayList<>();

    public ApplicationAvecSectionsDTO() {
    }

    public ApplicationAvecSectionsDTO(String menuCode, String nomApplication) {
        this.menuCode = menuCode;
        this.nomApplication = nomApplication;
    }

    public String getMenuCode() { return menuCode; }
    public void setMenuCode(String menuCode) { this.menuCode = menuCode; }

    public String getNomApplication() { return nomApplication; }
    public void setNomApplication(String nomApplication) { this.nomApplication = nomApplication; }

    public List<SectionObservationDTO> getSections() { return sections; }
    public void setSections(List<SectionObservationDTO> sections) { this.sections = sections; }
}
