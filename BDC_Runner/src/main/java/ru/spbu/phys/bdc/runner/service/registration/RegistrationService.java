package ru.spbu.phys.bdc.runner.service.registration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;

@Slf4j
@Service
public class RegistrationService {
    private static final String REGISTRATION_URL = "http://localhost:8095/registration/node";

    private final RestTemplate restTemplate;

    @Autowired
    public RegistrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RegistrationDataDTO registerNode() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(REGISTRATION_URL, HttpMethod.PUT, entity, RegistrationDataDTO.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            log.error("Can't register service");
            throw new RuntimeException("Can't register service");
        }
    }
}
