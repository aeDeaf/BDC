package ru.spbu.phys.bdc.registration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.spbu.phys.bdc.api.model.registration.NodesList;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;
import ru.spbu.phys.bdc.registration.service.RegistrationService;

@RestController
@RequestMapping("/registration")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PutMapping("/node")
    private ResponseEntity<RegistrationDataDTO> getNewRegistrationData() {
        return new ResponseEntity<>(registrationService.createNewNode(), HttpStatus.ACCEPTED);
    }

    @PostMapping("/node")
    private ResponseEntity<String> registerService(@RequestBody RegistrationDataDTO registrationDataDTO) {
        registrationService.registerService(registrationDataDTO.getNodeName(), registrationDataDTO.getUsername());
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping("/node")
    private ResponseEntity<NodesList> getNodes() {
        var data = registrationService.getNodes();
        return new ResponseEntity<>(new NodesList(data), HttpStatus.OK);
    }

    @GetMapping("/node/{username}")
    private ResponseEntity<NodesList> getNodesByUsername(@PathVariable String username) {
        var data = registrationService.getNodesByUsername(username);
        return new ResponseEntity<>(new NodesList(data), HttpStatus.OK);
    }
}
