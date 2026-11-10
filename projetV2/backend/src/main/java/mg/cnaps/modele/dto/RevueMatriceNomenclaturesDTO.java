package mg.cnaps.modele.dto;

import java.util.Arrays;
import java.util.List;

public class RevueMatriceNomenclaturesDTO {

    private List<String> decisions = Arrays.asList(
            "À RETENIR",
            "À SUPPRIMER",
            "DÉDIÉE AU SERVICE",
            "À ANALYSER"
    );

    private List<String> niveauxAcces = Arrays.asList(
            "Consultation",
            "Saisie",
            "Validation",
            "Administration"
    );

    private List<String> actionsMouvement = Arrays.asList(
            "Maintien",
            "Suppression",
            "Ajout",
            "Transfert",
            "Réaffectation"
    );

    private List<String> validationsAudit = Arrays.asList(
            "En attente",
            "Validée",
            "Rejetée",
            "À corriger"
    );

    private List<String> statutsExecution = Arrays.asList(
            "AUCUNE ACTION",
            "À EXÉCUTER",
            "EXÉCUTÉE",
            "BLOQUÉE"
    );

    public RevueMatriceNomenclaturesDTO() {
    }

    public List<String> getDecisions() {
        return decisions;
    }

    public void setDecisions(List<String> decisions) {
        this.decisions = decisions;
    }

    public List<String> getNiveauxAcces() {
        return niveauxAcces;
    }

    public void setNiveauxAcces(List<String> niveauxAcces) {
        this.niveauxAcces = niveauxAcces;
    }

    public List<String> getActionsMouvement() {
        return actionsMouvement;
    }

    public void setActionsMouvement(List<String> actionsMouvement) {
        this.actionsMouvement = actionsMouvement;
    }

    public List<String> getValidationsAudit() {
        return validationsAudit;
    }

    public void setValidationsAudit(List<String> validationsAudit) {
        this.validationsAudit = validationsAudit;
    }

    public List<String> getStatutsExecution() {
        return statutsExecution;
    }

    public void setStatutsExecution(List<String> statutsExecution) {
        this.statutsExecution = statutsExecution;
    }
}

