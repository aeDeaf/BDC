package ru.spbu.phys.bdc.runner.service;

import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;

public interface TaskObserver {
    void processCommand(RunnerCommand command);
}
