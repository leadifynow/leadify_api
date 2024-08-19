package com.api.leadify.dao;

import com.api.leadify.entity.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.EmptyStackException;
import java.util.List;

@Repository
public class CompanyDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CompanyDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Company> getCompanies() {
        String sql = "SELECT * FROM company";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Company.class));
    }
    public ResponseEntity<Company> createCompany(Company company) {
        String sql = "INSERT INTO company(name, location, flag, industry_id) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.update(
                    sql,
                    company.getName(),
                    company.getLocation(),
                    company.getFlag(),
                    company.getIndustry_id()
            );
            return ResponseEntity.ok(company);
            //return new ApiResponse<>("Company created successfully", company, 200);
        } catch (EmptyStackException e) {
            // Company not found
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        //return new ApiResponse<>("Error", null, 404);
    }
    public ResponseEntity<Company> updateCompany(Company company) {
        String sql = "UPDATE company SET name = ?, location = ?, flag = ?, industry_id = ? WHERE id = ?";
        try {
            int affectedRows = jdbcTemplate.update(
                    sql,
                    company.getName(),
                    company.getLocation(),
                    company.getFlag(),
                    company.getIndustry_id(),
                    company.getId()
            );

            if (affectedRows > 0) {
                // If at least one row is affected, it means the update was successful
                return ResponseEntity.ok(company);
                //return new ApiResponse<>("Company updated successfully", company, 200);
            } else {
                // No rows were affected, meaning the company with the given ID was not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("Company not found", null, 404);
            }
        } catch (Exception e) {
            // Handle exceptions such as DataAccessException, SQLException, etc.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error updating company", null, 500);
        }
    }
    public  ResponseEntity<String> deleteCompany(int companyId) {
        try {
            String sql = "DELETE FROM company WHERE id = ?";
            int affectedRows = jdbcTemplate.update(sql, companyId);

            if (affectedRows > 0) {
                return new ResponseEntity<>("Company deleted successfully", null, 200);
            } else {
                return new ResponseEntity<>("Company not found", null, 404);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting company", null, 500);
        }
    }


    public ResponseEntity<String> updateFavCompany(Integer CompanyId,boolean status) {
        String sql = "UPDATE company SET favorite = ? WHERE id = ?";
        try {
            int affectedRows = jdbcTemplate.update(
                    sql,
                    status,
                    CompanyId
            );
            if (affectedRows > 0) {
                return ResponseEntity.ok("Favorite company updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating favorite company.");
        }
    }

}
