package mg.cnaps.modele.controller;

import mg.cnaps.modele.dto.ProfilDTO;
import mg.cnaps.modele.service.ProfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profil")
public class ProfilController {

    private final ProfilService profilService;

    public ProfilController(ProfilService profilService) {
        this.profilService = profilService;
    }

    
    @GetMapping("/mon-compte")
    public ResponseEntity<ProfilDTO> getMonProfil() {
        return ResponseEntity.ok(profilService.getMonProfil());
    }
}