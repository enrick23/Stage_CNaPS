package mg.cnaps.modele.dto;

/**
 * Représente une section (uniquement).
 */
public class SectionDTO {

    private String section;
    private String nomSection;
    private String domaine;

    public SectionDTO() { }

    public SectionDTO(String section, String nomSection) {
        this.section = section;
        this.nomSection = nomSection;
    }

    public SectionDTO(String section, String nomSection, String domaine) {
        this.section = section;
        this.nomSection = nomSection;
        this.domaine = domaine;
    }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getNomSection() { return nomSection; }
    public void setNomSection(String nomSection) { this.nomSection = nomSection; }

    public String getDomaine() { return domaine; }
    public void setDomaine(String domaine) { this.domaine = domaine; }
}