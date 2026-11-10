package mg.cnaps.modele.service;
import mg.cnaps.modele.dto.FonctionDTO;
import mg.cnaps.modele.repository.FonctionRepository;

import java.util.List;

import org.springframework.stereotype.Service;

import mg.cnaps.modele.dto.ApplicationSection;
import mg.cnaps.modele.dto.SectionApplicationDTO;

@Service
public class FonctionService {

    private final FonctionRepository fonctionRepository;

    public FonctionService(FonctionRepository fonctionRepository) {
        this.fonctionRepository = fonctionRepository;
    }

    public List<FonctionDTO> listerFonctionsParService(String codeService) {
        if (codeService == null || codeService.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code service est obligatoire");
        }
        return fonctionRepository.findFonctionsByService(codeService.trim());
    }

    public List<SectionApplicationDTO> listerApplicationsParServiceEtFonction(
            String codeService, String codeFonction) {
        if (codeService == null || codeService.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code service est obligatoire");
        }
        if (codeFonction == null || codeFonction.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code fonction est obligatoire");
        }
        return fonctionRepository.findApplicationsByServiceAndFonction(
                codeService.trim(), codeFonction.trim());
    }

    public List<ApplicationSection> listerApplicationsAvecSections(
            String codeService, String codeFonction) {
        if (codeService == null || codeService.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code service est obligatoire");
        }
        if (codeFonction == null || codeFonction.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code fonction est obligatoire");
        }
        return fonctionRepository.findApplicationsAvecSections(
                codeService.trim(), codeFonction.trim());
    }
}
