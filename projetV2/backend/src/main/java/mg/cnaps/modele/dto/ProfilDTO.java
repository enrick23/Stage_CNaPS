package mg.cnaps.modele.dto;

public class ProfilDTO {
    private String matricule;
    private String codeService;
    private String codeFonction;
    private String libelleFonction;
    private boolean estChef;
    private String message;

    public ProfilDTO() {}

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getCodeService() { return codeService; }
    public void setCodeService(String codeService) { this.codeService = codeService; }

    public String getCodeFonction() { return codeFonction; }
    public void setCodeFonction(String codeFonction) { this.codeFonction = codeFonction; }

    public String getLibelleFonction() { return libelleFonction; }
    public void setLibelleFonction(String libelleFonction) { this.libelleFonction = libelleFonction; }

    public boolean isEstChef() { return estChef; }
    public void setEstChef(boolean estChef) { this.estChef = estChef; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}