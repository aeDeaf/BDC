package ru.spbu.phys.cph.bdc.taskmanager.model.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FrontendMessage {
    private int messageType;
    private String message;
    private Map<String, String> parameters;
}
