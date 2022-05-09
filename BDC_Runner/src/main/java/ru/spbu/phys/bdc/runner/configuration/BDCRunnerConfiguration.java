package ru.spbu.phys.bdc.runner.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.spbu.phys.bdc.runner.model.configuration.ConfigurationParameter;
import ru.spbu.phys.bdc.runner.model.configuration.Properties;
import ru.spbu.phys.bdc.runner.repository.ConfigurationRepository;
import ru.spbu.phys.bdc.runner.service.registration.RegistrationService;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class BDCRunnerConfiguration {
    private static final String NODE_NAME_CONFIGURATION_KEY = "NODE_NAME";

    private final ConfigurationRepository configurationRepository;
    private final RegistrationService registrationService;

    @Autowired
    public BDCRunnerConfiguration(ConfigurationRepository configurationRepository, RegistrationService registrationService) {
        this.configurationRepository = configurationRepository;
        this.registrationService = registrationService;
    }

    @Bean
    @Profile("!centrum")
    public Properties createPropertiesBean() {
        String nodeName = configurationRepository.findParameterByKey(NODE_NAME_CONFIGURATION_KEY)
                .map(ConfigurationParameter::value)
                .orElseGet(() -> {
                    log.info("Node name is empty, register node in registration service");
                    String name = registrationService.registerNode().getNodeName();
                    log.info("Get new node name: {}", name);
                    configurationRepository.saveParameter(new ConfigurationParameter(NODE_NAME_CONFIGURATION_KEY, name));
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

    @Bean
    @Profile("debug")
    public DataSource psqlDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/bdc_runner");
        dataSource.setUsername("postgres");
        dataSource.setPassword("postgres");
        return dataSource;
    }
}
