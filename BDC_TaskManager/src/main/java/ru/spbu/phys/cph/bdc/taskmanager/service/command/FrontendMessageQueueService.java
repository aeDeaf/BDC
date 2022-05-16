package ru.spbu.phys.cph.bdc.taskmanager.service.command;

import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.result.CommandExecuteStatusMessage;
import ru.spbu.phys.bdc.api.model.result.docker.DockerContainersInfos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

@Service
public class FrontendMessageQueueService {
    private final Queue<CommandExecuteStatusMessage> statusMessageQueue = new ArrayDeque<>();
    private final DockerContainersInfos containersInfos = new DockerContainersInfos(new ArrayList<>());

    public void addStatusMessageToQueue(CommandExecuteStatusMessage statusMessage) {
        statusMessageQueue.add(statusMessage);
    }

    public void addContainerInfosToQueue(DockerContainersInfos containersInfos) {
        synchronized (this.containersInfos) {
            this.containersInfos.getDockerContainerInfos().clear();
            this.containersInfos.getDockerContainerInfos().addAll(containersInfos.getDockerContainerInfos());
        }
    }

    public CommandExecuteStatusMessage getStatusMessageFormQueue() {
        return statusMessageQueue.poll();
    }

    public DockerContainersInfos getContainerInfosFromQueue() {
        synchronized (containersInfos) {
            return containersInfos;
        }
    }
}
