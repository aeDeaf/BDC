package ru.spbu.phys.cph.bdc.taskmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.spbu.phys.bdc.api.model.registration.NodesList;
import ru.spbu.phys.cph.bdc.taskmanager.service.registration.RegistrationService;

@CrossOrigin
@RestController
@RequestMapping("/nodes")
public class NodeStatusesController {
    private final RegistrationService registrationService;

    @Autowired
    public NodeStatusesController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    private ResponseEntity<NodesList> getNodes() {
        var nodes = registrationService.getNodes();
        return new ResponseEntity<>(new NodesList(nodes), HttpStatus.OK);
    }
}
