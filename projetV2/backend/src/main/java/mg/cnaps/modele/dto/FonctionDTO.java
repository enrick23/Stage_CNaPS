package mg.cnaps.modele.dto;

public class FonctionDTO {

    private String codeFonction;
    private String libelleFonction;

    public FonctionDTO() {
    }

    public FonctionDTO(String codeFonction, String libelleFonction) {
        this.codeFonction = codeFonction;
        this.libelleFonction = libelleFonction;
    }

    public String getCodeFonction() {
        return codeFonction;
    }

    public void setCodeFonction(String codeFonction) {
        this.codeFonction = codeFonction;
    }

    public String getLibelleFonction() {
        return libelleFonction;
    }

    public void setLibelleFonction(String libelleFonction) {
        this.libelleFonction = libelleFonction;
    }
}
