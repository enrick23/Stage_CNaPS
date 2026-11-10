package mg.cnaps.modele.dto;

import java.util.ArrayList;
import java.util.List;

public class ApplicationGroupeDTO {

    private String menuCode;
    private String nomApplication;
    private List<String> programmes = new ArrayList<>();
    private String categorie = "";
    private String typeCategorie = "";
    private Integer idBesoinService;
    private String motifSuppression = "";

    public ApplicationGroupeDTO() {
    }

    public ApplicationGroupeDTO(String menuCode, String nomApplication) {
        this.menuCode = menuCode;
        this.nomApplication = nomApplication;
    }

    public String getMenuCode() { return menuCode; }
    public void setMenuCode(String menuCode) { this.menuCode = menuCode; }

    public String getNomApplication() { return nomApplication; }
    public void setNomApplication(String nomApplication) { this.nomApplication = nomApplication; }

    public List<String> getProgrammes() { return programmes; }
    public void setProgrammes(List<String> programmes) { this.programmes = programmes; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getTypeCategorie() { return typeCategorie; }
    public void setTypeCategorie(String typeCategorie) { this.typeCategorie = typeCategorie; }

    public Integer getIdBesoinService() { return idBesoinService; }
    public void setIdBesoinService(Integer idBesoinService) { this.idBesoinService = idBesoinService; }

    public String getMotifSuppression() { return motifSuppression; }
    public void setMotifSuppression(String motifSuppression) { this.motifSuppression = motifSuppression; }
}
