package com.api.leadify.dao;

import com.api.leadify.entity.UserColumns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@Repository
public class UserColumnsDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserColumnsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<List<UserColumns>> getUserColumnsByUserIdAndWorkspaceId(Integer userId, String workspaceId) {
        try {
            String sql = "SELECT * FROM user_columns WHERE user_id = ? AND workspace_id = ?";
            List<UserColumns> userColumns = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserColumns.class), userId, workspaceId);
            if (userColumns.isEmpty()) {
                // If no columns are found, insert default columns and return the newly created record
                addUserColumns(new UserColumns( userId, workspaceId));
                // Fetch the newly created record
                userColumns = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserColumns.class), userId, workspaceId);
                return ResponseEntity.ok(userColumns);
                //return new ApiResponse<>("Added default columns.", userColumns, 200);
            } else {
                return ResponseEntity.ok(userColumns);
                //return new ApiResponse<>("User columns retrieved successfully", userColumns, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error retrieving user columns", null, 500);
        }
    }
    public void addUserColumns(UserColumns userColumns) {
        String sql = "INSERT INTO user_columns (user_id, workspace_id) VALUES (?, ?)";
        jdbcTemplate.update(
                sql,
                userColumns.getUser_id(),
                userColumns.getWorkspaceId()
        );
    }
    public ResponseEntity<UserColumns> updateUserColumns(UserColumns userColumns) {
        try {
            String sql = "UPDATE user_columns SET "
                    + "first_name = ?, "
                    + "last_name = ?, "
                    + "number_of_employees = ?, "
                    + "linkedin = ?, "
                    + "title = ?, "
                    + "campaign_name = ?, "
                    + "company_name = ?, "
                    + "interest_date = ?, "
                    + "notes = ?, "
                    + "website = ?, "
                    + "industry = ?, "
                    + "manager = ?, "
                    + "name = ?, "
                    + "event_name = ?, "
                    + "referral = ?, "
                    + "business = ? "
                    + "WHERE id = ? AND user_id = ? AND workspace_id = ?";

            int rowsUpdated = jdbcTemplate.update(sql,
                    userColumns.getFirst_name(),
                    userColumns.getLast_name(),
                    userColumns.getNumber_of_employees(),
                    userColumns.getLinkedin(),
                    userColumns.getTitle(),
                    userColumns.getCampaign_name(),
                    userColumns.getCompany_name(),
                    userColumns.getInterest_date(),
                    userColumns.getNotes(),
                    userColumns.getWebsite(),
                    userColumns.getIndustry(),
                    userColumns.getManager(),
                    userColumns.getName(),
                    userColumns.getEvent_name(),
                    userColumns.getReferral(),
                    userColumns.getBusiness(),
                    userColumns.getId(),
                    userColumns.getUser_id(),
                    userColumns.getWorkspaceId());

            if (rowsUpdated > 0) {
                return ResponseEntity.ok(userColumns);
                //return new ApiResponse<>("User columns updated successfully", userColumns, 200);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No user columns found for the given criteria", null, 404);
            }
        } catch (DataAccessException e) {
            // Log the SQL query and the exception
            //String errorMessage = "Error updating user columns. SQL: " + e.getLocalizedMessage();
            e.printStackTrace(); // Print the stack trace for detailed error information
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>(errorMessage, null, 500);
        }
    }

}