package ru.spbu.phys.bdc.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationData {
    private final String nodeName;
    @Builder.Default
    private User user = null;

    @JsonIgnore
    public String getUsername() {
        if (user == null) {
            return null;
        }
        return user.getUsername();
    }
}
