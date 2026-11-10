package mg.cnaps.modele.controller;

import mg.cnaps.modele.dto.MenuDTO;
import mg.cnaps.modele.service.MenuService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<List<MenuDTO>> listerTous() {
        return ResponseEntity.ok(menuService.listerTous());
    }

    @GetMapping("/{code}")
    public ResponseEntity<MenuDTO> trouverParCode(@PathVariable String code) {
        return ResponseEntity.ok(menuService.trouverParCode(code));
    }

    @PostMapping
    public ResponseEntity<MenuDTO> creer(@RequestBody MenuDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.creer(dto));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> supprimer(@PathVariable String code) {
        menuService.supprimer(code);
        return ResponseEntity.noContent().build();
    }
}