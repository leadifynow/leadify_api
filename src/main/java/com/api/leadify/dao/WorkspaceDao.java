package com.api.leadify.dao;

import com.api.leadify.entity.SessionM;
import com.api.leadify.entity.Workspace;
import com.api.leadify.entity.WorkspaceResponse;
import com.api.leadify.entity.WorkspaceResponse.workspace;

import com.api.leadify.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    public ResponseEntity<WorkspaceResponse> getAllOld(String ClientName, Integer orderBy) {
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT DISTINCT w.*, c.name AS client FROM workspace w " +
                            "LEFT JOIN workspace_user wu ON w.id = wu.workspace_id " +
                            "LEFT JOIN company c ON w.company_id = c.id ");

            List<Object> parameters = new ArrayList<>();

            // Check if ClientName is provided and add it to the SQL query
            if (ClientName != null && !ClientName.isEmpty()) {
                String search= "%"+ClientName +"%";
                sql.append("WHERE c.name like ? ");
                parameters.add(search);
            }

            // Check GroupOpc and apply groupings
            if (orderBy != null && orderBy != 0) {
                // Handle ordering without GroupOpc
                switch (orderBy) {
                    case 1:
                        sql.append("ORDER BY w.updated_at ASC ");
                        break;
                    case 2:
                        sql.append("ORDER BY w.updated_at DESC ");
                        break;
                    case 3:
                        sql.append("ORDER BY w.created_at ASC ");
                        break;
                    case 4:
                        sql.append("ORDER BY w.created_at DESC ");
                        break;
                }
            }

            // Default query for retrieving all companies and workspaces if no filters are applied
            String finalSql;
            if ((ClientName == null || ClientName.isEmpty()) && (orderBy == null || orderBy == 0)) {
                finalSql = "SELECT w.*, u.email AS users, c.name AS client " +
                        "FROM workspace w " +
                        "JOIN workspace_user wu ON w.id = wu.workspace_id " +
                        "JOIN user u ON wu.user_id = u.id " +
                        "JOIN company c ON w.company_id = c.id ";
            } else {
                finalSql = sql.toString();
            }

            // Execute query
            List<WorkspaceResponse> WorkspaceResp = jdbcTemplate.query(finalSql, parameters.toArray(),
                    (rs, rowNum) -> {
                        WorkspaceResponse work = new WorkspaceResponse();
                        WorkspaceResponse.resp data = new WorkspaceResponse.resp();
                        data.setId(rs.getString("id"));
                        data.setName(rs.getString("name"));
                        data.setClient(rs.getString("client"));
                        data.setDescription(rs.getString("description"));
                        data.setUsers(new ArrayList<>());
                        data.setFav(rs.getBoolean("favorite"));
                        work.setResponse(data);
                        return work;
                    }
            );

            // Query to retrieve all companies
            String query = "SELECT id, name FROM company";
            List<WorkspaceResponse.workspace> company = jdbcTemplate.query(query, (rs, rowNum) -> {
                WorkspaceResponse.workspace data = new WorkspaceResponse.workspace();
                data.setCompanyId(rs.getInt("id"));
                data.setCompanyName(rs.getString("name"));
                data.setWorkspaces(new ArrayList<>());
                return data;
            });

            // Query to retrieve users for each workspace
            String names = "SELECT DISTINCT wu.workspace_id AS id, u.id AS user_id, u.email AS name " +
                    "FROM workspace_user wu " +
                    "JOIN user u ON wu.user_id = u.id";

            List<WorkspaceResponse.user> users = jdbcTemplate.query(names, (rs, rowNum) -> {
                WorkspaceResponse.user userdata = new WorkspaceResponse.user();
                userdata.setIdWorkspace(rs.getString("id"));
                userdata.setUserId(rs.getInt("user_id"));
                userdata.setUserName(rs.getString("name"));
                return userdata;
            });

            // Process the retrieved data
            WorkspaceResponse workspace = new WorkspaceResponse();
            List<WorkspaceResponse.resp> respList = new ArrayList<>();
            List<WorkspaceResponse.workspace> clientWorkspace = new ArrayList<>();
            workspace.favorites = new ArrayList<>();
            workspace.companies = new ArrayList<>();

            for (WorkspaceResponse work : WorkspaceResp) {
                for (WorkspaceResponse.user data : users) {
                    if (work.getResponse().getId().equals(data.getIdWorkspace())) {
                        work.getResponse().getUsers().add(data);
                    }
                }

                if (work.getResponse().isFav()) {
                    respList.add(work.getResponse());
                }

                boolean added = false;
                for (WorkspaceResponse.workspace client : clientWorkspace) {
                    if (work.getResponse().getClient().equals(client.getCompanyName())) {
                        client.getWorkspaces().add(work.getResponse());
                        added = true;
                        break;
                    }
                }

                if (!added) {
                    WorkspaceResponse.workspace newWorkspace = new WorkspaceResponse.workspace();
                    for (WorkspaceResponse.workspace data : company) {
                        if (work.getResponse().getClient().equals(data.getCompanyName())) {
                            newWorkspace.setCompanyId(data.getCompanyId());
                            newWorkspace.setCompanyName(data.getCompanyName());
                        }
                    }
                    newWorkspace.setWorkspaces(new ArrayList<>());
                    newWorkspace.getWorkspaces().add(work.getResponse());
                    clientWorkspace.add(newWorkspace);
                }
            }

            workspace.setCompanies(clientWorkspace);
            workspace.setFavorites(respList);

            if (WorkspaceResp.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(workspace);
            }
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<Workspace> updateWorkspace(Workspace workspace) {
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
                return ResponseEntity.ok(workspace);
                //return new ApiResponse<>("Workspace updated successfully", workspace, 200);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("Workspace not found", null, 404);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error updating workspace", null, 500);
        }
    }
    public ResponseEntity<List<Workspace>> getWorkspacesByCompanyId(int companyId) {
        try {
            String sql = "SELECT * FROM workspace WHERE company_id = ?";
            List<Workspace> workspaces = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Workspace.class), companyId);

            if (workspaces.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No workspaces found for the given company ID", null, 404);
            } else {
                return ResponseEntity.ok(workspaces);
                //return new ApiResponse<>("Workspaces retrieved successfully", workspaces, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error retrieving workspaces", null, 500);
        }
    }

    public ResponseEntity<String> updateFavWorkspace(String workspaceId,boolean status) {
        String sql = "UPDATE workspace SET favorite = ? WHERE id = ?";
        try {
            int affectedRows = jdbcTemplate.update(
                    sql,
                    status,
                    workspaceId
            );
            if (affectedRows > 0) {
                return ResponseEntity.ok("Favorite workspace updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workspace not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating favorite workspace.");
        }
    }

    public ResponseEntity<List<WorkspaceResponse.resp>> getWorkspacesForUserOrAdmin(HttpServletRequest request) {
        try {
            // Retrieve the user session from the JWT
            SessionM sessionM = JWT.getSession(request);
            Integer userId = sessionM.getIdUsuario();

            // SQL to check if the user is an admin
            String adminCheckSql = "SELECT u.type_id FROM user u WHERE u.id = ?";
            Integer userTypeId = jdbcTemplate.queryForObject(adminCheckSql, new Object[]{userId}, Integer.class);

            // SQL to get all workspaces if the user is an admin
            String sql;
            List<Object> parameters = new ArrayList<>();

            if (userTypeId != null && userTypeId == 1) {
                // Admin user, retrieve all workspaces
                sql = "SELECT DISTINCT w.*, c.id as client_id, c.name AS client " +
                        "FROM workspace w " +
                        "LEFT JOIN company c ON w.company_id = c.id";
            } else {
                // Regular user, retrieve only workspaces the user has access to
                sql = "SELECT DISTINCT w.*,c.id as client_id, c.name AS client " +
                        "FROM workspace w " +
                        "JOIN workspace_user wu ON w.id = wu.workspace_id " +
                        "JOIN company c ON w.company_id = c.id " +
                        "WHERE wu.user_id = ?";
                parameters.add(userId);
            }

            // Execute the query with the proper parameters
            List<WorkspaceResponse.resp> workspaces = jdbcTemplate.query(sql, parameters.toArray(),
                    (rs, rowNum) -> {
                        WorkspaceResponse.resp workspace = new WorkspaceResponse.resp();
                        workspace.setId(rs.getString("id"));
                        workspace.setName(rs.getString("name"));
                        workspace.setClient(rs.getString("client"));
                        workspace.setClient_Id(rs.getInt("client_id"));
                        workspace.setDescription(rs.getString("description"));
                        workspace.setFav(rs.getBoolean("favorite"));
                        return workspace;
                    }
            );

            if (workspaces.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(workspaces);
            }

        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}
