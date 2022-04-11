package ru.spbu.phys.cph.bdc.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.spbu.phys.bdc.api.model.RunnerParameter;
import ru.spbu.phys.bdc.api.model.executor.CommandType;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FrontendCommand {
    private String nodeName;
    private CommandType commandType;
    private List<RunnerParameter> parameters;

    @JsonIgnore
    public String getParameter(String parameterName) {
        return parameters.
                stream()
                .filter(runnerParameter -> runnerParameter.getName().equals(parameterName))
                .findAny()
                .orElseThrow()
                .getValue();
    }
}
