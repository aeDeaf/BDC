package ru.spbu.phys.bdc.runner.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import ru.spbu.phys.bdc.runner.model.db.ContainerInfo;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class ContainerRepository extends JdbcDaoSupport {
    //language=SQL
    public static final String FIND_CONTAINERS_INFO = "SELECT id, container_name, image_name, username, password FROM containers;";

    //language=SQL
    public static final String FIND_CONTAINER_INFO_BY_CONTAINER_NAME =
            "SELECT id, container_name, image_name, username, password FROM containers WHERE container_name=?;";

    //language=SQL
    public static final String SAVE_CONTAINER =
            "INSERT INTO containers (container_name, image_name, username, password) VALUES (?, ?, ?, ?)";

    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    public ContainerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    private void init() {
        setDataSource(dataSource);
        jdbcTemplate = getJdbcTemplate();
        if (jdbcTemplate == null) {
            log.error("JdbcTemplate is null!");
            throw new RuntimeException("JdbcTemplate is null");
        }
    }

    public List<ContainerInfo> findContainerInfo() {
        return jdbcTemplate.query(FIND_CONTAINERS_INFO, this::containerInfoMapper);
    }

    public Optional<ContainerInfo> findContainerInfo(String containerName) {
        return Optional.ofNullable(
                jdbcTemplate.queryForObject(FIND_CONTAINER_INFO_BY_CONTAINER_NAME, this::containerInfoMapper, containerName));
    }

    public void saveContainer(ContainerInfo containerInfo) {
        jdbcTemplate.update(SAVE_CONTAINER, containerInfo.getContainerName(), containerInfo.getImageName(), containerInfo.getUsername(),
                containerInfo.getPassword());
    }

    private ContainerInfo containerInfoMapper(ResultSet rs, int rowNum) {
        try {
            return ContainerInfo
                    .builder()
                    .id(rs.getLong("id"))
                    .containerName(rs.getString("container_name"))
                    .imageName(rs.getString("image_name"))
                    .username(rs.getString("username"))
                    .password(rs.getString("password"))
                    .build();
        } catch (SQLException e) {
            log.error("Can't map result set to container info, skipping...", e);
            return new ContainerInfo();
        }
    }

}
