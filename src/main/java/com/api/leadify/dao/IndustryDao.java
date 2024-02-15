package com.api.leadify.dao;

import com.api.leadify.entity.Industry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.EmptyStackException;
import java.util.List;

@Repository
public class IndustryDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public IndustryDao(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public ApiResponse<Industry> createIndustry(Industry industry) {
        String sql = "INSERT INTO industry(name) values(?)";
        try {
            jdbcTemplate.update(
                    sql,
                    industry.getName()
            );

            return new ApiResponse<>("Industry created successfully", industry, 200);
        } catch (EmptyStackException e) {
            // Industry not found
        }
        return new ApiResponse<>("Error", null, 404);
    }

    public List<Industry> getIndustries() {
        String sql = "SELECT * FROM industry";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Industry.class));
    }
}
