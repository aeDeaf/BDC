package ru.spbu.phys.bdc.runner.service.command;

import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.runner.model.cli.Command;
import ru.spbu.phys.bdc.runner.model.cli.ReceivedCommand;
import ru.spbu.phys.bdc.runner.model.cli.SendCommand;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Service
public class CommandsService {
    private final Queue<SendCommand> commandsQueue = new ArrayDeque<>();

    private final Map<Long, CompletableFuture<String>> commandsResults = new HashMap<>();

    public synchronized CompletableFuture<String> addCommandToQueue(Command command) {
        SendCommand sendCommand = new SendCommand(command);
        commandsQueue.add(sendCommand);
        return sendCommand.getListener();
    }

    public SendCommand getCommandFromQueue() {
        SendCommand command = commandsQueue.poll();
        if (command != null) {
            commandsResults.put(command.getTimestamp(), command.getListener());
            return command;
        } else {
            return null;
        }
    }

    public synchronized void processReceivedCommand(ReceivedCommand receivedCommand) {
        CompletableFuture<String> listener = commandsResults.get(receivedCommand.getInitialMessageTimestamp());
        if (listener != null) {
            listener.complete(receivedCommand.getResult());
        }
    }
}
