package ru.spbu.phys.bdc.runner.service.cli.docker;

import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.executor.CommandType;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;
import ru.spbu.phys.bdc.api.model.result.CommandExecuteStatus;
import ru.spbu.phys.bdc.api.model.result.CommandExecuteStatusMessage;
import ru.spbu.phys.bdc.runner.model.configuration.Properties;
import ru.spbu.phys.bdc.runner.service.TaskManagerService;
import ru.spbu.phys.bdc.runner.service.TaskObserver;

import javax.annotation.PostConstruct;

@Service
public class BmnDockerService implements TaskObserver {
    public static final String IMAGE_NAME_PARAMETER_NAME = "imageName";

    private final DockerCLIExecutor executor;
    private final TaskManagerService taskManagerService;
    private final Properties properties;

    public BmnDockerService(DockerCLIExecutor executor, TaskManagerService taskManagerService, Properties properties) {
        this.executor = executor;
        this.taskManagerService = taskManagerService;
        this.properties = properties;
    }

    @PostConstruct
    private void init() {
        taskManagerService.addObserver(this);
    }

    @Override
    public void processCommand(RunnerCommand command) {
        if (command.getCommandType() == CommandType.PULL_IMAGE) {
            pullContainer(command);
        }
    }

    private void pullContainer(RunnerCommand command) {
        String imageName = command.getParameter(IMAGE_NAME_PARAMETER_NAME);
        executor.pullContainer(imageName)
                .thenAccept(result -> {
                    CommandExecuteStatusMessage message = CommandExecuteStatusMessage
                            .builder()
                            .taskName(command.getTaskName())
                            .nodeName(properties.getNodeName())
                            .status(CommandExecuteStatus.SUCCESS)
                            .message(result)
                            .build();
                    taskManagerService.sendStatus(message);
                });
    }
}
