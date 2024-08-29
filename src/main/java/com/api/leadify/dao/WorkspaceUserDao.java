package com.api.leadify.dao;

import com.api.leadify.entity.User;
import com.api.leadify.entity.WorkspaceUser;
import com.api.leadify.entity.WorkspaceUser_email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class WorkspaceUserDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public WorkspaceUserDao(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public ApiResponse<List<?>> getByWorkspaceId(UUID workspaceId) {
        try {
            String sql = "SELECT wu.id, wu.workspace_id, wu.user_id, u.email " +
                    "FROM workspace_user wu " +
                    "JOIN user u ON wu.user_id = u.id " +
                    "WHERE wu.workspace_id = ?";
            List<WorkspaceUser_email> workspaceUsers = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(WorkspaceUser_email.class), workspaceId.toString());

            if (workspaceUsers.isEmpty()) {
                return new ApiResponse<>("No workspace users found for the given workspace ID", null, 404);
            } else {
                return new ApiResponse<>("Workspace users retrieved successfully", workspaceUsers, 200);
            }
        } catch (Exception e) {
            return new ApiResponse<>("Error retrieving workspace users", null, 500);
        }
    }
    public ApiResponse<String> deleteByUserId(int userId, UUID workspaceId) {
        try {
            String sql = "DELETE FROM workspace_user WHERE user_id = ? AND workspace_id = ?";
            int affectedRows = jdbcTemplate.update(sql, userId, workspaceId);

            if (affectedRows > 0) {
                return new ApiResponse<>("Workspace user deleted successfully", null, 200);
            } else {
                return new ApiResponse<>("No workspace user found for the given user ID and workspace ID", null, 404);
            }
        } catch (Exception e) {
            return new ApiResponse<>("Error deleting workspace user", null, 500);
        }
    }
    public ApiResponse<String> addUserToWorkspace(int userId, UUID workspaceId) {
        try {
            String sql = "INSERT INTO workspace_user (user_id, workspace_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, userId, workspaceId.toString());
            return new ApiResponse<>("User added to workspace successfully", null, 200);
        } catch (Exception e) {
            return new ApiResponse<>("Error adding user to workspace", null, 500);
        }
    }
    public List<User> searchUsersNotInWorkspace(String searchTerm, UUID workspaceId) {
        try {
            String sql = "SELECT u.id, u.first_name, u.last_name, u.email " +
                    "FROM user u " +
                    "WHERE (lower(u.first_name) LIKE lower(?) OR lower(u.last_name) LIKE lower(?) OR lower(u.email) LIKE lower(?)) " +
                    "AND u.type_id = 2 " +
                    "AND NOT EXISTS (SELECT 1 FROM workspace_user wu WHERE wu.user_id = u.id AND wu.workspace_id = ?)";

            searchTerm = "%" + searchTerm + "%";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), searchTerm, searchTerm, searchTerm, workspaceId.toString());
        } catch (Exception e) {
            // Handle exception, e.g., log it or throw a custom exception
            return null;
        }
    }
}
