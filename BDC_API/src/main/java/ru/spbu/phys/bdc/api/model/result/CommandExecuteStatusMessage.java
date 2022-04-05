package ru.spbu.phys.bdc.api.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommandExecuteStatusMessage {
    private CommandExecuteStatus status;
    private String taskName;
    private String nodeName;
    private String message;
}
