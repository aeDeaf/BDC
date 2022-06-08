package ru.spbu.phys.bdc.runner.service.settings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ru.spbu.phys.bdc.api.model.settings.ConfigurationParameter;
import ru.spbu.phys.bdc.api.model.settings.ModuleParameters;

@Slf4j
@Service
public class SettingsService {
    private static final String PARAMETER_URL = "http://localhost:8081/configuration";
    private static final String MODULES_URL = "http://localhost:8081/configuration/module";

    private final RestTemplate restTemplate;

    @Autowired
    public SettingsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ConfigurationParameter getParameterByKey(String key) {
        String url = PARAMETER_URL + "/" + key;
        try {
            return restTemplate.getForObject(url, ConfigurationParameter.class);
        } catch (ResourceAccessException e) {
            try {
                Thread.sleep(3000);
                return restTemplate.getForObject(url, ConfigurationParameter.class);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

        }
    }

    public ModuleParameters getModuleParametersByKey(String moduleName) {
        String url = MODULES_URL + "/" + moduleName;
        return restTemplate.getForObject(url, ModuleParameters.class);
    }

    public void saveParameter(ConfigurationParameter parameter) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(parameter, headers);
        var response = restTemplate.exchange(PARAMETER_URL, HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Can't save parameter with key {} and value {}", parameter.key(), parameter.value());
        }
    }
}
