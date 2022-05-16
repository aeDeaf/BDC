package ru.spbu.phys.bdc.runner.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.spbu.phys.bdc.api.model.settings.ConfigurationParameter;
import ru.spbu.phys.bdc.runner.model.configuration.Properties;
import ru.spbu.phys.bdc.runner.service.registration.RegistrationService;
import ru.spbu.phys.bdc.runner.service.settings.SettingsService;

import java.util.Optional;

@Slf4j
@Configuration
public class BDCRunnerConfiguration {
    private static final String NODE_NAME_CONFIGURATION_KEY = "NODE_NAME";

    private final SettingsService settingsService;
    private final RegistrationService registrationService;

    @Autowired
    public BDCRunnerConfiguration(SettingsService settingsService, RegistrationService registrationService) {
        this.settingsService = settingsService;
        this.registrationService = registrationService;
    }

    @Bean
    @Profile("!centrum")
    public Properties createPropertiesBean() {
        String nodeName = Optional.ofNullable(settingsService.getParameterByKey(NODE_NAME_CONFIGURATION_KEY).value())
                .orElseGet(() -> {
                    log.info("Node name is empty, register node in registration service");
                    String name = registrationService.registerNode().getNodeName();
                    log.info("Get new node name: {}", name);
                    settingsService.saveParameter(new ConfigurationParameter(NODE_NAME_CONFIGURATION_KEY, name));
                    return name;
                });
        log.info("Current node name: {}", nodeName);
        return Properties
                .builder()
                .nodeName(nodeName)
                .requestsDelta(3000)
                .healthCheckDelta(3000)
                .build();
    }
}
