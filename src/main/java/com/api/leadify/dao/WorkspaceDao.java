package com.api.leadify.dao;

import com.api.leadify.entity.SessionM;
import com.api.leadify.entity.Workspace;
import com.api.leadify.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class WorkspaceDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public WorkspaceDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean workspaceExists(UUID workspaceId) {
        String sql = "SELECT COUNT(*) FROM workspace WHERE id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, workspaceId.toString());
        return count >= 1;
    }
    public void createWorkspace(Workspace workspace) {
        String sql = "INSERT INTO workspace (id, name) VALUES (?, ?)";
        jdbcTemplate.update(sql, workspace.getId().toString(), workspace.getName());
    }
    public ApiResponse<List<Workspace>> getAll() {
        try {
            String sql = "SELECT * FROM workspace";
            List<Workspace> workspaces = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Workspace.class));

            if (workspaces.isEmpty()) {
                return new ApiResponse<>("No workspaces found", null, 404);
            } else {
                return new ApiResponse<>("Workspaces retrieved successfully", workspaces, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return new ApiResponse<>("Error retrieving workspaces", null, 500);
        }
    }
    public ApiResponse<Workspace> updateWorkspace(Workspace workspace) {
        try {
            String sql = "UPDATE workspace SET name = ?, description = ?, company_id = ? WHERE id = ?";
            int affectedRows = jdbcTemplate.update(
                    sql,
                    workspace.getName(),
                    workspace.getDescription(),
                    workspace.getCompany_id(),
                    workspace.getId().toString()
            );

            if (affectedRows > 0) {
                return new ApiResponse<>("Workspace updated successfully", workspace, 200);
            } else {
                return new ApiResponse<>("Workspace not found", null, 404);
            }
        } catch (Exception e) {
            return new ApiResponse<>("Error updating workspace", null, 500);
        }
    }
    public ApiResponse<List<Workspace>> getWorkspacesByCompanyId(int companyId, HttpServletRequest request) {
        try {
            // Retrieve the user information from the JWT token
            SessionM sessionM = JWT.getSession(request);
            int userId = sessionM.idUsuario;

            // SQL to check if the user is an admin
            String adminCheckSql = "SELECT ut.id FROM user u JOIN user_type ut ON u.type_id = ut.id WHERE u.id = ?";
            Integer userTypeId = jdbcTemplate.queryForObject(adminCheckSql, Integer.class, userId);

            List<Workspace> workspaces;

            if (userTypeId != null && userTypeId == 1) {
                // User is an admin, retrieve all workspaces for the company
                String sql = "SELECT * FROM workspace WHERE company_id = ?";
                workspaces = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Workspace.class), companyId);
            } else {
                // User is not an admin, retrieve only workspaces the user is associated with
                String sql = "SELECT w.* FROM workspace w " +
                        "JOIN workspace_user wu ON w.id = wu.workspace_id " +
                        "WHERE w.company_id = ? AND wu.user_id = ?";
                workspaces = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Workspace.class), companyId, userId);
            }

            if (workspaces.isEmpty()) {
                return new ApiResponse<>("No workspaces found for the given company ID", null, 404);
            } else {
                return new ApiResponse<>("Workspaces retrieved successfully", workspaces, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return new ApiResponse<>("Error retrieving workspaces", null, 500);
        }
    }
}
