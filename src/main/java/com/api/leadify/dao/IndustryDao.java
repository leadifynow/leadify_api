package com.api.leadify.dao;

import com.api.leadify.entity.Industry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<Industry> createIndustry(Industry industry) {
        String sql = "INSERT INTO industry(name) values(?)";
        try {
            jdbcTemplate.update(
                    sql,
                    industry.getName()
            );
            return ResponseEntity.ok(industry);
            //return new ApiResponse<>("Industry created successfully", industry, 200);
        } catch (EmptyStackException e) {
            // Industry not found
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        //return new ApiResponse<>("Error", null, 404);
    }

    public List<Industry> getIndustries() {
        String sql = "SELECT * FROM industry";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Industry.class));
    }
}
