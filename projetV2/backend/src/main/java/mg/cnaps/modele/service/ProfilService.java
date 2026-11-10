package mg.cnaps.modele.service;

import mg.cnaps.modele.dto.ProfilDTO;
import mg.cnaps.modele.repository.ProfilRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProfilService {

    private final ProfilRepository profilRepository;

    @Value("${app.test.matricule-statique}")
    private String matriculeStatique;

    public ProfilService(ProfilRepository profilRepository) {
        this.profilRepository = profilRepository;
    }

    public ProfilDTO getMonProfil() {
        ProfilDTO agent = profilRepository.getAgentByMatricule(matriculeStatique);

        if (agent == null) {
            ProfilDTO erreur = new ProfilDTO();
            erreur.setMatricule(matriculeStatique);
            erreur.setEstChef(false);
            erreur.setMessage("Erreur : Agent introuvable ou inactif.");
            return erreur;
        }

         boolean estChef = profilRepository.estChefDeService(matriculeStatique);

        if (estChef) {
            agent.setEstChef(true);
            agent.setMessage("Accès autorisé : Vous êtes Chef de Service.");
        } else {
            agent.setEstChef(false);
            agent.setMessage("Accès refusé : Vous n'avez pas les privilèges requis.");
        }

        return agent;
    }
}