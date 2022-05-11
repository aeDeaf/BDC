package ru.spbu.phys.bdc.registration.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import ru.spbu.phys.bdc.api.model.registration.Status;
import ru.spbu.phys.bdc.registration.model.RegistrationData;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RegistrationDataRepository extends JdbcDaoSupport {
    //language=SQL
    private static final String GET_MAX_NODE_ID = "SELECT MAX(id) FROM nodes;";

    //language=SQL
    private static final String SAVE_REGISTRATION_DATA = """
            INSERT INTO nodes (nodename, username)
            VALUES (?, ?)
            ON CONFLICT (nodename) DO UPDATE SET username=excluded.username;
            """;

    //language=SQL
    private static final String FIND_NODES_BY_USERNAME = """
            SELECT nodename, username, status FROM nodes AS n
            JOIN statuses AS s ON s.node_index = n.id
            WHERE username=?""";

    //language=SQL
    private static final String FIND_NODES = """
            SELECT nodename, username, status FROM nodes AS n
            JOIN statuses AS s ON n.id = s.node_index""";

    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    @Autowired
    public RegistrationDataRepository(DataSource dataSource) {
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

    public int saveRegistrationData(RegistrationData registrationData) {
        return jdbcTemplate.update(SAVE_REGISTRATION_DATA, registrationData.getNodeName(), registrationData.getUsername());
    }

    public Optional<Long> getMaxNodeId() {
        return Optional.ofNullable(jdbcTemplate.queryForObject(GET_MAX_NODE_ID, Long.class));
    }

    public List<RegistrationData> findNodesByUsername(String username) {
        return jdbcTemplate.query(FIND_NODES_BY_USERNAME, this::registrationDataMapper, username);
    }

    public List<RegistrationData> findNodes() {
        return jdbcTemplate.query(FIND_NODES, this::registrationDataMapper);
    }

    private RegistrationData registrationDataMapper(ResultSet rs, int rowNum) {
        try {
            String nodeName = rs.getString("nodename");
            String username = rs.getString("username");
            return RegistrationData.builder()
                    .username(username)
                    .nodeName(nodeName)
                    .status(Status.values()[rs.getInt("status")])
                    .build();
        } catch (SQLException e) {
            log.warn("Can't create registration data");
            return null;
        }
    }
}
