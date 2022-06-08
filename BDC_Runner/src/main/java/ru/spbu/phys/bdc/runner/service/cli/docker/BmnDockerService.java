package ru.spbu.phys.bdc.runner.service.cli.docker;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.spbu.phys.bdc.runner.service.updater.BackupService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static final String BACKUP_HOST_PATH = "/tmp/bdc";
    public static final String BACKUP_CONTAINER_PATH_TEMPLATE = "/home/dockerx/%s";

    public static final String UNPACK_CONTAINER_PATH = "/tmp";

    private final DockerCLIExecutor executor;
    private final TaskManagerService taskManagerService;
    private final Properties properties;
    private final ContainerInfoService containerInfoService;
    private final BackupService backupService;

    @Autowired
    public BmnDockerService(DockerCLIExecutor executor, TaskManagerService taskManagerService, Properties properties, ContainerInfoService containerInfoService,
                            BackupService backupService) {
        this.executor = executor;
        this.taskManagerService = taskManagerService;
        this.properties = properties;
        this.containerInfoService = containerInfoService;
        this.backupService = backupService;
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
        } else if (command.getCommandType() == CommandType.UPDATE_CONTAINER) {
            updateContainer(command);
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
                                sendSuccessStatus(command, containerName);
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
                    sendSuccessStatus(command, containerName);
                });
    }

    private void getContainerStatuses(RunnerCommand command) {
        executor.dockerPS(CONTAINER_NAME_PREFIX)
                .thenAccept(this::processDockerPsResults);
    }

    private void updateContainer(RunnerCommand command) {
        String containerName = command.getParameter(CONTAINER_NAME_PARAMETER_NAME);
        executor.dockerPS(CONTAINER_NAME_PREFIX)
                .thenAccept(result -> {
                    DockerContainerInfo.DockerContainerStatus containerStatus = getDockerContainersInfos(result).getDockerContainerInfos()
                            .stream()
                            .filter(info -> info.getName().equals(containerName))
                            .map(DockerContainerInfo::getStatus)
                            .findAny()
                            .orElseThrow();
                    if (containerStatus != DockerContainerInfo.DockerContainerStatus.UP) {
                        executor.startContainer(containerName)
                                .thenAccept(result2 -> {
                                    executor.getContainerIP(containerName)
                                            .thenAccept(result3 -> {
                                                makeUpdate(command, containerName, result3);
                                            });
                                });
                    } else {
                        executor.getContainerIP(containerName)
                                .thenAccept(result2 -> {
                                    makeUpdate(command, containerName, result2);
                                });
                    }
                });
    }

    private void makeUpdate(RunnerCommand command, String containerName, String ipData) {
        log.info("Start backup data...");
        var names = makeBackup(containerName, ipData);
        copyBackupDataToHost(containerName, names, ipData, command);
    }

    private void afterBackupActions(RunnerCommand command, String containerName, String ipData) {
        log.info("Backup complete");
        log.info("Stop container");
        executor.stopContainer(containerName)
                .thenAccept(r -> {
                    log.info("Remove container");
                    executor.removeContainer(containerName)
                            .thenAccept(result -> {
                                log.info("Remove image");
                                String imageName = containerInfoService.getContainerInfoByContainerName(containerName).map(ContainerInfo::getImageName).orElseThrow();
                                executor.removeImage(imageName)
                                        .thenAccept(result2 -> {
                                            log.info("Create container");
                                            executor.runContainer(imageName, containerName)
                                                    .thenAccept(result3 -> {
                                                        executor.getContainerUsernameAndPassword(containerName)
                                                                .thenAccept(result4 -> {
                                                                    List<String> logData = processLogData(result4);
                                                                    String username = logData.get(0);
                                                                    String password = logData.get(1);
                                                                    containerInfoService.saveContainerInfo(containerName, imageName, username, password);
                                                                    try {
                                                                        log.info("Unpack data");
                                                                        var paths = getBackupFilesPaths();
                                                                        copyBackupDataToContainer(containerName, paths, ipData, command);
                                                                    } catch (Exception e) {
                                                                        log.error("Can't find backup files");
                                                                        throw new RuntimeException("Can't find backup files");
                                                                    }

                                                                });
                                                    });
                                        });
                            });
                });
    }

    private List<String> makeBackup(String containerName, String result2) {
        String ipAddress = processIPData(result2);
        ContainerInfo containerInfo = containerInfoService.getContainerInfoByContainerName(containerName).orElseThrow();
        return backupService.backup(ipAddress, containerInfo.getUsername(), containerInfo.getPassword());
    }

    private void makeUnpack(String containerName, String ipData) {
        String ipAddress = processIPData(ipData);
        ContainerInfo containerInfo = containerInfoService.getContainerInfoByContainerName(containerName).orElseThrow();
        backupService.unpack(ipAddress, containerInfo.getUsername(), containerInfo.getPassword());
    }

    private List<String> getBackupFilesPaths() throws IOException {
        Path path = Paths.get("/tmp/bdc");
        return Files.list(path)
                .map(p -> p.toAbsolutePath().toString())
                .toList();
    }

    private void copyBackupDataToHost(String containerName, List<String> names, String ipData, RunnerCommand command) {
        String name = names.get(0);
        String containerBackupPath = String.format(BACKUP_CONTAINER_PATH_TEMPLATE, name);
        executor.copyBackupDataToHost(containerName, containerBackupPath, BACKUP_HOST_PATH)
                .thenAccept(result -> {
                    if (names.size() > 1) {
                        copyBackupDataToHost(containerName, names.subList(1, names.size()), ipData, command);
                    } else {
                        afterBackupActions(command, containerName, ipData);
                    }
                });
    }

    private void copyBackupDataToContainer(String containerName, List<String> paths, String ipData, RunnerCommand command) {
        String path = paths.get(0);
        executor.copyBackupDataToContainer(containerName, UNPACK_CONTAINER_PATH, path)
                .thenAccept(result -> {
                    if (paths.size() > 1) {
                        copyBackupDataToContainer(containerName, paths.subList(1, paths.size()), ipData, command);
                    } else {
                        afterCopyBackupToContainerActions(containerName, ipData, command);
                    }
                });
    }

    private void afterCopyBackupToContainerActions(String containerName, String ipData, RunnerCommand command) {
        makeUnpack(containerName, ipData);
        sendSuccessStatus(command, containerName);
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
                sendSuccessStatus(runnerCommand, containerName);
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
        DockerContainersInfos containersInfos = getDockerContainersInfos(result);
        taskManagerService.sendContainerInfos(containersInfos);
    }

    private DockerContainersInfos getDockerContainersInfos(String result) {
        var containerInfosLines = Arrays.asList(result.split("\n"));
        var containerInfoList = containerInfosLines
                .stream()
                .map(this::createDockerContainerInfo)
                .toList();
        return new DockerContainersInfos(containerInfoList);
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
