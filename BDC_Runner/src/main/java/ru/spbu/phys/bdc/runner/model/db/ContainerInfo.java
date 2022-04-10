package ru.spbu.phys.bdc.runner.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerInfo {
    private Long id;
    private String containerName;
    private String imageName;
    private String username;
    private String password;
}
