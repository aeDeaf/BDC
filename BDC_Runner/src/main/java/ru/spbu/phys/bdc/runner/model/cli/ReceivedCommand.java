package ru.spbu.phys.bdc.runner.model.cli;

import lombok.Getter;

@Getter
public class ReceivedCommand {
    private Long initialMessageTimestamp;
    private Long sendTimestamp;
    private String result;
}
