package mg.cnaps.modele.historevu;

import java.sql.Timestamp;

public class HistoRevuDTO {
    private String idAccHisto;
    private String idAccDetail;
    private String agentMatricule;
    private String codeFonction;
    private String codeService;
    private String section;
    private String application;
    private String categorie;
    private String typeCategorie;
    private String motifSuppression;
    private Timestamp dateAction;
    private Integer idBesoinService;
    private Integer idTypeAction;

    public HistoRevuDTO() {}

    public HistoRevuDTO(String idAccHisto, String idAccDetail, String agentMatricule, String codeFonction, 
                        String codeService, String section, String application, String categorie, 
                        String typeCategorie, String motifSuppression, Timestamp dateAction, 
                        Integer idBesoinService, Integer idTypeAction) {
        this.idAccHisto = idAccHisto;
        this.idAccDetail = idAccDetail;
        this.agentMatricule = agentMatricule;
        this.codeFonction = codeFonction;
        this.codeService = codeService;
        this.section = section;
        this.application = application;
        this.categorie = categorie;
        this.typeCategorie = typeCategorie;
        this.motifSuppression = motifSuppression;
        this.dateAction = dateAction;
        this.idBesoinService = idBesoinService;
        this.idTypeAction = idTypeAction;
    }

    public String getIdAccHisto() { return idAccHisto; }
    public void setIdAccHisto(String idAccHisto) { this.idAccHisto = idAccHisto; }

    public String getIdAccDetail() { return idAccDetail; }
    public void setIdAccDetail(String idAccDetail) { this.idAccDetail = idAccDetail; }

    public String getAgentMatricule() { return agentMatricule; }
    public void setAgentMatricule(String agentMatricule) { this.agentMatricule = agentMatricule; }

    public String getCodeFonction() { return codeFonction; }
    public void setCodeFonction(String codeFonction) { this.codeFonction = codeFonction; }

    public String getCodeService() { return codeService; }
    public void setCodeService(String codeService) { this.codeService = codeService; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getApplication() { return application; }
    public void setApplication(String application) { this.application = application; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getTypeCategorie() { return typeCategorie; }
    public void setTypeCategorie(String typeCategorie) { this.typeCategorie = typeCategorie; }

    public String getMotifSuppression() { return motifSuppression; }
    public void setMotifSuppression(String motifSuppression) { this.motifSuppression = motifSuppression; }

    public Timestamp getDateAction() { return dateAction; }
    public void setDateAction(Timestamp dateAction) { this.dateAction = dateAction; }

    public Integer getIdBesoinService() { return idBesoinService; }
    public void setIdBesoinService(Integer idBesoinService) { this.idBesoinService = idBesoinService; }

    public Integer getIdTypeAction() { return idTypeAction; }
    public void setIdTypeAction(Integer idTypeAction) { this.idTypeAction = idTypeAction; }
}