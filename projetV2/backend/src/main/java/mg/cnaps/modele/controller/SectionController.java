package mg.cnaps.modele.controller;

import mg.cnaps.modele.dto.SectionApplicationProgrammeDTO;
import mg.cnaps.modele.dto.SectionDTO;
import mg.cnaps.modele.service.SectionService;

import mg.cnaps.modele.dto.SectionApplicationProgrammeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    /**
     * GET /api/sections/service/{codeService}
     * Liste les sections d'un service (sans les applications).
     */
    @GetMapping("/service/{codeService}")
    public ResponseEntity<List<SectionDTO>> listerParService(
            @PathVariable String codeService) {
        return ResponseEntity.ok(sectionService.listerParService(codeService));
    }

    /**
     * GET /api/sections/service/{codeService}/applications
     * Liste les sections d'un service AVEC leurs applications.
     */
    @GetMapping("/service/{codeService}/applications")
    public ResponseEntity<List<SectionApplicationProgrammeDTO>> listerSectionsAvecApplications(
            @PathVariable String codeService) {
        return ResponseEntity.ok(sectionService.listerSectionsAvecApplications(codeService));
    }

    /**
     * GET /api/sections/{section}
     * Récupère une section par son code.
     */
    @GetMapping("/{section}")
    public ResponseEntity<SectionDTO> trouverParCode(@PathVariable String section) {
        return ResponseEntity.ok(sectionService.trouverParCode(section));
    }
}