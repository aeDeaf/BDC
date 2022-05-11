package ru.spbu.phys.cph.bdc.taskmanager.service.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;
import ru.spbu.phys.cph.bdc.taskmanager.service.user.provider.UserProvider;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class AutoRegistrationProcessor {
    private final AutoRegistrationService autoRegistrationService;
    private final UserProvider userProvider;
    private final RegistrationService registrationService;

    @Autowired
    public AutoRegistrationProcessor(AutoRegistrationService autoRegistrationService, UserProvider userProvider, RegistrationService registrationService) {
        this.autoRegistrationService = autoRegistrationService;
        this.userProvider = userProvider;
        this.registrationService = registrationService;
    }

    @PostConstruct
    private void init() {
        List<RegistrationDataDTO> nodes = registrationService.getNodes()
                .stream()
                .filter(node -> node.getUsername() == null)
                .toList();
        autoRegistrationService.register(nodes, userProvider.getCurrentUser());
    }
}
