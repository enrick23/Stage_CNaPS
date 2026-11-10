package mg.cnaps.modele.dto;

public class SectionApplicationProgrammeDTO {

    private String section;
    private String nomSection;
    private String menuCode;
    private String nomApplication;
    private String programme;

    public SectionApplicationProgrammeDTO() {
    }

    public SectionApplicationProgrammeDTO(String section, String nomSection, String menuCode,
            String nomApplication, String programme) {
        this.section = section;
        this.nomSection = nomSection;
        this.menuCode = menuCode;
        this.nomApplication = nomApplication;
        this.programme = programme;
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

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }
}