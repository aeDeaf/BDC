package ru.spbu.phys.bdc.runner.model.updater;

import java.util.List;

public record BackupData(Integer index, String backupFolder, List<String> excludedFiles) {
}
