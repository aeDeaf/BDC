import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.spbu.phys.bdc.api.model.RunnerParameter;
import ru.spbu.phys.bdc.api.model.executor.CommandType;
import ru.spbu.phys.bdc.api.model.executor.RunnerCommand;

import java.util.ArrayList;
import java.util.Collections;

public class TestClass {
    @Test
    void test() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        RunnerParameter parameter = new RunnerParameter("imageName", "ubuntu:20.04");
        RunnerCommand command = new RunnerCommand(CommandType.PULL_IMAGE, "task1", Collections.singletonList(parameter));
        System.out.println(objectMapper.writeValueAsString(command));
    }
}
