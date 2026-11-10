package mg.cnaps.modele.controller;

import mg.cnaps.modele.dto.RevueMatriceDTO;
import mg.cnaps.modele.dto.RevueMatriceNomenclaturesDTO;
import mg.cnaps.modele.service.RevueMatriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/revue-matrice")
public class RevueMatriceController {

    private final RevueMatriceService service;

    public RevueMatriceController(RevueMatriceService service) {
        this.service = service;
    }

    @GetMapping("/service/{codeService}/fonction/{codeFonction}")
    public ResponseEntity<List<RevueMatriceDTO>> listerMatrice(
            @PathVariable String codeService,
            @PathVariable String codeFonction) {
        return ResponseEntity.ok(service.listerMatrice(codeService, codeFonction));
    }

    @PostMapping
    public ResponseEntity<RevueMatriceDTO> enregistrerLigne(@RequestBody RevueMatriceDTO dto) {
        return ResponseEntity.ok(service.enregistrerLigne(dto));
    }

    @PutMapping("/{id}/validation-audit")
    public ResponseEntity<RevueMatriceDTO> validerAudit(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {
        String validation = payload.get("validationAudit");
        String avis = payload.get("avisAudit");
        String auditeur = payload.get("auditeurInfo");
        return ResponseEntity.ok(service.validerAudit(id, validation, avis, auditeur));
    }

    @GetMapping("/nomenclatures")
    public ResponseEntity<RevueMatriceNomenclaturesDTO> getNomenclatures() {
        return ResponseEntity.ok(service.getNomenclatures());
    }
}

