package ru.spbu.phys.bdc.runner.model.cli;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandMessage {
    private final Long sendTimestamp;
    private final String command;
}
