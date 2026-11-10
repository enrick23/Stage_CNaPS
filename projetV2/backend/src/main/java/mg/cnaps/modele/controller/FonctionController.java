package mg.cnaps.modele.controller;
import mg.cnaps.modele.dto.FonctionDTO;
import mg.cnaps.modele.service.FonctionService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.cnaps.modele.dto.ApplicationSection;
import mg.cnaps.modele.dto.SectionApplicationDTO;

@RestController
@RequestMapping("/api/fonctions")
public class FonctionController {

    private final FonctionService fonctionService;

    public FonctionController(FonctionService fonctionService) {
        this.fonctionService = fonctionService;
    }

    @GetMapping("/service/{codeService}")
    public ResponseEntity<List<FonctionDTO>> listerFonctions(@PathVariable String codeService) {
        return ResponseEntity.ok(fonctionService.listerFonctionsParService(codeService));
    }

    @GetMapping("/service/{codeService}/fonction/{codeFonction}")
    public ResponseEntity<List<SectionApplicationDTO>> listerApplications(
            @PathVariable String codeService,
            @PathVariable String codeFonction) {
        return ResponseEntity.ok(
                fonctionService.listerApplicationsParServiceEtFonction(codeService, codeFonction));
    }

    @GetMapping("/service/{codeService}/fonction/{codeFonction}/sections")
    public ResponseEntity<List<ApplicationSection>> listerApplicationsAvecSections(
            @PathVariable String codeService,
            @PathVariable String codeFonction) {
        return ResponseEntity.ok(
                fonctionService.listerApplicationsAvecSections(codeService, codeFonction));
    }
}
