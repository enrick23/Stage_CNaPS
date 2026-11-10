package mg.cnaps.modele.controller;

import mg.cnaps.modele.dto.BesoinServiceDTO;
import mg.cnaps.modele.dto.RevuDTO;
import mg.cnaps.modele.dto.TypeActionDTO;
import mg.cnaps.modele.service.RevuService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController 
@RequestMapping ("/revu")
public class RevuController {
    private final RevuService revuService;

    public RevuController(RevuService revuService) {
        this.revuService = revuService;
    }

    @GetMapping("/besoins-service")
    public ResponseEntity<List<BesoinServiceDTO>> listerBesoinsService() {
        return ResponseEntity.ok(revuService.listerBesoinsService());
    }

    @GetMapping("/types-action")
    public ResponseEntity<List<TypeActionDTO>> listerTypesAction() {
        return ResponseEntity.ok(revuService.listerTypesAction());
    }

    @PostMapping
    public ResponseEntity<Void> insererRevu(@RequestBody RevuDTO revu) {
        int lignesInserees = revuService.ajouterRevuAcc(revu);
        if (lignesInserees == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
