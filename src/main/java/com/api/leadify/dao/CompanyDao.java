package com.api.leadify.dao;

import com.api.leadify.entity.Company;
import com.api.leadify.entity.CompanyResponse;
import com.api.leadify.entity.DashboardResponse;
import com.api.leadify.entity.Interested;
import com.api.leadify.entity.SessionM;
import com.api.leadify.entity.User;
import com.api.leadify.entity.WorkspaceResponse;
import com.api.leadify.jwt.JWT;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.digester.ArrayStack;
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
    private HttpServletRequest request;

    @Autowired
    public CompanyDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<CompanyResponse> getCompanies() {
        String sql = "select c.* ,i.name as industry from company c JOIN industry i where c.industry_id = i.id";
        String favsql="select c.* ,i.name as industry from company c JOIN industry i where c.industry_id = i.id and favorite=true";
        CompanyResponse company=new CompanyResponse();
        try {
            List<CompanyResponse.resp> companyList = jdbcTemplate.query(sql, (rs, rowNum) -> {
                CompanyResponse.resp datas= new CompanyResponse.resp();
                datas.setId(rs.getInt("id"));
                datas.setName(rs.getString("name"));
                datas.setLocation(rs.getString("location"));
                datas.setIndustry(rs.getString("industry"));
                datas.setFav(rs.getBoolean("favorite"));
                return datas;
                });
    
             List<CompanyResponse.resp> companyfav = jdbcTemplate.query(favsql, (rs, rowNum) -> {
                CompanyResponse.resp datas= new CompanyResponse.resp();
                datas.setId(rs.getInt("id"));
                datas.setName(rs.getString("name"));
                datas.setLocation(rs.getString("location"));
                datas.setIndustry(rs.getString("industry"));
                datas.setFav(rs.getBoolean("favorite"));
                return datas;
                });
    
            company.setCompanyList(companyList);
            company.setFavorites(companyfav);
    
            return ResponseEntity.ok(company);
        } catch (EmptyStackException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

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

    public ResponseEntity<DashboardResponse> getDashboardData(String Search){
        Integer limit=3;
        String searchTermLike = "%" + Search + "%";
        String searchTermNull = "%" + "" + "%";
        boolean normalclient=false;
        boolean normalworkspaces=false;
       
        String clientSQL = "SELECT c.id, c.name AS clients, c.favorite, COUNT(w.id) AS workspaces, COUNT(wu.user_id) as users  FROM company c LEFT JOIN workspace w ON c.id = w.company_id \n" + //
        "LEFT JOIN workspace_user wu ON w.id = wu.workspace_id where c.name LIKE ?\n" + //
        "GROUP BY c.id order by c.updated_at DESC LIMIT ?;";
        String clientFavoriteSQL="SELECT c.id, c.name AS clients, c.favorite, COUNT(w.id) AS workspaces, COUNT(wu.user_id) as users  FROM company c LEFT JOIN workspace w ON c.id = w.company_id \n" + //
        "LEFT JOIN workspace_user wu ON w.id = wu.workspace_id where c.favorite=1 and c.name LIKE ?\n" + //
        "GROUP BY c.id order by c.updated_at DESC LIMIT ?;";
        String workspaceSQL = "select w.id as workspace_id , c.name as client ,w.name ,w.description, w.favorite from workspace w \n" + //
        "join company c on w.company_id=c.id where w.name LIKE ? order by w.updated_at DESC LIMIT ?;";
        String workspaceFavoriteSQL="select w.id as workspace_id , c.name as client ,w.name ,w.description,w.favorite from workspace w \n" + //
        "join company c on w.company_id=c.id where w.favorite=1 and w.name LIKE ? order by w.updated_at DESC LIMIT ?;";
        String userSQL = "select wu.id, u.email as name, wu.updated_at as date from workspace_user wu join user u on wu.user_id=u.id group by u.id order by wu.updated_at DESC LIMIT ?;";
       
        String workspaceUserSQL = "select w.id as workspace_id , c.name as client ,w.name ,w.description,w.favorite from workspace w left join workspace_user wu on w.id=wu.workspace_id\n" + //
                        "join company c on w.company_id=c.id where wu.user_id=? and w.name LIKE ? order by w.updated_at DESC LIMIT ?;";
        String workspaceUserFavoriteSQL="select w.id as workspace_id , c.name as client ,w.name ,w.description,w.favorite from workspace w left join workspace_user wu on w.id=wu.workspace_id\n" + //
                        "join company c on w.company_id=c.id where wu.user_id=? and w.favorite=1 and w.name LIKE ? order by w.updated_at DESC LIMIT ?;";

        DashboardResponse dash= new DashboardResponse();
        List<DashboardResponse.clientsResp> clientList;
        List<DashboardResponse.clientsResp> clientFavList;
        List<DashboardResponse.workspaceResp> workspaceList;
        List<DashboardResponse.workspaceResp> workspaceFavoriteList;
        List<DashboardResponse.userResp> UserList;

        SessionM sessionM = JWT.getSession(request);
        Integer userId = sessionM.getIdUsuario();

        String adminCheckSql = "select type_id from user where id=?;";
        Integer userTypeId = jdbcTemplate.queryForObject(adminCheckSql, Integer.class, userId);

        if(userTypeId==1){
                clientFavList = jdbcTemplate.query(clientFavoriteSQL, new BeanPropertyRowMapper<>(DashboardResponse.clientsResp.class),searchTermNull,limit);
                if(clientFavList.size()<3){
                    clientList = jdbcTemplate.query(clientSQL, new BeanPropertyRowMapper<>(DashboardResponse.clientsResp.class),searchTermNull,limit);
                    normalclient=true;
                    clientFavList=clientList;
                }
                workspaceFavoriteList = jdbcTemplate.query(workspaceFavoriteSQL, new BeanPropertyRowMapper<>(DashboardResponse.workspaceResp.class),searchTermNull,limit);
                if(workspaceFavoriteList.size()<3){
                    workspaceList = jdbcTemplate.query(workspaceSQL, new BeanPropertyRowMapper<>(DashboardResponse.workspaceResp.class),searchTermNull,limit);
                    normalworkspaces=true;
                    workspaceFavoriteList=workspaceList;
                }
                if(!Search.isEmpty()){
                    if(normalclient== true){
                        clientFavList = jdbcTemplate.query(clientSQL, new BeanPropertyRowMapper<>(DashboardResponse.clientsResp.class),searchTermLike ,limit);
                    }else{
                        clientFavList = jdbcTemplate.query(clientFavoriteSQL, new BeanPropertyRowMapper<>(DashboardResponse.clientsResp.class),searchTermLike,limit);
                    }
                    if(normalworkspaces== true){
                        workspaceFavoriteList = jdbcTemplate.query(workspaceSQL, new BeanPropertyRowMapper<>(DashboardResponse.workspaceResp.class),searchTermLike ,limit);  
                    }else{
                        workspaceFavoriteList = jdbcTemplate.query(workspaceFavoriteSQL, new BeanPropertyRowMapper<>(DashboardResponse.workspaceResp.class),searchTermLike ,limit);

                    }
                }
            UserList = jdbcTemplate.query(userSQL, new BeanPropertyRowMapper<>(DashboardResponse.userResp.class),limit);
            dash.setClients(clientFavList);
            dash.setWorksapces(workspaceFavoriteList);
            dash.setUserWorkspaces(UserList);
        }
        else{
            workspaceFavoriteList = jdbcTemplate.query(workspaceUserFavoriteSQL, new BeanPropertyRowMapper<>(DashboardResponse.workspaceResp.class),userId,searchTermNull,limit);
                if(workspaceFavoriteList.size()<3){
                    workspaceList = jdbcTemplate.query(workspaceUserSQL, new BeanPropertyRowMapper<>(DashboardResponse.workspaceResp.class),userId,searchTermNull,limit);
                    normalworkspaces=true;
                    workspaceFavoriteList=workspaceList;
                }
                if(!Search.isEmpty()){
                    if(normalworkspaces== true){
                        workspaceFavoriteList = jdbcTemplate.query(workspaceUserSQL, new BeanPropertyRowMapper<>(DashboardResponse.workspaceResp.class),userId,searchTermLike ,limit);  
                    }else{
                        workspaceFavoriteList = jdbcTemplate.query(workspaceUserFavoriteSQL, new BeanPropertyRowMapper<>(DashboardResponse.workspaceResp.class),userId,searchTermLike ,limit);
                    }  
                }
                dash.setWorksapces(workspaceFavoriteList);
        }

        return ResponseEntity.ok(dash);
    }

}
