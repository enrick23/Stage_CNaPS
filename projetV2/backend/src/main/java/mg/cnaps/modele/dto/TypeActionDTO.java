package mg.cnaps.modele.dto;

public class TypeActionDTO {
    private Integer idTypeAction;
    private String libelle;

    public TypeActionDTO() {}

    public TypeActionDTO(Integer idTypeAction, String libelle) {
        this.idTypeAction = idTypeAction;
        this.libelle = libelle;
    }

    public Integer getIdTypeAction() {
        return idTypeAction;
    }

    public void setIdTypeAction(Integer idTypeAction) {
        this.idTypeAction = idTypeAction;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}