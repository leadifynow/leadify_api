package com.api.leadify.dao;

import com.api.leadify.entity.Workspace;
import com.api.leadify.entity.WorkspaceResponse;
import com.api.leadify.entity.WorkspaceResponse.workspace;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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
    public ResponseEntity<WorkspaceResponse> getAll() {
        try {
            String sql = "select w.*,  u.email as users, c.name as client\n" + //
                                "from workspace w JOIN workspace_user wu ON w.company_id=wu.id \n" + //
                                "JOIN user u ON wu.user_id=u.id \n" + //
                                "JOIN company c ON w.company_id=c.id ";
            List<WorkspaceResponse> WorkspaceResp = jdbcTemplate.query(sql, (rs, rowNum) -> {
                WorkspaceResponse work = new WorkspaceResponse();
                WorkspaceResponse.resp data = new WorkspaceResponse.resp();
                data.setId(rs.getString("id"));
                data.setName(rs.getString("name"));
                data.setClient(rs.getString("client"));
                data.setDescription(rs.getString("description"));
                data.setUsers(rs.getString("users"));
                data.setFav(rs.getBoolean("favorite"));
                work.setResponse(data);
                return work;
                });

                WorkspaceResponse workspace = new WorkspaceResponse();
                WorkspaceResponse.workspace companies = new WorkspaceResponse.workspace();
                List<WorkspaceResponse.resp> respList = new ArrayList<>();
                workspace.favorites = new ArrayList<>();
                companies.mindful_Agency = new ArrayList<>();
                companies.leadify_Now = new ArrayList<>();
                companies.mediablitz = new ArrayList<>();
                companies.vincent_Koza = new ArrayList<>();

                for (WorkspaceResponse work : WorkspaceResp) {
                boolean fav=(work.getResponse().isFav());
                if(fav){
                     WorkspaceResponse.resp favorite = new WorkspaceResponse.resp();
                    favorite.setId(work.getResponse().getId());
                    favorite.setName(work.getResponse().getName());
                    favorite.setClient(work.getResponse().getClient());
                    favorite.setDescription(work.getResponse().getDescription());
                    favorite.setUsers(work.getResponse().getUsers());
                    favorite.setFav(work.getResponse().isFav());
                    respList.add(favorite);
                }
                String name=(work.getResponse().getClient());
                if(name.equals("Mindful Agency")){
                    WorkspaceResponse.resp info = new WorkspaceResponse.resp();
                    info.setId(work.getResponse().getId());
                    info.setName(work.getResponse().getName());
                    info.setClient(work.getResponse().getClient());
                    info.setDescription(work.getResponse().getDescription());
                    info.setUsers(work.getResponse().getUsers());
                    companies.mindful_Agency.add(info);           
                }
                if(name.equals("Media Blitz")){
                    WorkspaceResponse.resp info = new WorkspaceResponse.resp();
                    info.setId(work.getResponse().getId());
                    info.setName(work.getResponse().getName());
                    info.setClient(work.getResponse().getClient());
                    info.setDescription(work.getResponse().getDescription());
                    info.setUsers(work.getResponse().getUsers());
                    companies.mediablitz.add(info);
                    
                }
                if(name.equals("Leadify Now")){
                    WorkspaceResponse.resp info = new WorkspaceResponse.resp();
                    info.setId(work.getResponse().getId());
                    info.setName(work.getResponse().getName());
                    info.setClient(work.getResponse().getClient());
                    info.setDescription(work.getResponse().getDescription());
                    info.setUsers(work.getResponse().getUsers());
                    companies.leadify_Now.add(info);
                    
                }
                if(name.equals("Vincent Koza")){
                    WorkspaceResponse.resp info = new WorkspaceResponse.resp();
                    info.setId(work.getResponse().getId());
                    info.setName(work.getResponse().getName());
                    info.setClient(work.getResponse().getClient());
                    info.setDescription(work.getResponse().getDescription());
                    info.setUsers(work.getResponse().getUsers());
                    companies.vincent_Koza.add(info); 
                }
                }
                workspace.setCompanies(companies);
                workspace.setFavorites(respList);


            if (WorkspaceResp.isEmpty()) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No workspaces found", null, 404);
            } else {

                return ResponseEntity.ok(workspace);
                //return new ApiResponse<>("Workspaces retrieved successfully", workspaces, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error retrieving workspaces", null, 500);
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
}
