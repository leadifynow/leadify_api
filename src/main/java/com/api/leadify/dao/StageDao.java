package com.api.leadify.dao;

import com.api.leadify.entity.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

@Repository
public class StageDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StageDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ApiResponse<List<Stage>> getStagesByWorkspaceId(UUID workspaceId) {
        try {
            String sql = "SELECT * FROM stage WHERE workspace_id = ? ORDER BY position_workspace";
            List<Stage> stages = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Stage.class), workspaceId.toString());

            if (stages.isEmpty()) {
                return new ApiResponse<>("No stages found for the given workspace ID", null, 404);
            } else {
                return new ApiResponse<>("Stages retrieved successfully", stages, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return new ApiResponse<>("Error retrieving stages", null, 500);
        }
    }
    public ApiResponse<String> updatePositions(List<Stage> stages) {
        try {
            for (Stage stage : stages) {
                String sql = "UPDATE stage SET position_workspace = ? WHERE id = ?";
                jdbcTemplate.update(sql, stage.getPosition_workspace(), stage.getId());
            }
            return new ApiResponse<>("Positions updated successfully", null, 200);
        } catch (Exception e) {
            return new ApiResponse<>("Error updating positions", null, 500);
        }
    }
    public ApiResponse<Integer> createStage(Stage newStage) {
        try {
            // Find the current maximum position_workspace value
            String maxPositionSql = "SELECT MAX(position_workspace) FROM stage WHERE workspace_id = ?";
            Integer maxPosition = jdbcTemplate.queryForObject(maxPositionSql, Integer.class, newStage.getWorkspace_id().toString());

            // If there are no existing stages, set position_workspace to 1; otherwise, increment the maximum value
            int newPosition = (maxPosition != null) ? maxPosition + 1 : 1;

            // Insert the new stage with the calculated position_workspace
            String insertSql = "INSERT INTO stage (name, description, workspace_id, position_workspace, followup, color) VALUES (?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, newStage.getName());
                ps.setString(2, newStage.getDescription());
                ps.setString(3, newStage.getWorkspace_id().toString());
                ps.setInt(4, newPosition);
                if (newStage.getFollowup() != null) {
                    ps.setInt(5, newStage.getFollowup());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.setString(6, newStage.getColor());
                return ps;
            }, keyHolder);

            // Retrieve the auto-generated ID of the newly created stage
            int newStageId = keyHolder.getKey().intValue();

            return new ApiResponse<>("Stage created successfully", newStageId, 201);
        } catch (EmptyResultDataAccessException e) {
            // Handle case where no results were returned from the database query
            return new ApiResponse<>("No existing stages found", null, 404);
        } catch (DataAccessException e) {
            // Handle database-related exceptions
            e.printStackTrace(); // Log the exception stack trace for debugging purposes
            return new ApiResponse<>("Error accessing database", null, 500);
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            e.printStackTrace(); // Log the exception stack trace for debugging purposes
            return new ApiResponse<>("Unexpected error", null, 500);
        }
    }
    public ApiResponse<Integer> getMinPositionStageId(UUID workspaceId) {
        try {
            String sql = "SELECT id FROM stage WHERE workspace_id = ? ORDER BY position_workspace ASC LIMIT 1";
            Integer minPositionStageId = jdbcTemplate.queryForObject(sql, Integer.class, workspaceId.toString());
            return new ApiResponse<>("Minimum position stage ID retrieved successfully", minPositionStageId, 200);
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving minimum position stage ID: " + e.getLocalizedMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public Integer getFollowupForStage(Integer stageId) {
        try {
            String sql = "SELECT followup FROM stage WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, stageId);
        } catch (DataAccessException e) {
            // Handle the case where the stage ID is not found or any other data access exception
            e.printStackTrace();
            return null;
        }
    }
    public ApiResponse<Integer> getStageIdByName(UUID workspaceId, String stageName) {
        try {
            String sql = "SELECT id FROM stage WHERE workspace_id = ? AND name = ?";
            Integer stageId = jdbcTemplate.queryForObject(sql, Integer.class, workspaceId.toString(), stageName);
            return new ApiResponse<>("Stage ID retrieved successfully", stageId, 200);
        } catch (EmptyResultDataAccessException e) {
            return new ApiResponse<>("No stage found with the given name in the workspace", null, 404);
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving stage ID by name: " + e.getLocalizedMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }

}