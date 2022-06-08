package ru.spbu.phys.bdc.runner.service.updater;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.settings.ModuleConfigurationParameter;
import ru.spbu.phys.bdc.runner.model.updater.BackupData;
import ru.spbu.phys.bdc.runner.service.settings.SettingsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BackupService {
    private static final String BACKUP_FOLDER = "BACKUP_FOLDER";
    private static final String EXCLUDE_BACKUP_FOLDER = "EXCLUDE_BACKUP_FOLDER";
    private static final String BACKUP_NAME_TEMPLATE = "backup_%d.tar.gz";

    private static final String CREATE_PYTHON_BACKUP_SCRIPT_TEMPLATE = "printf \"%s\" > /home/dockerx/tar_data.py";
    private static final String CREATE_PYTHON_UNPACK_SCRIPT_TEMPLATE = "printf \"%s\" > /tmp/unpack_data.py";
    private static final String CREATE_BACKUP_DATA_JSON_FILE_TEMPLATE = "printf \"%s\" > /home/dockerx/backup_data.json";
    private static final String EXEC_PYTHON_BACKUP_SCRIPT = "python3 tar_data.py";
    private static final String EXEC_PYTHON_UNPACK_SCRIPT = "python3 /tmp/unpack_data.py";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SettingsService settingsService;

    public BackupService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public List<String> backup(String ipAddress, String username, String password) {
        var backupFolders = settingsService.getModuleParametersByKey(BACKUP_FOLDER).parameters()
                .stream()
                .map(ModuleConfigurationParameter::value)
                .toList();
        var excludedBackupsFolder = settingsService.getModuleParametersByKey(EXCLUDE_BACKUP_FOLDER).parameters()
                .stream()
                .map(ModuleConfigurationParameter::value)
                .toList();
        var backupMap = getBackupMap(backupFolders, excludedBackupsFolder);
        return makeBackup(backupMap, ipAddress, username, password);
    }

    public void unpack(String ipAddress, String username, String password) {
        try {
            String unpackPythonScript = loadPythonScript("backuper/unpack_data.py");
            Session session = getSession(username, ipAddress, password);
            Channel channel = session.openChannel("exec");
            execCommand(channel, String.format(CREATE_PYTHON_UNPACK_SCRIPT_TEMPLATE, unpackPythonScript.replace("\"", "\\\"")));
            channel.disconnect();
            execPythonScript(session, EXEC_PYTHON_UNPACK_SCRIPT);
            session.disconnect();
        } catch (Exception e) {
            log.error("Can't unpack data", e);
            throw new RuntimeException("Can't unpack data");
        }
    }

    private void execPythonScript(Session session, String execPythonUnpackScript) throws JSchException, IOException {
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(execPythonUnpackScript);
        ((ChannelExec) channel).setErrStream(System.err);
        InputStream in = channel.getInputStream();
        channel.connect();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int j = in.read(tmp, 0, 1024);
                if (j < 0) {
                    break;
                }
                log.info(new String(tmp, 0, j));
            }
            if (channel.isClosed()) {
                log.info("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
        }
        channel.disconnect();
    }

    private Session getSession(String username, String ipAddress, String password) throws JSchException {
        Session session = new JSch().getSession(username, ipAddress);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    private Map<String, List<String>> getBackupMap(List<String> backupFolder, List<String> excludedBackupFolder) {
        return backupFolder
                .stream()
                .collect(Collectors.toMap(folder -> folder, folder -> checkExcludedFolders(excludedBackupFolder, folder)));
    }

    private List<String> checkExcludedFolders(List<String> excludedFolders, String backupFolder) {
        return excludedFolders
                .stream()
                .filter(excludedFolder -> excludedFolder.startsWith(backupFolder))
                .map(excludedFolder -> excludedFolder.substring(backupFolder.length()))
                .toList();
    }

    private List<String> makeBackup(Map<String, List<String>> backupMap, String ipAddress, String username, String password) {
        List<String> backupFolders = new ArrayList<>(backupMap.keySet());
        List<String> names = new ArrayList<>();
        for (int i = 0; i < backupFolders.size(); i++) {
            String backupFolder = backupFolders.get(i);
            List<String> excludedFiles = backupMap.get(backupFolder);
            BackupData backupData = new BackupData(i, backupFolder, excludedFiles);
            try {
                String backupDataJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(backupData);
                String pythonBackupScript = loadPythonScript("backuper/tar_data.py");
                Session session = getSession(username, ipAddress, password);
                Channel channel = session.openChannel("exec");
                execCommand(channel, String.format(CREATE_PYTHON_BACKUP_SCRIPT_TEMPLATE, pythonBackupScript.replace("\"", "\\\"")));
                execCommand(channel, String.format(CREATE_BACKUP_DATA_JSON_FILE_TEMPLATE, backupDataJson.replace("\"", "\\\"")));
                channel.disconnect();
                execPythonScript(session, EXEC_PYTHON_BACKUP_SCRIPT);
                session.disconnect();
                names.add(String.format(BACKUP_NAME_TEMPLATE, i));
            } catch (Exception e) {
                log.error("Can't serialize backup data", e);
                throw new RuntimeException("Can't serialize backup data");
            }
        }
        return names;
    }

    private String loadPythonScript(String path) throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) {
            log.error("Can't get python backuper script from resources");
            throw new RuntimeException("Can't get python backuper script from resources");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        return builder.toString();
    }

    private void execCommand(Channel channel, String command) throws JSchException, InterruptedException {
        ((ChannelExec) channel).setCommand(command);
        channel.connect();
        Thread.sleep(100);
        channel.disconnect();
    }

}
