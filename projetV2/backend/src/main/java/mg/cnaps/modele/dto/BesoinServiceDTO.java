package mg.cnaps.modele.dto;

public class BesoinServiceDTO {
    private Integer idBesoinService;
    private String libelle;

    public BesoinServiceDTO() {}

    public BesoinServiceDTO(Integer idBesoinService, String libelle) {
        this.idBesoinService = idBesoinService;
        this.libelle = libelle;
    }

    public Integer getIdBesoinService() {
        return idBesoinService;
    }

    public void setIdBesoinService(Integer idBesoinService) {
        this.idBesoinService = idBesoinService;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}