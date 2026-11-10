package mg.cnaps.modele.controller;
import mg.cnaps.modele.dto.ColonneDTO;
import mg.cnaps.modele.dto.DatabaseDTO;
import mg.cnaps.modele.service.DatabaseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/db")
public class DatabaseController {

    private final DatabaseService databaseService;

    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GetMapping("/tables")
    public ResponseEntity<List<DatabaseDTO>> listerTables() {
        return ResponseEntity.ok(databaseService.listerTables());
    }

    @GetMapping("/tables/{nomTable}/colonnes")
    public ResponseEntity<List<ColonneDTO>> colonnes(@PathVariable String nomTable) {
        return ResponseEntity.ok(databaseService.getColonnes(nomTable));
    }

    @GetMapping("/tables/{nomTable}/contenu")
    public ResponseEntity<List<Map<String, Object>>> contenu(@PathVariable String nomTable) {
        return ResponseEntity.ok(databaseService.getContenu(nomTable));
    }
}