package ru.spbu.phys.cph.bdc.taskmanager.service.user.provider;

import ru.spbu.phys.cph.bdc.taskmanager.model.user.Role;
import ru.spbu.phys.cph.bdc.taskmanager.model.user.User;

public class LocalUserProvider implements UserProvider {
    @Override
    public User getCurrentUser() {
        return new User("user", "password", Role.LOCAL_USER);
    }
}
