package mg.cnaps.modele.service;
import mg.cnaps.modele.dto.AgentDetailDTO;
import mg.cnaps.modele.repository.AgentRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentService {

    private final AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public List<AgentDetailDTO> listerDetailsParService(String codeService) {
        if (codeService == null || codeService.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code service est obligatoire");
        }

        return agentRepository.findDetailsByService(codeService);
    }
}