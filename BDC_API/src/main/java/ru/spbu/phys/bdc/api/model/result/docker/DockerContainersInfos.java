package ru.spbu.phys.bdc.api.model.result.docker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DockerContainersInfos {
    private List<DockerContainerInfo> dockerContainerInfos;
}
