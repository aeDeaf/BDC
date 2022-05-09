package ru.spbu.phys.bdc.runner.service.cli.docker;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.spbu.phys.bdc.api.model.executor.CommandType;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;
import ru.spbu.phys.bdc.api.model.result.CommandExecuteStatus;
import ru.spbu.phys.bdc.api.model.result.CommandExecuteStatusMessage;
import ru.spbu.phys.bdc.api.model.result.docker.DockerContainerInfo;
import ru.spbu.phys.bdc.api.model.result.docker.DockerContainersInfos;
import ru.spbu.phys.bdc.runner.model.configuration.Properties;
import ru.spbu.phys.bdc.runner.model.db.ContainerInfo;
import ru.spbu.phys.bdc.runner.service.TaskManagerService;
import ru.spbu.phys.bdc.runner.service.TaskObserver;
import ru.spbu.phys.bdc.runner.service.db.ContainerInfoService;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BmnDockerService implements TaskObserver {
    public static final String CONTAINER_NAME_PREFIX = "bmnroot";

    public static final String IMAGE_NAME_PARAMETER_NAME = "imageName";
    public static final String CONTAINER_NAME_PARAMETER_NAME = "containerName";

    public static final String CREATE_INIT_SH_TEMPLATE =
            "printf \"export DISPLAY=%s source /opt/bmnroot/install/bmnroot_config.sh\" > init.sh";
    public static final String SSH_CONNECT_COMMAND_TEMPLATE = "sshpass -p %s ssh -o \"StrictHostKeyChecking=no\" -o \"UserKnownHostsFile=/dev/null\" %s@%s";

    private final DockerCLIExecutor executor;
    private final TaskManagerService taskManagerService;
    private final Properties properties;
    private final ContainerInfoService containerInfoService;

    public BmnDockerService(DockerCLIExecutor executor, TaskManagerService taskManagerService, Properties properties, ContainerInfoService containerInfoService) {
        this.executor = executor;
        this.taskManagerService = taskManagerService;
        this.properties = properties;
        this.containerInfoService = containerInfoService;
    }

    @PostConstruct
    private void init() {
        taskManagerService.addObserver(this);
    }

    @Override
    public void processCommand(RunnerCommand command) {
        //TODO: зарефакторить на реализации интерфейса
        if (command.getCommandType() == CommandType.PULL_IMAGE) {
            pullContainer(command);
        } else if (command.getCommandType() == CommandType.CREATE_CONTAINER) {
            createContainer(command);
        } else if (command.getCommandType() == CommandType.START_CONTAINER) {
            runContainer(command);
        } else if (command.getCommandType() == CommandType.STOP_CONTAINER) {
            stopContainer(command);
        } else if (command.getCommandType() == CommandType.GET_CONTAINERS_STATUSES) {
            getContainerStatuses(command);
        }
    }

    private void pullContainer(RunnerCommand command) {
        String imageName = command.getParameter(IMAGE_NAME_PARAMETER_NAME);
        executor.pullContainer(imageName)
                .thenAccept(result -> {
                    sendSuccessStatus(command, result);
                });
    }

    private void createContainer(RunnerCommand command) {
        String imageName = command.getParameter(IMAGE_NAME_PARAMETER_NAME);
        Long maxContainerInfoID = containerInfoService.getContainerInfos()
                .stream()
                .map(ContainerInfo::getId)
                .max(Long::compareTo)
                .orElse(0L);
        String containerName = CONTAINER_NAME_PREFIX + maxContainerInfoID;
        if (!StringUtils.hasLength(containerName) || !StringUtils.hasLength(imageName)) {
            log.error("Error while processing CREATE_CONTAINER command");
            return;
        }
        executor.runContainer(imageName, containerName)
                .thenAccept(result -> {
                    executor.getContainerUsernameAndPassword(containerName)
                            .thenAccept(result2 -> {
                                List<String> logData = processLogData(result2);
                                String username = logData.get(0);
                                String password = logData.get(1);
                                containerInfoService.saveContainerInfo(containerName, imageName, username, password);
                                executor.stopContainer(containerName);
                                sendSuccessStatus(command, result);
                            });

                });
    }

    private void runContainer(RunnerCommand command) {
        String containerName = command.getParameter(CONTAINER_NAME_PARAMETER_NAME);
        executor.startContainer(containerName)
                .thenAccept(result -> {
                    executor.getContainerIP(containerName)
                            .thenAccept(result2 -> {
                                prepareContainer(result2, containerName, command);
                            });
                });
    }

    private void stopContainer(RunnerCommand command) {
        String containerName = command.getParameter(CONTAINER_NAME_PARAMETER_NAME);
        executor.stopContainer(containerName)
                .thenAccept(result -> {
                    sendSuccessStatus(command, result);
                });
    }

    private void getContainerStatuses(RunnerCommand command) {
        executor.dockerPS(CONTAINER_NAME_PREFIX)
                .thenAccept(this::processDockerPsResults);
    }

    private List<String> processLogData(String logData) {
        var logDataList = Arrays.asList(logData.split("\n")).subList(0, 2);
        return logDataList
                .stream()
                .map(line -> line.split(": ")[1])
                .collect(Collectors.toList());
    }

    private void prepareContainer(String result, String containerName, RunnerCommand runnerCommand) {
        String ipAddress = processIPData(result);
        if (ipAddress == null) {
            throw new RuntimeException("IP address is null!");
        }
        ContainerInfo containerInfo = containerInfoService.getContainerInfoByContainerName(containerName).orElseThrow();

        executor.getDisplayValue().thenAccept(result2 -> {
            try {
                Session session = new JSch().getSession(containerInfo.getUsername(), ipAddress);
                session.setPassword(containerInfo.getPassword());
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                Channel channel = session.openChannel("exec");
                String command = String.format(CREATE_INIT_SH_TEMPLATE, result2);
                ((ChannelExec) channel).setCommand(command);
                channel.connect();
                Thread.sleep(100);
                channel.disconnect();
                ((ChannelExec) channel).setCommand("chmod +x init.sh");
                channel.connect();
                Thread.sleep(100);
                channel.disconnect();
                session.disconnect();

                openTerminal(ipAddress, containerInfo.getUsername(), containerInfo.getPassword());
                sendSuccessStatus(runnerCommand, result2);
            } catch (Exception e) {
                log.error("Can't established ssh connection to container", e);
                throw new RuntimeException();
            }
        });
    }

    private void openTerminal(String ipAddress, String username, String password) {
        String command = String.format(SSH_CONNECT_COMMAND_TEMPLATE, password, username, ipAddress);
        executor.showTerminalWindow(command);
    }

    private String processIPData(String ipData) {
        var ipDataLines = Arrays.stream(ipData.split("\n"))
                .map(String::trim)
                .toList();
        for (var line : ipDataLines) {
            if (line.startsWith("\"IPAddress\"")) {
                String ipAddress = line.split(": ")[1];
                return ipAddress.substring(1, ipAddress.length() - 2);
            }
        }
        return null;
    }

    private void processDockerPsResults(String result) {
        var containerInfosLines = Arrays.asList(result.split("\n"));
        var containerInfoList = containerInfosLines
                .stream()
                .map(this::createDockerContainerInfo)
                .toList();
        DockerContainersInfos containersInfos = new DockerContainersInfos(containerInfoList);
        taskManagerService.sendContainerInfos(containersInfos);
    }

    private DockerContainerInfo createDockerContainerInfo(String line) {
        var splitLine = line.split("\s{2,}");
        var length = splitLine.length;
        return DockerContainerInfo
                .builder()
                .containerId(splitLine[0])
                .image(splitLine[1])
                .status(DockerContainerInfo.DockerContainerStatus.getStatusFromString(splitLine[4]))
                .name(splitLine[length - 1])
                .build();
    }

    private void sendSuccessStatus(RunnerCommand command, String result) {
        CommandExecuteStatusMessage message = CommandExecuteStatusMessage
                .builder()
                .taskName(command.getTaskName())
                .nodeName(properties.getNodeName())
                .status(CommandExecuteStatus.SUCCESS)
                .message(result)
                .build();
        taskManagerService.sendStatus(message);
    }
}
