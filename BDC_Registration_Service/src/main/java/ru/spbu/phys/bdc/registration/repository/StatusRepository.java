package ru.spbu.phys.bdc.registration.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import ru.spbu.phys.bdc.api.model.registration.NodeStatus;
import ru.spbu.phys.bdc.api.model.registration.Status;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class StatusRepository extends JdbcDaoSupport {
    //language=SQL
    private static final String INSERT_STATUS = """
            INSERT INTO statuses (node_index, status, timestamp)
            VALUES ((SELECT id FROM nodes WHERE nodename=?), ?, ?)
            ON CONFLICT (node_index) DO UPDATE SET status=EXCLUDED.status, timestamp=EXCLUDED.timestamp""";

    //language=SQL
    private static final String UPDATE_TIMESTAMP = """
            UPDATE statuses SET timestamp=? WHERE node_index=(SELECT id FROM nodes WHERE nodename=?)""";

    //language=SQL
    private static final String FIND_STATUSES = """
            SELECT n.nodename, s.status, s.timestamp FROM statuses AS s
            JOIN nodes AS n ON n.id = s.node_index;""";

    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    @Autowired
    public StatusRepository(DataSource dataSource) {
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

    public void updateStatus(NodeStatus status) {
        jdbcTemplate.update(INSERT_STATUS, status.nodeName(), status.status().ordinal(), status.timestamp().toEpochMilli());
    }

    public void updateTimestamp(NodeStatus status) {
        jdbcTemplate.update(UPDATE_TIMESTAMP, status.timestamp().toEpochMilli(), status.nodeName());
    }

    public List<NodeStatus> findStatuses() {
        return jdbcTemplate.query(FIND_STATUSES, this::nodeStatusMapper);
    }

    private NodeStatus nodeStatusMapper(ResultSet rs, int rowNum) {
        try {
            String nodeName = rs.getString("nodename");
            Status status = Status.values()[rs.getInt("status")];
            Instant timestamp = Instant.ofEpochMilli(rs.getLong("timestamp"));
            return new NodeStatus(nodeName, status, timestamp);
        } catch (SQLException e) {
            log.error("Can't create node status", e);
            return null;
        }
    }
}
