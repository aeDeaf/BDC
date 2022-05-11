package ru.spbu.phys.cph.bdc.taskmanager.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.spbu.phys.cph.bdc.taskmanager.service.registration.AutoRegistrationService;
import ru.spbu.phys.cph.bdc.taskmanager.service.registration.AutoRegistrationServiceImpl;
import ru.spbu.phys.cph.bdc.taskmanager.service.registration.NOPAutoRegistrationService;
import ru.spbu.phys.cph.bdc.taskmanager.service.registration.RegistrationService;
import ru.spbu.phys.cph.bdc.taskmanager.service.user.provider.LocalUserProvider;
import ru.spbu.phys.cph.bdc.taskmanager.service.user.provider.UserProvider;

@Configuration
public class ProfileConfiguration {
    private final RegistrationService registrationService;

    @Autowired
    public ProfileConfiguration(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Bean
    @Profile("!centrum")
    public AutoRegistrationService localAutoRegistrationService() {
        return new AutoRegistrationServiceImpl(registrationService);
    }

    @Bean
    @Profile("centrum")
    public AutoRegistrationService nopAutoRegistrationService() {
        return new NOPAutoRegistrationService();
    }

    @Bean
    @Profile("!centrum")
    public UserProvider localUserProvider() {
        return new LocalUserProvider();
    }
}
