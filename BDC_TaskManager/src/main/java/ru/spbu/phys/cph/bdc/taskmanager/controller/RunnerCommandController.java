package ru.spbu.phys.cph.bdc.taskmanager.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.spbu.phys.bdc.api.model.CommonResponse;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;
import ru.spbu.phys.bdc.api.model.result.CommandExecuteStatusMessage;
import ru.spbu.phys.bdc.api.model.result.docker.DockerContainersInfos;
import ru.spbu.phys.cph.bdc.taskmanager.service.command.FrontendMessageQueueService;
import ru.spbu.phys.cph.bdc.taskmanager.service.command.RunnerCommandQueueService;

@Slf4j
@RestController
@RequestMapping("/command")
public class RunnerCommandController {
    private final RunnerCommandQueueService queueService;
    private final FrontendMessageQueueService frontendMessageQueueService;

    @Autowired
    public RunnerCommandController(RunnerCommandQueueService queueService, FrontendMessageQueueService frontendMessageQueueService) {
        this.queueService = queueService;
        this.frontendMessageQueueService = frontendMessageQueueService;
    }

    @GetMapping("/{nodeName}")
    private ResponseEntity<RunnerCommand> getCommandFromQueue(@PathVariable String nodeName) {
        RunnerCommand command = queueService.getCommandFromQueue(nodeName);
        return new ResponseEntity<>(command, HttpStatus.OK);
    }

    @PostMapping("/status")
    private ResponseEntity<CommonResponse> processCommandStatus(@RequestBody CommandExecuteStatusMessage message) {
        log.info("Received status message for task {} with status {}", message.getTaskName(), message.getStatus());
        frontendMessageQueueService.addStatusMessageToQueue(message);
        return new ResponseEntity<>(new CommonResponse(), HttpStatus.OK);
    }

    @PostMapping("/containers")
    private ResponseEntity<CommonResponse> processDockerContainerInfos(@RequestBody DockerContainersInfos infos) {
        log.info("Received docker containers infos message");
        frontendMessageQueueService.addContainerInfosToQueue(infos);
        return new ResponseEntity<>(new CommonResponse(), HttpStatus.OK);
    }
}
