package mg.cnaps.modele.controller;
import mg.cnaps.modele.dto.ApplicationDTO;
import mg.cnaps.modele.dto.ApplicationSection;
import mg.cnaps.modele.service.ApplicationService;
import mg.cnaps.modele.dto.Observation;
import mg.cnaps.modele.dto.SectionApplicationDTO;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import mg.cnaps.modele.dto.FonctionDTO;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{menuCode}")
    public ResponseEntity<ApplicationDTO> getApplicationComplete(
            @PathVariable String menuCode) {
        return ResponseEntity.ok(
                applicationService.getApplicationComplete(menuCode));
    }

    @GetMapping("/fonctions/service/{codeService}")
    public ResponseEntity<List<FonctionDTO>> listerFonctions(
            @PathVariable String codeService) {
        return ResponseEntity.ok(applicationService.listerFonctionsParService(codeService));
    }

    @GetMapping("/service/{codeService}/fonction/{codeFonction}")
    public ResponseEntity<List<SectionApplicationDTO>> listerApplications(
            @PathVariable String codeService,
            @PathVariable String codeFonction) {
        return ResponseEntity.ok(
                applicationService.listerApplicationsParServiceEtFonction(
                        codeService, codeFonction));
    }

    @GetMapping("/service/{codeService}/fonction/{codeFonction}/sections")
    public ResponseEntity<List<ApplicationSection>> listerApplicationsAvecSections(
            @PathVariable String codeService,
            @PathVariable String codeFonction) {
        return ResponseEntity.ok(
                applicationService.listerApplicationsAvecSections(codeService, codeFonction));
    }

    @PostMapping("/observations")
    public ResponseEntity<String> ajouterObservation(
            @RequestParam String codeService,
            @RequestParam String section,
            @RequestParam String application,
            @RequestParam String observation,
            @RequestParam String servicePropriete,
            @RequestParam String fonctionnement) {
        applicationService.ajouterObservation(codeService, section, application, observation,
                servicePropriete, fonctionnement);
        return ResponseEntity.ok("Observation ajoutée avec succès");
    }

    @PutMapping("/modif/observation")
    public ResponseEntity<String> modifierObservation(
            @RequestParam String idModifObs,
            @RequestParam String observation) {
        applicationService.modifierObservation(idModifObs, observation);
        return ResponseEntity.ok("Observation modifiée avec succès");

    }
}