package mg.cnaps.modele.dto;

public class MenuDTO {

    private String code;
    private String libelle;
    private String description;

    public MenuDTO() { }

    public MenuDTO(String code, String libelle, String description) {
        this.code = code;
        this.libelle = libelle;
        this.description = description;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}