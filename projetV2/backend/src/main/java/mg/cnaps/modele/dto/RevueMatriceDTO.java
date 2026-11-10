package mg.cnaps.modele.dto;

import java.sql.Timestamp;

public class RevueMatriceDTO {

    private String idRevueMatrice;
    private String codeDirection;
    private String codeService;
    private String codeFonction;
    private String section;
    private String nomSection;
    private String application;
    private String nomApplication;

    // Cible & Normalisation
    private String serviceCible;
    private String fonctionCible;
    private String fonctionnement;

    // Arbitrage Métier (Chef de Service)
    private String decisionRevue;
    private String niveauAcces;
    private String actionMouvement;
    private String regleHabilitation;

    // Contrôle automatisé & Conformité
    private String controleAuto;
    private String conformite;

    // Validation Auditeur Informatique
    private String validationAudit;
    private String auditeurInfo;
    private Timestamp dateValidationAudit;
    private String avisAudit;

    // Exécution & Traçabilité
    private String autorisationExecution;
    private Timestamp dateExecution;
    private String executePar;
    private String statutExecution;
    private String observation;
    private Timestamp dateModification;

    public RevueMatriceDTO() {
    }

    public String getIdRevueMatrice() {
        return idRevueMatrice;
    }

    public void setIdRevueMatrice(String idRevueMatrice) {
        this.idRevueMatrice = idRevueMatrice;
    }

    public String getCodeDirection() {
        return codeDirection;
    }

    public void setCodeDirection(String codeDirection) {
        this.codeDirection = codeDirection;
    }

    public String getCodeService() {
        return codeService;
    }

    public void setCodeService(String codeService) {
        this.codeService = codeService;
    }

    public String getCodeFonction() {
        return codeFonction;
    }

    public void setCodeFonction(String codeFonction) {
        this.codeFonction = codeFonction;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getNomSection() {
        return nomSection;
    }

    public void setNomSection(String nomSection) {
        this.nomSection = nomSection;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getNomApplication() {
        return nomApplication;
    }

    public void setNomApplication(String nomApplication) {
        this.nomApplication = nomApplication;
    }

    public String getServiceCible() {
        return serviceCible;
    }

    public void setServiceCible(String serviceCible) {
        this.serviceCible = serviceCible;
    }

    public String getFonctionCible() {
        return fonctionCible;
    }

    public void setFonctionCible(String fonctionCible) {
        this.fonctionCible = fonctionCible;
    }

    public String getFonctionnement() {
        return fonctionnement;
    }

    public void setFonctionnement(String fonctionnement) {
        this.fonctionnement = fonctionnement;
    }

    public String getDecisionRevue() {
        return decisionRevue;
    }

    public void setDecisionRevue(String decisionRevue) {
        this.decisionRevue = decisionRevue;
    }

    public String getNiveauAcces() {
        return niveauAcces;
    }

    public void setNiveauAcces(String niveauAcces) {
        this.niveauAcces = niveauAcces;
    }

    public String getActionMouvement() {
        return actionMouvement;
    }

    public void setActionMouvement(String actionMouvement) {
        this.actionMouvement = actionMouvement;
    }

    public String getRegleHabilitation() {
        return regleHabilitation;
    }

    public void setRegleHabilitation(String regleHabilitation) {
        this.regleHabilitation = regleHabilitation;
    }

    public String getControleAuto() {
        return controleAuto;
    }

    public void setControleAuto(String controleAuto) {
        this.controleAuto = controleAuto;
    }

    public String getConformite() {
        return conformite;
    }

    public void setConformite(String conformite) {
        this.conformite = conformite;
    }

    public String getValidationAudit() {
        return validationAudit;
    }

    public void setValidationAudit(String validationAudit) {
        this.validationAudit = validationAudit;
    }

    public String getAuditeurInfo() {
        return auditeurInfo;
    }

    public void setAuditeurInfo(String auditeurInfo) {
        this.auditeurInfo = auditeurInfo;
    }

    public Timestamp getDateValidationAudit() {
        return dateValidationAudit;
    }

    public void setDateValidationAudit(Timestamp dateValidationAudit) {
        this.dateValidationAudit = dateValidationAudit;
    }

    public String getAvisAudit() {
        return avisAudit;
    }

    public void setAvisAudit(String avisAudit) {
        this.avisAudit = avisAudit;
    }

    public String getAutorisationExecution() {
        return autorisationExecution;
    }

    public void setAutorisationExecution(String autorisationExecution) {
        this.autorisationExecution = autorisationExecution;
    }

    public Timestamp getDateExecution() {
        return dateExecution;
    }

    public void setDateExecution(Timestamp dateExecution) {
        this.dateExecution = dateExecution;
    }

    public String getExecutePar() {
        return executePar;
    }

    public void setExecutePar(String executePar) {
        this.executePar = executePar;
    }

    public String getStatutExecution() {
        return statutExecution;
    }

    public void setStatutExecution(String statutExecution) {
        this.statutExecution = statutExecution;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Timestamp getDateModification() {
        return dateModification;
    }

    public void setDateModification(Timestamp dateModification) {
        this.dateModification = dateModification;
    }
}

