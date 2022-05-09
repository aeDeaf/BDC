package ru.spbu.phys.bdc.registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;
import ru.spbu.phys.bdc.api.model.registration.Status;
import ru.spbu.phys.bdc.registration.mapper.RegistrationDataMapper;
import ru.spbu.phys.bdc.registration.model.RegistrationData;
import ru.spbu.phys.bdc.registration.repository.RegistrationDataRepository;
import ru.spbu.phys.bdc.registration.repository.UserRepository;

@Slf4j
@Service
public class RegistrationService {
    private static final String NODE_NAME_TEMPLATE = "node%d";

    private final RegistrationDataRepository registrationDataRepository;
    private final UserRepository userRepository;
    private final RegistrationDataMapper registrationDataMapper;
    private final StatusService statusService;

    public RegistrationService(RegistrationDataRepository registrationDataRepository, UserRepository userRepository, RegistrationDataMapper registrationDataMapper, StatusService statusService) {
        this.registrationDataRepository = registrationDataRepository;
        this.userRepository = userRepository;
        this.registrationDataMapper = registrationDataMapper;
        this.statusService = statusService;
    }

    public RegistrationDataDTO createNewNode() {
        Long newNodeId = registrationDataRepository.getMaxNodeId().orElse(0L) + 1L;
        String nodeName = String.format(NODE_NAME_TEMPLATE, newNodeId);
        RegistrationData registrationData = RegistrationData
                .builder()
                .nodeName(nodeName)
                .build();
        if (registrationDataRepository.saveRegistrationData(registrationData) > 0) {
            log.info("Successfully saved new node registration data");
            statusService.setStatus(registrationData.getNodeName(), Status.OFFLINE);
            return registrationDataMapper.map(registrationData);
        } else {
            log.warn("Can't save registration data");
            return null;
        }
    }

    public void registerService(String nodeName, String username) {
        var user = userRepository.getUser(username);
        if (user.isEmpty()) {
            log.error("Can't find user with username {}", username);
            throw new RuntimeException("Can't find user with username " + username);
        }
        RegistrationData registrationData = RegistrationData
                .builder()
                .nodeName(nodeName)
                .user(user.get())
                .build();
        registrationDataRepository.saveRegistrationData(registrationData);
    }
}
