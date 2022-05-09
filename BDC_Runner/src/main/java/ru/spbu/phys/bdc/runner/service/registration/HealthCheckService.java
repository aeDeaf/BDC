package ru.spbu.phys.bdc.runner.service.registration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.spbu.phys.bdc.api.model.registration.NodeStatus;
import ru.spbu.phys.bdc.runner.model.configuration.Properties;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class HealthCheckService {
    private static final String HEALTH_CHECK_URL = "http://localhost:8095/status/health";

    private final Properties properties;
    private final RestTemplate restTemplate;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public HealthCheckService(Properties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void init() {
        executorService.scheduleWithFixedDelay(this::healthCheck, 0, properties.getHealthCheckDelta(), TimeUnit.MILLISECONDS);
    }

    private void healthCheck() {
        NodeStatus status = new NodeStatus(properties.getNodeName(), null, null);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<NodeStatus> entity = new HttpEntity<>(status, headers);
        var response = restTemplate.exchange(HEALTH_CHECK_URL, HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Failed to send health check");
        }
    }
}
