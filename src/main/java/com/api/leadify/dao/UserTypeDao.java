package com.api.leadify.dao;

import com.api.leadify.entity.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserTypeDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserTypeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createdUserType(UserType userType) {
        String sql = "INSERT INTO user_type (name) VALUES (?)";
        jdbcTemplate.update(sql, userType.getName());
    }
    public ApiResponse<List<UserType>> getUserTypes() {
        String sql = "SELECT * FROM user_type where id > 1";

        try {
            List<UserType> userTypes = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserType.class));

            if (userTypes.isEmpty()) {
                return new ApiResponse<>("No user types found", null, 404);
            } else {
                return new ApiResponse<>("User types retrieved successfully", userTypes, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return new ApiResponse<>("Error retrieving user types", null, 500);
        }
    }
}
