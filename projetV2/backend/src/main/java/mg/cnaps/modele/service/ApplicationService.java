package mg.cnaps.modele.service;
import mg.cnaps.modele.dto.ApplicationDTO;
import mg.cnaps.modele.repository.ApplicationRepository;
import mg.cnaps.modele.dto.ApplicationSection;
import mg.cnaps.modele.dto.Observation;
import mg.cnaps.modele.dto.SectionApplicationDTO;
import mg.cnaps.modele.dto.ServiceSimple;

import java.util.List;

import org.springframework.stereotype.Service;

import mg.cnaps.modele.dto.FonctionDTO;
import mg.cnaps.modele.service.FonctionService;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final FonctionService fonctionService;

    public ApplicationService(ApplicationRepository applicationRepository,
            FonctionService fonctionService) {
        this.applicationRepository = applicationRepository;
        this.fonctionService = fonctionService;
    }


    public ApplicationDTO getApplicationComplete(String menuCode) {
        if (menuCode == null || menuCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code application est obligatoire");
        }

        String nomApp = applicationRepository.findNomApplication(menuCode);
        List<ServiceSimple> services =
                applicationRepository.findServicesByApplication(menuCode);
        List<Observation> observations =
                applicationRepository.findObservationsByApplication(menuCode);

        return new ApplicationDTO(menuCode, nomApp, services, observations);
    }


    public List<FonctionDTO> listerFonctionsParService(String codeService) {
        return fonctionService.listerFonctionsParService(codeService);
    }

    public List<SectionApplicationDTO> listerApplicationsParServiceEtFonction(
            String codeService, String codeFonction) {
        return fonctionService.listerApplicationsParServiceEtFonction(
                codeService, codeFonction);
    }

    public List<ApplicationSection> listerApplicationsAvecSections(
            String codeService, String codeFonction) {
        return fonctionService.listerApplicationsAvecSections(
                codeService, codeFonction);
    }

    public void ajouterObservation(String codeService, String section, String application,
            String observation, String servicePropriete, String fonctionnement) {
        if (codeService == null || codeService.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code service est obligatoire");
        }
        if (section == null || section.trim().isEmpty()) {
            throw new IllegalArgumentException("La section est obligatoire");
        }
        if (application == null || application.trim().isEmpty()) {
            throw new IllegalArgumentException("L'application est obligatoire");
        }
        if (observation == null || observation.trim().isEmpty()) {
            throw new IllegalArgumentException("L'observation est obligatoire");
        }

        applicationRepository.insereObservation(
                codeService.trim(),
                section.trim(),
                application.trim(),
                observation.trim(),
                servicePropriete,
                fonctionnement);
    }

    public void modifierObservation(String idModifObs, String observation) {
        if (idModifObs == null || idModifObs.trim().isEmpty()) {
            throw new IllegalArgumentException("L'identifiant de l'observation est obligatoire");
        }
        if (observation == null || observation.trim().isEmpty()) {
            throw new IllegalArgumentException("L'observation est obligatoire");
        }

        applicationRepository.modifierObservation(idModifObs.trim(), observation.trim());
    }

}