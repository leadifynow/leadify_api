package com.api.leadify.dao;

import com.api.leadify.entity.Workspace;
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
    public ApiResponse<List<Workspace>> getWorkspacesByCompanyId(int companyId) {
        try {
            String sql = "SELECT * FROM workspace WHERE company_id = ?";
            List<Workspace> workspaces = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Workspace.class), companyId);

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
