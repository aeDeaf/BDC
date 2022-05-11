package ru.spbu.phys.cph.bdc.taskmanager.service.registration;

import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;
import ru.spbu.phys.cph.bdc.taskmanager.model.user.User;

import java.util.List;

public interface AutoRegistrationService {
    void register(List<RegistrationDataDTO> nodes, User user);
}
