package ru.spbu.phys.bdc.runner.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import ru.spbu.phys.bdc.runner.model.configuration.ConfigurationParameter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Slf4j
@Component
public class ConfigurationRepository extends JdbcDaoSupport {
    //language=SQL
    private static final String FIND_PARAMETER_BY_KEY = "SELECT key, value FROM configuration WHERE key=?";

    //language=SQL
    private static final String SAVE_PARAMETER = """
            INSERT INTO configuration (key, value) VALUES (?, ?)
            ON CONFLICT (key) DO UPDATE SET value=EXCLUDED.value;""";

    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    public ConfigurationRepository(DataSource dataSource) {
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

    public Optional<ConfigurationParameter> findParameterByKey(String key) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_PARAMETER_BY_KEY, this::configurationParameterMapper, key));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void saveParameter(ConfigurationParameter parameter) {
        jdbcTemplate.update(SAVE_PARAMETER, parameter.key(), parameter.value());
    }

    private ConfigurationParameter configurationParameterMapper(ResultSet rs, int rowNum) {
        try {
            String key = rs.getString("key");
            String value = rs.getString("value");
            return new ConfigurationParameter(key, value);
        } catch (SQLException e) {
            log.error("Can't create configuration parameter");
            return null;
        }
    }
}
