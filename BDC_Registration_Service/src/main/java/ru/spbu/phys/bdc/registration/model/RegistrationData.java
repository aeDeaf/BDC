package ru.spbu.phys.bdc.registration.model;

import lombok.Builder;
import lombok.Data;
import ru.spbu.phys.bdc.api.model.registration.Status;

@Data
@Builder
public class RegistrationData {
    private final String nodeName;
    @Builder.Default
    private String username = null;

    private Status status;
}
