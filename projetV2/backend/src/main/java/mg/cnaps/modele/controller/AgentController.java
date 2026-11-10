package mg.cnaps.modele.controller;
import mg.cnaps.modele.dto.AgentDetailDTO;
import mg.cnaps.modele.service.AgentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * GET /api/agents/service/{codeService}/details
     * Liste plate : agent + fonction + section + application + programme.
     */
    @GetMapping("/service/{codeService}/details")
    public ResponseEntity<List<AgentDetailDTO>> listerDetails(
            @PathVariable String codeService) {
        return ResponseEntity.ok(agentService.listerDetailsParService(codeService));
    }
}