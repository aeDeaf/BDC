package ru.spbu.phys.bdc.registration.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import ru.spbu.phys.bdc.registration.model.User;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Slf4j
@Component
public class UserRepository extends JdbcDaoSupport {
    //language=SQL
    private static final String GET_USER = "SELECT username FROM users WHERE username=?;";

    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    @Autowired
    public UserRepository(DataSource dataSource) {
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

    public Optional<User> getUser(String username) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(GET_USER, this::userMapper, username));
        } catch (EmptyResultDataAccessException e) {
            log.warn("Can't find user {}", username);
            return Optional.empty();
        }
    }

    private User userMapper(ResultSet rs, int rowNum) {
        try {
            return User
                    .builder()
                    .username(rs.getString("username"))
                    .build();
        } catch (SQLException e) {
            log.error("Can't create User object");
            return null;
        }
    }
}
