package mg.cnaps.modele.service;

import mg.cnaps.modele.dto.ServiceDTO;
import mg.cnaps.modele.repository.ServiceRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceDTO> getListeServices() {
        return serviceRepository.findAll();
    }
}