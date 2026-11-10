package mg.cnaps.modele.service;

import mg.cnaps.modele.dto.MenuDTO;
import mg.cnaps.modele.repository.MenuRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<MenuDTO> listerTous() {
        return menuRepository.findAll();
    }

    public MenuDTO trouverParCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code menu est obligatoire");
        }

        MenuDTO menu = menuRepository.findByCode(code);
        if (menu == null) {
            throw new RuntimeException("Menu introuvable : " + code);
        }
        return menu;
    }

    public MenuDTO creer(MenuDTO dto) {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Le code menu est obligatoire");
        }
        if (dto.getLibelle() == null || dto.getLibelle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé est obligatoire");
        }

        if (menuRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Un menu avec le code "
                    + dto.getCode() + " existe déjà");
        }

        menuRepository.save(dto);
        return dto;
    }

    public void supprimer(String code) {
        if (!menuRepository.existsByCode(code)) {
            throw new RuntimeException("Menu introuvable : " + code);
        }
        menuRepository.delete(code);
    }
}