package mg.cnaps.modele.dto;

public class ServiceSimple {

    private String codeService;
    private String nomService;

    public ServiceSimple() {
    }

    public ServiceSimple(String codeService, String nomService) {
        this.codeService = codeService;
        this.nomService = nomService;
    }

    public String getCodeService() {
        return codeService;
    }

    public void setCodeService(String codeService) {
        this.codeService = codeService;
    }

    public String getNomService() {
        return nomService;
    }

    public void setNomService(String nomService) {
        this.nomService = nomService;
    }
}
