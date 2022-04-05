package ru.spbu.phys.bdc.runner.model.docker;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class DockerContainerInfo {
    public enum DockerContainerStatus {
        CREATED,
        EXITED,
        UP;

        public static DockerContainerStatus getStatusFromString(String string) {
            return DockerContainerStatus.valueOf(string.split(" ")[0].toUpperCase());
        }
    }

    private final String containerId;
    private final String image;
    private final DockerContainerStatus status;
    private final String name;
}
