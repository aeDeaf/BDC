package ru.spbu.phys.cph.bdc.taskmanager.service.registration;

import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;
import ru.spbu.phys.cph.bdc.taskmanager.model.user.User;

import java.util.List;

public class AutoRegistrationServiceImpl implements AutoRegistrationService {
    private final RegistrationService registrationService;

    public AutoRegistrationServiceImpl(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Override
    public void register(List<RegistrationDataDTO> nodes, User user) {
        nodes
                .stream()
                .map(node -> RegistrationDataDTO
                        .builder()
                        .nodeName(node.getNodeName())
                        .username(user.getUsername())
                        .build()
                )
                .forEach(registrationService::registerNode);
    }
}
