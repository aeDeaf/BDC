package ru.spbu.phys.bdc.registration.service;

import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.registration.NodeStatus;
import ru.spbu.phys.bdc.api.model.registration.Status;
import ru.spbu.phys.bdc.registration.configuration.RegistrationServiceConfiguration;
import ru.spbu.phys.bdc.registration.repository.StatusRepository;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class StatusService {
    private final StatusRepository statusRepository;
    private final RegistrationServiceConfiguration configuration;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public StatusService(StatusRepository statusRepository, RegistrationServiceConfiguration configuration) {
        this.statusRepository = statusRepository;
        this.configuration = configuration;
    }

    @PostConstruct
    private void init() {
        executorService.scheduleAtFixedRate(this::healthCheck, 0, configuration.getHealthCheckPeriod(), TimeUnit.SECONDS);
    }

    private void healthCheck() {
        var statuses = statusRepository.findStatuses();
        for (var status : statuses) {
            Instant timestamp = status.timestamp();
            if ((timestamp.getEpochSecond() + configuration.getHealthCheckPeriod() - Instant.now().getEpochSecond()) < 0) {
                setStatus(status.nodeName(), Status.OFFLINE);
            }
        }
    }

    public void setStatus(String nodeName, Status status) {
        NodeStatus nodeStatus = new NodeStatus(nodeName, status, Instant.now());
        statusRepository.updateStatus(nodeStatus);
    }

    public void updateTimestamp(String nodeName) {
        NodeStatus status = new NodeStatus(nodeName, Status.ONLINE, Instant.now());
        statusRepository.updateTimestamp(status);
    }
}
