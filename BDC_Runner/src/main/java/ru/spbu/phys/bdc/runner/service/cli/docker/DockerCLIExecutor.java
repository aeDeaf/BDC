package ru.spbu.phys.bdc.runner.service.cli.docker;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.runner.service.command.CommandsService;
import ru.spbu.phys.bdc.runner.model.cli.Command;

import java.util.concurrent.CompletableFuture;

@Service
public class DockerCLIExecutor {
    private final CommandsService commandsService;

    public static final Command dockerRunCommandTemplate = Command
            .createCommand()
            .addCommand("docker")
            .addCommand("run")
            .addCommand("-d")
            .addCommand("-v")
            .addCommand("/tmp/.X11-unix:/tmp/.X11-unix")
            .addCommand("-v")
            .addCommand("$HOME:/workdir");

    public DockerCLIExecutor(CommandsService commandsService) {
        this.commandsService = commandsService;
    }

    public CompletableFuture<String> runContainer(String imageName, String containerName) {
        Command command = dockerRunCommandTemplate
                .addCommand("--name=" + containerName)
                .addCommand(imageName);
        return exec(command);
    }

    public CompletableFuture<String> pullContainer(String imageName) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("pull")
                .addCommand(imageName);
        return exec(command);
    }

    @SneakyThrows
    public CompletableFuture<String> exec(Command command) {
        return commandsService.addCommandToQueue(command);
    }
}
