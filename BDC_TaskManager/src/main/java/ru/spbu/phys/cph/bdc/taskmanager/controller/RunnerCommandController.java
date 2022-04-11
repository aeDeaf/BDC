package ru.spbu.phys.cph.bdc.taskmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;
import ru.spbu.phys.cph.bdc.taskmanager.service.RunnerCommandQueueService;

@RestController
@RequestMapping("/controller")
public class RunnerCommandController {
    private final RunnerCommandQueueService queueService;

    @Autowired
    public RunnerCommandController(RunnerCommandQueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/{nodeName}")
    private ResponseEntity<RunnerCommand> getCommandFromQueue(@PathVariable String nodeName) {
        RunnerCommand command = queueService.getCommandFromQueue(nodeName);
        return new ResponseEntity<>(command, HttpStatus.OK);
    }
}
