package ru.spbu.phys.bdc.registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;
import ru.spbu.phys.bdc.api.model.registration.Status;
import ru.spbu.phys.bdc.registration.mapper.RegistrationDataMapper;
import ru.spbu.phys.bdc.registration.model.RegistrationData;
import ru.spbu.phys.bdc.registration.repository.RegistrationDataRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RegistrationService {
    private static final String NODE_NAME_TEMPLATE = "node%d";

    private final RegistrationDataRepository registrationDataRepository;
    private final RegistrationDataMapper registrationDataMapper;
    private final StatusService statusService;

    public RegistrationService(RegistrationDataRepository registrationDataRepository, RegistrationDataMapper registrationDataMapper, StatusService statusService) {
        this.registrationDataRepository = registrationDataRepository;
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
        RegistrationData registrationData = RegistrationData
                .builder()
                .nodeName(nodeName)
                .username(username)
                .build();
        registrationDataRepository.saveRegistrationData(registrationData);
    }

    public List<RegistrationDataDTO> getNodesByUsername(String username) {
        return registrationDataRepository.findNodesByUsername(username)
                .stream()
                .map(registrationDataMapper::map)
                .collect(Collectors.toList());
    }

    public List<RegistrationDataDTO> getNodes() {
        return registrationDataRepository.findNodes()
                .stream()
                .map(registrationDataMapper::map)
                .collect(Collectors.toList());
    }
}
