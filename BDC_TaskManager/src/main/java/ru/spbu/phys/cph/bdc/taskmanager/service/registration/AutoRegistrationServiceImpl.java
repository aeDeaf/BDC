package ru.spbu.phys.cph.bdc.taskmanager.service.registration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.spbu.phys.bdc.api.model.registration.RegistrationDataDTO;
import ru.spbu.phys.cph.bdc.taskmanager.model.user.User;
import ru.spbu.phys.cph.bdc.taskmanager.service.user.provider.UserProvider;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile("!centrum")
public class AutoRegistrationServiceImpl {
    private final RegistrationService registrationService;
    private final UserProvider userProvider;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public AutoRegistrationServiceImpl(RegistrationService registrationService, UserProvider userProvider) {
        this.registrationService = registrationService;
        this.userProvider = userProvider;
    }

    @PostConstruct
    public void register() {
        executorService.scheduleAtFixedRate(this::registerNode, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void registerNode() {
        List<RegistrationDataDTO> nodes = registrationService.getNodes();
        User user = userProvider.getCurrentUser();
        if (nodes.size() > 0) {
            log.info("Nodes auto registration");
            nodes
                    .stream()
                    .filter(node -> node.getUsername() == null)
                    .map(node -> RegistrationDataDTO
                            .builder()
                            .nodeName(node.getNodeName())
                            .username(user.getUsername())
                            .build()
                    )
                    .forEach(registrationService::registerNode);

            executorService.shutdown();
        }
    }
}
