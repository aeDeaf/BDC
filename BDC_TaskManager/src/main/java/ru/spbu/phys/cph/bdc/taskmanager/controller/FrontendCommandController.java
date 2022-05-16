package ru.spbu.phys.cph.bdc.taskmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.spbu.phys.bdc.api.model.result.CommandExecuteStatusMessage;
import ru.spbu.phys.bdc.api.model.result.docker.DockerContainersInfos;
import ru.spbu.phys.cph.bdc.taskmanager.model.command.FrontendCommand;
import ru.spbu.phys.cph.bdc.taskmanager.service.command.FrontendCommandService;
import ru.spbu.phys.cph.bdc.taskmanager.service.command.FrontendMessageQueueService;

@CrossOrigin
@RestController
@RequestMapping("/frontend")
public class FrontendCommandController {
    private final FrontendCommandService commandService;
    private final FrontendMessageQueueService queueService;

    @Autowired
    public FrontendCommandController(FrontendCommandService commandService, FrontendMessageQueueService queueService) {
        this.commandService = commandService;
        this.queueService = queueService;
    }

    @PostMapping
    private ResponseEntity<String> processFrontendCommand(@RequestBody FrontendCommand command) {
        commandService.processCommand(command);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping("/status")
    private ResponseEntity<CommandExecuteStatusMessage> getStatusMessage() {
        return new ResponseEntity<>(queueService.getStatusMessageFormQueue(), HttpStatus.OK);
    }

    @GetMapping("/containers")
    private ResponseEntity<DockerContainersInfos> getContainerInfos() {
        return new ResponseEntity<>(queueService.getContainerInfosFromQueue(), HttpStatus.OK);
    }
}
