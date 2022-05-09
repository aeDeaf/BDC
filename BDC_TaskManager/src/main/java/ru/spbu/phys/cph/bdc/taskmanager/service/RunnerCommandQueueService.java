package ru.spbu.phys.cph.bdc.taskmanager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@Slf4j
@Service
public class RunnerCommandQueueService {
    private final Map<String, Queue<RunnerCommand>> runnerCommandQueue = new HashMap<>();

    public RunnerCommand getCommandFromQueue(String nodeName) {
        if (runnerCommandQueue.containsKey(nodeName)) {
            return runnerCommandQueue.get(nodeName).poll();
        } else {
            return null;
        }
    }

    public void addCommandToQueue(String nodeName, RunnerCommand command) {
        if (!runnerCommandQueue.containsKey(nodeName)) {
            runnerCommandQueue.put(nodeName, new ArrayDeque<>());
        }
        runnerCommandQueue.get(nodeName).add(command);
        log.info("Command {} putted to queue for node {}", command.getCommandType(), nodeName);
    }
}
