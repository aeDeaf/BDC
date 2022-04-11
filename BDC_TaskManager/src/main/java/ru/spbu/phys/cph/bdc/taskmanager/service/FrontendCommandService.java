package ru.spbu.phys.cph.bdc.taskmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;
import ru.spbu.phys.cph.bdc.taskmanager.model.FrontendCommand;

@Service
public class FrontendCommandService {
    public static final String TASK_NAME_PREFIX = "task";

    private long tasksCounter = 0;

    private final RunnerCommandQueueService queueService;

    @Autowired
    public FrontendCommandService(RunnerCommandQueueService queueService) {
        this.queueService = queueService;
    }

    public void processCommand(FrontendCommand frontendCommand) {
        RunnerCommand command = RunnerCommand
                .builder()
                .commandType(frontendCommand.getCommandType())
                .parameters(frontendCommand.getParameters())
                .taskName(getNextTaskName())
                .build();
        queueService.addCommandToQueue(frontendCommand.getNodeName(), command);
    }

    private synchronized String getNextTaskName() {
        String taskName = TASK_NAME_PREFIX + tasksCounter;
        tasksCounter++;
        return taskName;
    }
}
