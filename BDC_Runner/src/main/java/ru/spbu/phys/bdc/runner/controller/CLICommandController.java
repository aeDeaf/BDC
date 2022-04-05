package ru.spbu.phys.bdc.runner.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.spbu.phys.bdc.runner.model.cli.CommandMessage;
import ru.spbu.phys.bdc.runner.model.cli.SendCommand;
import ru.spbu.phys.bdc.runner.service.command.CommandsService;
import ru.spbu.phys.bdc.runner.model.cli.ReceivedCommand;

@RestController
@RequestMapping("/command")
public class CLICommandController {
    private final CommandsService commandsService;

    public CLICommandController(CommandsService commandsService) {
        this.commandsService = commandsService;
    }

    @GetMapping
    private ResponseEntity<CommandMessage> getCommand() {
        SendCommand sendCommand = commandsService.getCommandFromQueue();
        if (sendCommand != null) {
            CommandMessage message = new CommandMessage(sendCommand.getTimestamp(), sendCommand.getCommandString());
            return new ResponseEntity<>(message, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

    }

    @PostMapping
    private void receiveCommand(@RequestBody ReceivedCommand receivedCommand) {
        commandsService.processReceivedCommand(receivedCommand);
    }


}
