package mg.cnaps.modele.dto;

public class SectionApplicationDTO {

    private String section;
    private String nomSection;
    private String menuCode;
    private String nomApplication;

    public SectionApplicationDTO() {
    }

    public SectionApplicationDTO(String section, String nomSection, String menuCode, String nomApplication) {
        this.section = section;
        this.nomSection = nomSection;
        this.menuCode = menuCode;
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
}
