package ru.spbu.phys.bdc.api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommonResponse {
    private String message;

    public String getMessage() {
        return message;
    }
}
