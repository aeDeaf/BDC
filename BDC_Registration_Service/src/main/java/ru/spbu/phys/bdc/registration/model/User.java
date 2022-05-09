package ru.spbu.phys.bdc.registration.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private final String username;
}
