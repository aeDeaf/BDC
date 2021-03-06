package ru.spbu.phys.bdc.runner.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.spbu.phys.bdc.api.model.CommonResponse;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;
import ru.spbu.phys.bdc.api.model.result.CommandExecuteStatusMessage;
import ru.spbu.phys.bdc.api.model.result.docker.DockerContainersInfos;
import ru.spbu.phys.bdc.runner.model.configuration.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TaskManagerService {
    public static final String STATUS_URL = "http://localhost:8090/command/status";
    public static final String CONTAINER_INFOS_URL = "http://localhost:8090/command/containers";
    public static final String GET_COMMAND_URL = "http://127.0.0.1:8090/command";

    private final Properties properties;

    private final RestTemplate restTemplate;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final List<TaskObserver> observers = new ArrayList<>();

    public TaskManagerService(Properties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @EventListener
    public void init(ContextRefreshedEvent event) {
        executorService.scheduleAtFixedRate(this::getNewCommand, 0, properties.getRequestsDelta(),
                TimeUnit.MILLISECONDS);
    }

    private void getNewCommand() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            String url = GET_COMMAND_URL + "/" + properties.getNodeName();
            ResponseEntity<RunnerCommand> response = restTemplate.exchange(url, HttpMethod.GET, entity, RunnerCommand.class);
            RunnerCommand command = response.getBody();
            if (command != null && command.getCommandType() != null && command.getTaskName() != null) {
                log.info("Received new command {}", command.getCommandType());
                observers
                        .forEach(observer -> observer.processCommand(command));
            }
        } catch (ResourceAccessException e) {
            log.warn("Can't access task manager");
        }
    }

    public void addObserver(TaskObserver observer) {
        observers.add(observer);
    }

    public void sendStatus(CommandExecuteStatusMessage message) {
        CommonResponse response = restTemplate.postForObject(STATUS_URL, message, CommonResponse.class);
        if (response != null) {
            System.out.println(response.getMessage());
        }
    }

    public void sendContainerInfos(DockerContainersInfos containersInfos) {
        CommonResponse response = restTemplate.postForObject(CONTAINER_INFOS_URL, containersInfos, CommonResponse.class);
        if (response != null) {
            System.out.println(response.getMessage());
        }
    }
}
