package mg.cnaps.modele.controller;

import mg.cnaps.modele.dto.ServiceDTO;
import mg.cnaps.modele.service.ServiceService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping("/liste")
    public ResponseEntity<List<ServiceDTO>> getListeServices() {
        List<ServiceDTO> services = serviceService.getListeServices();
        return ResponseEntity.ok(services);
    }
}