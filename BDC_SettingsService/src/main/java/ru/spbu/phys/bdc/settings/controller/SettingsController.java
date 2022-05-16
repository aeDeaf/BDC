package ru.spbu.phys.bdc.settings.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.spbu.phys.bdc.api.model.settings.Settings;
import ru.spbu.phys.bdc.settings.service.ConfigurationService;

@RestController
@RequestMapping("/settings")
public class SettingsController {
    private final ConfigurationService configurationService;

    @Autowired
    public SettingsController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping
    private ResponseEntity<Settings> getSettings() {
        var settings = configurationService.findSettings();
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @PostMapping
    private ResponseEntity<String> saveSettings(@RequestBody Settings settings) {
        configurationService.saveSettings(settings);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
