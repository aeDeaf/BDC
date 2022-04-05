package ru.spbu.phys.bdc.runner.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.client.RestTemplate;
import ru.spbu.phys.bdc.runner.model.configuration.Properties;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
public class BDCRunnerConfiguration {
    @Bean
    @Profile("!centrum")
    public Properties createPropertiesBean() {
        return Properties
                .builder()
                .nodeName("node1")
                .requestsDelta(3000)
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

    @Bean
    public RestTemplate createRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(1000))
                .setReadTimeout(Duration.ofMillis(1000))
                .build();
    }
}
