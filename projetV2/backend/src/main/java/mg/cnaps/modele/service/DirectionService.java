package mg.cnaps.modele.service;
import mg.cnaps.modele.dto.DirectionDTO;
import mg.cnaps.modele.repository.DirectionRepository;

import java.util.List;

import org.springframework.stereotype.Service;

import mg.cnaps.modele.dto.ServiceDTO;

@Service
public class DirectionService {

    private final DirectionRepository directionRepository;

    public DirectionService(DirectionRepository directionRepository) {
        this.directionRepository = directionRepository;
    }

    public List<DirectionDTO> listerDirections() {
        return directionRepository.findAll();
    }

    public List<ServiceDTO> listerServicesParDirection(String codeDirection) {
        if (codeDirection == null || codeDirection.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code direction est obligatoire");
        }
        return directionRepository.findServicesByDirection(codeDirection.trim());
    }
}
