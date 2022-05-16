package ru.spbu.phys.cph.bdc.taskmanager.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.spbu.phys.cph.bdc.taskmanager.service.user.provider.LocalUserProvider;
import ru.spbu.phys.cph.bdc.taskmanager.service.user.provider.UserProvider;

@Configuration
public class ProfileConfiguration {
    @Bean
    @Profile("!centrum")
    public UserProvider localUserProvider() {
        return new LocalUserProvider();
    }
}
