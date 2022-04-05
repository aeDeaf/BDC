package ru.spbu.phys.bdc.runner.model.cli;

import lombok.Getter;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Getter
public class SendCommand {
    private final Long timestamp;
    private final Command command;
    private final CompletableFuture<String> listener;

    public SendCommand(Command command) {
        this.timestamp = Instant.now().toEpochMilli();
        this.command = command;
        this.listener = new CompletableFuture<>();
    }

    public String getCommandString() {
        return String.join(" ", command.build());
    }
}
