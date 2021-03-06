package ru.spbu.phys.bdc.api.model.executor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.spbu.phys.bdc.api.model.RunnerParameter;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class RunnerCommand {
    private CommandType commandType;

    private String taskName;

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
