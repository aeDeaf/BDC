package ru.spbu.phys.cph.bdc.taskmanager.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.spbu.phys.bdc.api.model.CommonResponse;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;
import ru.spbu.phys.bdc.api.model.result.CommandExecuteStatusMessage;
import ru.spbu.phys.cph.bdc.taskmanager.service.command.RunnerCommandQueueService;

@Slf4j
@RestController
@RequestMapping("/command")
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

    @PostMapping("/status")
    private ResponseEntity<CommonResponse> processCommandStatus(@RequestBody CommandExecuteStatusMessage message) {
        log.info("Received status message for task {} with status {}", message.getTaskName(), message.getStatus());
        return new ResponseEntity<>(new CommonResponse(), HttpStatus.OK);
    }
}
