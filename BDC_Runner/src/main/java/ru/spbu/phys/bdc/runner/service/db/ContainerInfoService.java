package ru.spbu.phys.bdc.runner.service.db;

import org.springframework.stereotype.Service;
import ru.spbu.phys.bdc.runner.model.db.ContainerInfo;
import ru.spbu.phys.bdc.runner.repository.ContainerRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ContainerInfoService {
    private final ContainerRepository containerRepository;

    public ContainerInfoService(ContainerRepository containerRepository) {
        this.containerRepository = containerRepository;
    }

    public void saveContainerInfo(String containerName, String imageName, String username, String password) {
        ContainerInfo containerInfo = ContainerInfo
                .builder()
                .containerName(containerName)
                .imageName(imageName)
                .username(username)
                .password(password)
                .build();
        containerRepository.saveContainer(containerInfo);
    }

    public List<ContainerInfo> getContainerInfos() {
        return containerRepository.findContainerInfo();
    }

    public Optional<ContainerInfo> getContainerInfoByContainerName(String containerName) {
        return containerRepository.findContainerInfo(containerName);
    }
}
