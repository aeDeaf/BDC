package ru.spbu.phys.bdc.registration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.spbu.phys.bdc.api.model.registration.NodeStatus;
import ru.spbu.phys.bdc.registration.service.StatusService;

@RestController
@RequestMapping("/status")
public class StatusController {
    private final StatusService statusService;

    @Autowired
    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @PostMapping
    private ResponseEntity<String> setStatus(@RequestBody NodeStatus status) {
        statusService.setStatus(status.nodeName(), status.status());
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PostMapping("/health")
    private ResponseEntity<String> healthCheck(@RequestBody NodeStatus status) {
        statusService.updateTimestamp(status.nodeName());
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
