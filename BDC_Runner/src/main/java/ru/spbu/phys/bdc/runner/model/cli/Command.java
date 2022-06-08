package ru.spbu.phys.bdc.runner.model.cli;

import java.util.ArrayList;
import java.util.List;

public class Command {
    public static Command createCommand() {
        Command command = new Command();
        boolean isLinux = !System.getProperty("os.name").toLowerCase().startsWith("windows");
        if (!isLinux) {
            command = command.addCommand("cmd.exe");
        }
        return command;
    }

    private Command() {
    }

    private final List<String> commands = new ArrayList<>();

    public Command addCommand(String command) {
        commands.add(command);
        return this;
    }

    public List<String> build() {
        return commands;
    }

    @Override
    public String toString() {
        return String.join(" ", build());
    }
}
