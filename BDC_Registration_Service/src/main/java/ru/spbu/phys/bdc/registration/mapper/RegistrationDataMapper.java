package ru.spbu.phys.bdc.registration.mapper;

import org.springframework.stereotype.Component;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;
import ru.spbu.phys.bdc.registration.model.RegistrationData;

@Component
public class RegistrationDataMapper {
    public RegistrationDataDTO map(RegistrationData data) {
        return RegistrationDataDTO
                .builder()
                .nodeName(data.getNodeName())
                .username(data.getUsername())
                .build();
    }
}
