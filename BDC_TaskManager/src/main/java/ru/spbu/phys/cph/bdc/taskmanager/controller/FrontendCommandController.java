package ru.spbu.phys.cph.bdc.taskmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.spbu.phys.cph.bdc.taskmanager.model.command.FrontendCommand;
import ru.spbu.phys.cph.bdc.taskmanager.service.command.FrontendCommandService;

@RestController
@RequestMapping("/frontend")
public class FrontendCommandController {
    private final FrontendCommandService commandService;

    @Autowired
    public FrontendCommandController(FrontendCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    private ResponseEntity<String> processFrontendCommand(@RequestBody FrontendCommand command) {
        commandService.processCommand(command);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
