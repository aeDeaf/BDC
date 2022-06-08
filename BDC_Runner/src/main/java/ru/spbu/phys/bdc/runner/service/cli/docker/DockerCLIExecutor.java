package ru.spbu.phys.bdc.runner.service.cli.docker;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.runner.model.cli.Command;
import ru.spbu.phys.bdc.runner.service.command.CommandsService;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class DockerCLIExecutor {
    private final CommandsService commandsService;

    public static Command getDockerRunCommandTemplate() {
        return Command
                .createCommand()
                .addCommand("docker")
                .addCommand("run")
                .addCommand("-d")
                .addCommand("-v")
                .addCommand("/tmp/.X11-unix:/tmp/.X11-unix")
                .addCommand("-v")
                .addCommand("$HOME:/workdir");
    }

    public DockerCLIExecutor(CommandsService commandsService) {
        this.commandsService = commandsService;
    }

    public CompletableFuture<String> runContainer(String imageName, String containerName) {
        Command command = getDockerRunCommandTemplate()
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

    public CompletableFuture<String> getContainerUsernameAndPassword(String containerName) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("logs")
                .addCommand(containerName);
        return exec(command);
    }

    public CompletableFuture<String> stopContainer(String containerName) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("stop")
                .addCommand(containerName);
        return exec(command);
    }

    public CompletableFuture<String> startContainer(String containerName) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("start")
                .addCommand(containerName);
        return exec(command);
    }

    public CompletableFuture<String> getContainerIP(String containerName) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("inspect")
                .addCommand(containerName)
                .addCommand("|")
                .addCommand("grep")
                .addCommand("IPAddress");
        return exec(command);
    }

    public CompletableFuture<String> getDisplayValue() {
        Command command = Command
                .createCommand()
                .addCommand("echo")
                .addCommand("$DISPLAY");
        return exec(command);
    }

    public void showTerminalWindow(String executeCommand) {
        Command command = Command
                .createCommand()
                .addCommand("gnome-terminal")
                .addCommand("--")
                .addCommand(executeCommand);
        exec(command);
    }

    public CompletableFuture<String> dockerPS(String containerNamePrefix) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("ps")
                .addCommand("--all")
                .addCommand("|")
                .addCommand("grep")
                .addCommand("\"" + containerNamePrefix + "*\"");
        return exec(command);
    }

    public CompletableFuture<String> copyBackupDataToHost(String containerName, String containerPath, String hostPath) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("cp")
                .addCommand(containerName + ":" + containerPath)
                .addCommand(hostPath);
        return exec(command);
    }

    public CompletableFuture<String> copyBackupDataToContainer(String containerName, String containerPath, String hostPath) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("cp")
                .addCommand(hostPath)
                .addCommand(containerName + ":" + containerPath);
        return exec(command);
    }

    public CompletableFuture<String> removeContainer(String containerName) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("rm")
                .addCommand(containerName);
        return exec(command);
    }

    public CompletableFuture<String> removeImage(String imageName) {
        Command command = Command
                .createCommand()
                .addCommand("docker")
                .addCommand("image")
                .addCommand("rm")
                .addCommand(imageName);
        var res = new CompletableFuture<String>();
        res.complete("");
        return res;
    }

    @SneakyThrows
    public CompletableFuture<String> exec(Command command) {
        log.info("Executing command: {}", command.toString());
        return commandsService.addCommandToQueue(command);
    }
}
