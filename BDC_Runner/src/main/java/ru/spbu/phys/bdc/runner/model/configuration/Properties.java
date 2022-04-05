package ru.spbu.phys.bdc.runner.model.configuration;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Properties {
    private String nodeName;

    private int requestsDelta;
}
