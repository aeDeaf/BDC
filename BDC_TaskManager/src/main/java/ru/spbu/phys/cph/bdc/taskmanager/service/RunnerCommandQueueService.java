package ru.spbu.phys.cph.bdc.taskmanager.service;

import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@Service
public class RunnerCommandQueueService {
    private final Map<String, Queue<RunnerCommand>> runnerCommandQueue = new HashMap<>();

    public RunnerCommand getCommandFromQueue(String nodeName) {
        return runnerCommandQueue.get(nodeName).poll();
    }

    public void addCommandToQueue(String nodeName, RunnerCommand command) {
        if (!runnerCommandQueue.containsKey(nodeName)) {
            runnerCommandQueue.put(nodeName, new ArrayDeque<>());
        }
        runnerCommandQueue.get(nodeName).add(command);
    }
}
