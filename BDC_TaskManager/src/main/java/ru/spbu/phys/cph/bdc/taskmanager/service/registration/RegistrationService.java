package ru.spbu.phys.cph.bdc.taskmanager.service.registration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.spbu.phys.bdc.api.model.registration.NodesList;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RegistrationService {
    private static final String REGISTRATION_SERVICE_URL = "http://localhost:8095/registration/node";

    private final RestTemplate restTemplate;

    @Autowired
    public RegistrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<RegistrationDataDTO> getNodes() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(REGISTRATION_SERVICE_URL, HttpMethod.GET, entity, NodesList.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            NodesList list = response.getBody();
            if (list != null) {
                return list.nodes();
            } else {
                log.warn("Response body is null!");
                return new ArrayList<>();
            }
        } else {
            log.error("Can't get nodes from registration service");
            return new ArrayList<>();
        }
    }

    public void registerNode(RegistrationDataDTO registrationDataDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(registrationDataDTO, headers);
        var response = restTemplate.exchange(REGISTRATION_SERVICE_URL, HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to register node {} for user {}", registrationDataDTO.getNodeName(), registrationDataDTO.getUsername());
        }
    }
}
