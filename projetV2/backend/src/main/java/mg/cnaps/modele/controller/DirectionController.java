package mg.cnaps.modele.controller;
import mg.cnaps.modele.dto.DirectionDTO;
import mg.cnaps.modele.service.DirectionService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.cnaps.modele.dto.ServiceDTO;

@RestController
@RequestMapping("/api/directions")
public class DirectionController {

    private final DirectionService directionService;

    public DirectionController(DirectionService directionService) {
        this.directionService = directionService;
    }

    @GetMapping
    public ResponseEntity<List<DirectionDTO>> listerDirections() {
        return ResponseEntity.ok(directionService.listerDirections());
    }

    @GetMapping("/{codeDirection}/services")
    public ResponseEntity<List<ServiceDTO>> listerServices(
            @PathVariable String codeDirection) {
        return ResponseEntity.ok(
                directionService.listerServicesParDirection(codeDirection)
        );
    }
}
