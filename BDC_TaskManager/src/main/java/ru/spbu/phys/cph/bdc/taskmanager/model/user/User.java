package ru.spbu.phys.cph.bdc.taskmanager.model.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;

import java.util.HashSet;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public class User {
    private final String username;
    private final String password;
    private final Role role;
    private final Set<RegistrationDataDTO> nodes = new HashSet<>();
}
