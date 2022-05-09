package ru.spbu.phys.bdc.api.model.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDataDTO {
    private String nodeName;
    private String username;
}
