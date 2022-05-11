package ru.spbu.phys.cph.bdc.taskmanager.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import ru.spbu.phys.cph.bdc.taskmanager.model.user.Role;
import ru.spbu.phys.cph.bdc.taskmanager.model.user.User;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Slf4j
@Component
public class UserRepository extends JdbcDaoSupport {
    //language=SQL
    private static final String FIND_USER_BY_USERNAME = "SELECT username, password, role FROM users_data WHERE username=?";

    private JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

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

    public Optional<User> findUserByUsername(String username) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_USER_BY_USERNAME, this::userMapper, username));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private User userMapper(ResultSet rs, int rowNum) {
        try {
            String username = rs.getString("username");
            String password = rs.getString("password");
            Role role = Role.values()[rs.getInt("role")];
            return new User(username, password, role);
        } catch (SQLException e) {
            log.warn("Can't create user object");
            return null;
        }
    }
}
