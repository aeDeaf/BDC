package ru.spbu.phys.bdc.registration.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import ru.spbu.phys.bdc.registration.model.RegistrationData;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
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
}
