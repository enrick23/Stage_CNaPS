package mg.cnaps.modele.service;

import mg.cnaps.modele.dto.RevueMatriceDTO;
import mg.cnaps.modele.dto.RevueMatriceNomenclaturesDTO;
import mg.cnaps.modele.repository.RevueMatriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Service
public class RevueMatriceService {

    static final String AUDIT_EN_ATTENTE = "En attente";
    static final String AUDIT_VALIDEE = "Validée";

    static final String EXEC_AUCUNE = "AUCUNE ACTION";
    static final String EXEC_A_EXECUTER = "À EXÉCUTER";
    static final String EXEC_BLOQUEE = "BLOQUÉE";

    private final RevueMatriceRepository repository;
    private final RevueMatriceNomenclaturesDTO nomenclatures = new RevueMatriceNomenclaturesDTO();

    public RevueMatriceService(RevueMatriceRepository repository) {
        this.repository = repository;
    }

    public List<RevueMatriceDTO> listerMatrice(String codeService, String codeFonction) {
        if (estVide(codeService)) {
            throw new IllegalArgumentException("Le code service est obligatoire.");
        }
        if (estVide(codeFonction)) {
            throw new IllegalArgumentException("Le code fonction est obligatoire.");
        }
        return repository.findMatriceByServiceAndFonction(codeService.trim(), codeFonction.trim());
    }

    /**
     * Enregistrement d'une ligne par le Chef de Service.
     * Les champs d'audit et d'exécution envoyés par le client sont IGNORÉS :
     * seul l'auditeur (via validerAudit) peut les modifier.
     */
    @Transactional
    public RevueMatriceDTO enregistrerLigne(RevueMatriceDTO dto) {
        if (dto == null || estVide(dto.getCodeService()) || estVide(dto.getCodeFonction())
                || estVide(dto.getSection()) || estVide(dto.getApplication())) {
            throw new IllegalArgumentException("Service, Fonction, Section et Application sont obligatoires.");
        }
        normaliser(dto);
        validerValeur(dto.getDecisionRevue(), nomenclatures.getDecisions(), "Décision");
        validerValeur(dto.getNiveauAcces(), nomenclatures.getNiveauxAcces(), "Niveau d'accès");
        validerValeur(dto.getActionMouvement(), nomenclatures.getActionsMouvement(), "Action");

        String id = repository.findExistingId(dto.getCodeService(), dto.getCodeFonction(),
                dto.getSection(), dto.getApplication());
        RevueMatriceDTO existant = id != null ? repository.findById(id) : null;

        if (existant != null && !changementMetier(existant, dto)) {
            // Aucun changement d'arbitrage : on conserve la validation d'audit déjà obtenue.
            copierAudit(existant, dto);
        } else {
            // Nouvelle ligne ou arbitrage modifié : l'audit doit être refait.
            dto.setValidationAudit(AUDIT_EN_ATTENTE);
            dto.setAuditeurInfo(null);
            dto.setDateValidationAudit(null);
            dto.setAvisAudit(null);
        }
        dto.setAutorisationExecution(existant != null ? existant.getAutorisationExecution() : "NON");
        dto.setDateExecution(existant != null ? existant.getDateExecution() : null);
        dto.setExecutePar(existant != null ? existant.getExecutePar() : null);

        evaluerControleEtConformite(dto);
        evaluerStatutExecution(dto);

        if (existant != null) {
            dto.setIdRevueMatrice(id);
            repository.update(dto);
        } else {
            repository.insert(dto);
            dto.setIdRevueMatrice(repository.findExistingId(dto.getCodeService(), dto.getCodeFonction(),
                    dto.getSection(), dto.getApplication()));
        }
        return dto;
    }

    @Transactional
    public RevueMatriceDTO validerAudit(String idRevueMatrice, String validationAudit, String avisAudit, String auditeurInfo) {
        if (estVide(idRevueMatrice)) {
            throw new IllegalArgumentException("L'identifiant de la ligne est obligatoire.");
        }
        if (estVide(validationAudit)) {
            throw new IllegalArgumentException("Le statut de validation est obligatoire.");
        }
        validerValeur(validationAudit.trim(), nomenclatures.getValidationsAudit(), "Validation audit");

        RevueMatriceDTO existant = repository.findById(idRevueMatrice.trim());
        if (existant == null) {
            throw new IllegalArgumentException("Ligne de revue introuvable pour l'id : " + idRevueMatrice);
        }
        if (!AUDIT_VALIDEE.equals(validationAudit.trim()) && estVide(avisAudit)) {
            throw new IllegalArgumentException("Un avis est obligatoire en cas de rejet ou de correction demandée.");
        }

        existant.setValidationAudit(validationAudit.trim());
        existant.setAvisAudit(vide(avisAudit));
        existant.setAuditeurInfo(estVide(auditeurInfo) ? "AUDITEUR_INFO" : auditeurInfo.trim());
        existant.setDateValidationAudit(new Timestamp(System.currentTimeMillis()));
        evaluerStatutExecution(existant);

        repository.updateValidationAudit(existant.getIdRevueMatrice(), existant.getValidationAudit(),
                existant.getAvisAudit(), existant.getAuditeurInfo(), existant.getStatutExecution());
        return existant;
    }

    public RevueMatriceNomenclaturesDTO getNomenclatures() {
        return nomenclatures;
    }

    /** Règle 1 : contrôle automatisé des incohérences et données manquantes. */
    void evaluerControleEtConformite(RevueMatriceDTO dto) {
        String decision = dto.getDecisionRevue();
        String action = dto.getActionMouvement();
        String anomalie = null;

        if (decision == null || action == null || dto.getNiveauAcces() == null) {
            anomalie = "À COMPLÉTER (décision, niveau d'accès ou action manquant)";
        } else if ("DÉDIÉE AU SERVICE".equals(decision) && dto.getServiceCible() == null) {
            anomalie = "À COMPLÉTER (service cible manquant)";
        } else if (("Transfert".equals(action) || "Réaffectation".equals(action))
                && dto.getServiceCible() == null && dto.getFonctionCible() == null) {
            anomalie = "À COMPLÉTER (service ou fonction cible manquant)";
        } else if ("Suppression".equals(action) && dto.getObservation() == null) {
            anomalie = "À COMPLÉTER (motif de suppression manquant)";
        } else if ("À RETENIR".equals(decision) && !"Maintien".equals(action)) {
            anomalie = "INCOHÉRENT (À RETENIR implique Maintien)";
        } else if ("À SUPPRIMER".equals(decision) && !"Suppression".equals(action)) {
            anomalie = "INCOHÉRENT (À SUPPRIMER implique Suppression)";
        } else if ("À ANALYSER".equals(decision)) {
            anomalie = "À VÉRIFIER (décision non tranchée)";
        }

        dto.setControleAuto(anomalie == null ? "CONFORME" : anomalie);
        dto.setConformite(anomalie == null ? "CONFORME" : "À VÉRIFIER");
    }

    /** Règle 2 : toute action autre que Maintien reste BLOQUÉE tant que l'audit n'est pas Validée. */
    void evaluerStatutExecution(RevueMatriceDTO dto) {
        String action = dto.getActionMouvement();
        if (action == null || "Maintien".equals(action)) {
            dto.setStatutExecution(EXEC_AUCUNE);
        } else if (AUDIT_VALIDEE.equals(dto.getValidationAudit()) && "CONFORME".equals(dto.getConformite())) {
            dto.setStatutExecution(EXEC_A_EXECUTER);
        } else {
            dto.setStatutExecution(EXEC_BLOQUEE);
        }
    }

    private boolean changementMetier(RevueMatriceDTO a, RevueMatriceDTO b) {
        return !Objects.equals(a.getDecisionRevue(), b.getDecisionRevue())
                || !Objects.equals(a.getNiveauAcces(), b.getNiveauAcces())
                || !Objects.equals(a.getActionMouvement(), b.getActionMouvement())
                || !Objects.equals(a.getServiceCible(), b.getServiceCible())
                || !Objects.equals(a.getFonctionCible(), b.getFonctionCible());
    }

    private void copierAudit(RevueMatriceDTO source, RevueMatriceDTO cible) {
        cible.setValidationAudit(source.getValidationAudit() == null ? AUDIT_EN_ATTENTE : source.getValidationAudit());
        cible.setAuditeurInfo(source.getAuditeurInfo());
        cible.setDateValidationAudit(source.getDateValidationAudit());
        cible.setAvisAudit(source.getAvisAudit());
    }

    private void normaliser(RevueMatriceDTO dto) {
        dto.setCodeService(dto.getCodeService().trim());
        dto.setCodeFonction(dto.getCodeFonction().trim());
        dto.setSection(dto.getSection().trim());
        dto.setApplication(dto.getApplication().trim());
        dto.setCodeDirection(vide(dto.getCodeDirection()));
        dto.setServiceCible(vide(dto.getServiceCible()));
        dto.setFonctionCible(vide(dto.getFonctionCible()));
        dto.setFonctionnement(vide(dto.getFonctionnement()));
        dto.setDecisionRevue(vide(dto.getDecisionRevue()));
        dto.setNiveauAcces(vide(dto.getNiveauAcces()));
        dto.setActionMouvement(vide(dto.getActionMouvement()));
        dto.setRegleHabilitation(vide(dto.getRegleHabilitation()));
        dto.setObservation(vide(dto.getObservation()));
    }

    private void validerValeur(String valeur, List<String> autorisees, String libelle) {
        if (valeur != null && !autorisees.contains(valeur)) {
            throw new IllegalArgumentException(libelle + " invalide : " + valeur);
        }
    }

    private static boolean estVide(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Retourne null pour une chaîne vide ou "null" (valeur envoyée par un select HTML non renseigné). */
    private static String vide(String s) {
        if (estVide(s) || "null".equalsIgnoreCase(s.trim())) {
            return null;
        }
        return s.trim();
    }
}
