package ru.spbu.phys.bdc.settings.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.spbu.phys.bdc.api.model.settings.ConfigurationParameter;
import ru.spbu.phys.bdc.settings.service.ConfigurationService;

@RestController
@RequestMapping("/configuration")
public class ConfigurationController {
    private final ConfigurationService configurationService;

    @Autowired
    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/{key}")
    private ResponseEntity<ConfigurationParameter> findParameterByKey(@PathVariable String key) {
        var parameter = configurationService.findParameterByKey(key);
        return new ResponseEntity<>(parameter, HttpStatus.OK);
    }

    @PostMapping
    private ResponseEntity<String> saveParameter(@RequestBody ConfigurationParameter parameter) {
        configurationService.saveParameter(parameter);
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
