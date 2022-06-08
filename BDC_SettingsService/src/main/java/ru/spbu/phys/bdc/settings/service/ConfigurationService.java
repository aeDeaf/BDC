package ru.spbu.phys.bdc.settings.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.api.model.settings.ConfigurationParameter;
import ru.spbu.phys.bdc.api.model.settings.ModuleParameters;
import ru.spbu.phys.bdc.api.model.settings.Settings;
import ru.spbu.phys.bdc.settings.repository.ConfigurationRepository;

@Service
public class ConfigurationService {
    private final ConfigurationRepository configurationRepository;

    @Autowired
    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public ConfigurationParameter findParameterByKey(String key) {
        return configurationRepository.findParameterByKey(key)
                .orElseGet(() -> {
                    ConfigurationParameter parameter = new ConfigurationParameter(key, null);
                    saveParameter(parameter);
                    return parameter;
                });
    }

    public ModuleParameters findParametersByModuleName(String moduleName) {
        return new ModuleParameters(configurationRepository.findModuleParameterByModuleName(moduleName));
    }

    public Settings findSettings() {
        return new Settings(configurationRepository.findParameters());
    }

    public void saveParameter(ConfigurationParameter parameter) {
        configurationRepository.findParameterByKey(parameter.key())
                .ifPresentOrElse(p -> configurationRepository.updateParameter(parameter),
                        () -> configurationRepository.saveParameter(parameter));
    }

    public void saveSettings(Settings settings) {
        settings.parameters()
                .forEach(this::saveParameter);
    }
}
