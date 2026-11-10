package mg.cnaps.modele.dto;

public class ServiceDTO {
    private String codeService;
    private String libelleService;

    public ServiceDTO(String codeService, String libelleService) {
        this.codeService = codeService;
        this.libelleService = libelleService;
    }

    public String getCodeService() { return codeService; }
    public void setCodeService(String codeService) { this.codeService = codeService; }

    public String getLibelleService() { return libelleService; }
    public void setLibelleService(String libelleService) { this.libelleService = libelleService; }
}