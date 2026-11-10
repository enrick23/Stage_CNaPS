package mg.cnaps.modele.dto;

public class AgentDetailDTO {

    private String section;
    private String nomSection;
    private String matricule;
    private String nom;
    private String prenoms;
    private String libelleFonction;
    private String codeFonction;
    private String menuCode;
    private String nomApplication;
    private String programme;

    public AgentDetailDTO() { }

    public AgentDetailDTO(String section, String nomSection,
                          String matricule, String nom, String prenoms,
                          String libelleFonction, String codeFonction,
                          String menuCode, String nomApplication, String programme) {
        this.section = section;
        this.nomSection = nomSection;
        this.matricule = matricule;
        this.nom = nom;
        this.prenoms = prenoms;
        this.libelleFonction = libelleFonction;
        this.codeFonction = codeFonction;
        this.menuCode = menuCode;
        this.nomApplication = nomApplication;
        this.programme = programme;
    }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getNomSection() { return nomSection; }
    public void setNomSection(String nomSection) { this.nomSection = nomSection; }

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

    public String getMenuCode() { return menuCode; }
    public void setMenuCode(String menuCode) { this.menuCode = menuCode; }

    public String getNomApplication() { return nomApplication; }
    public void setNomApplication(String nomApplication) { this.nomApplication = nomApplication; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }
}