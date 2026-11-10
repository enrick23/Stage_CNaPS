package mg.cnaps.modele.service;

import mg.cnaps.modele.dto.SectionApplicationProgrammeDTO;
import mg.cnaps.modele.dto.SectionDTO;
import mg.cnaps.modele.repository.SectionRepository;

import mg.cnaps.modele.dto.SectionApplicationProgrammeDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service = logique métier uniquement.
 * Aucun SQL.
 */
@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    /**
     * Liste les sections d'un service.
     * LOGIQUE MÉTIER : valide le code service.
     */
    public List<SectionDTO> listerParService(String codeService) {
        if (codeService == null || codeService.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code service est obligatoire");
        }
        return sectionRepository.findSectionsByService(codeService.trim());
    }

    /**
     * Liste les sections d'un service avec leurs applications.
     * LOGIQUE MÉTIER : valide le code service.
     */
    public List<SectionApplicationProgrammeDTO> listerSectionsAvecApplications(String codeService) {
        if (codeService == null || codeService.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code service est obligatoire");
        }
        return sectionRepository.findSectionsAvecApplications(codeService.trim());
    }

    /**
     * Récupère une section par son code.
     * LOGIQUE MÉTIER : lève une erreur si introuvable.
     */
    public SectionDTO trouverParCode(String section) {
        if (section == null || section.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code section est obligatoire");
        }

        SectionDTO dto = sectionRepository.findByCode(section.trim());
        if (dto == null) {
            throw new RuntimeException("Section introuvable : " + section);
        }
        return dto;
    }
}