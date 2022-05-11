package ru.spbu.phys.cph.bdc.taskmanager.service.registration;

import lombok.extern.slf4j.Slf4j;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;
import ru.spbu.phys.cph.bdc.taskmanager.model.user.User;

import java.util.List;

@Slf4j
public class NOPAutoRegistrationService implements AutoRegistrationService {
    @Override
    public void register(List<RegistrationDataDTO> nodes, User user) {
        log.info("No need to auto registration in centrum profile");
    }
}
