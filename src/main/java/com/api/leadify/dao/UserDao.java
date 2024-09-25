package com.api.leadify.dao;

import com.api.leadify.entity.Company;
import com.api.leadify.entity.Interested;
import com.api.leadify.entity.User;
import com.api.leadify.entity.Paths;
import com.api.leadify.entity.SessionM;
import com.api.leadify.entity.UserPath;
import com.api.leadify.entity.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.api.leadify.jwt.JWT;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private HttpServletRequest request;

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public ResponseEntity<String> deleteUser(Integer userId) {
        String sql = "DELETE FROM user WHERE id = ?";

        try {
            int deletedRows = jdbcTemplate.update(sql, userId);

            if (deletedRows > 0) {
                return new ResponseEntity<>("User deleted successfully", null, 200);
            } else {
                return new ResponseEntity<>("User not found or already deleted", null, 404);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting user", null, 500);
        }
    }
    public ResponseEntity<User> updateUser(User updatedUser) {
        String sql = "UPDATE user SET first_name=?, last_name=?, email=?, password=?, type_id=? WHERE id=?";

        try {
            int updatedRows = jdbcTemplate.update(
                    sql,
                    updatedUser.getFirst_name(),
                    updatedUser.getLast_name(),
                    updatedUser.getEmail(),
                    updatedUser.getPassword(),
                    updatedUser.getType_id(),
                    updatedUser.getId()
            );

            if (updatedRows > 0) {

                String fetchNewUserQuery = "SELECT u.id, u.first_name, u.last_name, u.email, u.password, u.created_at, u.updated_at, u.type_id, ut.name as type_name\n" + //
                "FROM user u JOIN user_type ut ON u.type_id = ut.id WHERE u.id=?";
                User updatedUserData = jdbcTemplate.queryForObject(fetchNewUserQuery, new BeanPropertyRowMapper<>(User.class), updatedUser.getId());
                return ResponseEntity.ok(updatedUserData);
                //return new ApiResponse<>("User updated successfully", updatedUserData, 200);
            } else {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("User not found or no updates applied", null, 404);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error updating user", null, 500);
        }
    }
    private User getUserById(Integer userId) {
        String sql = "SELECT * FROM user WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    public ResponseEntity<String> getUserPassword(Integer userId){
        SessionM sessionM = JWT.getSession(request);
        Integer userActualId = sessionM.getIdUsuario();

        String sqlgetType="select type_id from user where id=?;";
        String sqlgetPass="select password from user where id=?;";
        String Pass="";
        Integer type = jdbcTemplate.queryForObject(sqlgetType, new Object[]{userActualId}, Integer.class);

        if(type==1){
             Pass = jdbcTemplate.queryForObject(sqlgetPass, new Object[]{userId}, String.class);
        }else{
            if(userActualId==userId){
                Pass = jdbcTemplate.queryForObject(sqlgetPass, new Object[]{userId}, String.class);  
            }
            else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can perform this action");
            }
        }

         return ResponseEntity.ok(Pass);
    }

    public ResponseEntity<User> createUser(User user) {
        String sql = "INSERT INTO user (first_name, last_name, email, password, type_id) VALUES (?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(
                    sql,
                    user.getFirst_name(),
                    user.getLast_name(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getType_id()
            );

              // Get the generated user_id
            String getUserIdQuery = "SELECT LAST_INSERT_ID()";
            int UserId = jdbcTemplate.queryForObject(getUserIdQuery, Integer.class);

            // Fetch the newly created User record
            String fetchNewUserQuery = "SELECT u.id, u.first_name, u.last_name, u.email, u.password, u.created_at, u.updated_at, u.type_id, ut.name as type_name\n" + //
                                "FROM user u JOIN user_type ut ON u.type_id = ut.id WHERE u.id=?";
            User newUser = jdbcTemplate.queryForObject(fetchNewUserQuery, new BeanPropertyRowMapper<>(User.class), UserId);

            // Fetch the created user to include in the ApiResponse
            return ResponseEntity.ok(newUser);
            //return new ApiResponse<>("User created successfully", null, 201);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error creating user", null, 500);
        }
    }
    public ResponseEntity<List<User>> getUsers(String search, Integer SortOpc, Integer GroupOpc) {
        StringBuilder sqlUser = new StringBuilder(
                    "SELECT u.id, u.first_name, u.last_name, u.email, u.password, u.created_at, u.updated_at, u.type_id, ut.name as type_name \n" + //
                                                "FROM user u JOIN user_type ut ON u.type_id = ut.id WHERE u.email LIKE ? ");

        String SearchPararm="%"+search+"%";

        if (GroupOpc != null && GroupOpc != 0){
            if(GroupOpc==1){
                sqlUser.append(" and u.type_id = 1");
            }
            if(GroupOpc==2){
                sqlUser.append(" and u.type_id = 2");
            }
        }
        if (SortOpc != null && SortOpc != 0){
            if(SortOpc==1){
                sqlUser.append(" order by u.updated_at desc ");
            }
            if(SortOpc==2){
                sqlUser.append(" order by u.created_at desc ");
            }
        }

        String finalSql = sqlUser.toString();
        

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(finalSql,SearchPararm);

            List<User> userList = new ArrayList<>();

            for (Map<String, Object> row : rows) {
                User user = new User();
                user.setId((Integer) row.get("id"));
                user.setFirst_name((String) row.get("first_name"));
                user.setLast_name((String) row.get("last_name"));
                user.setEmail((String) row.get("email"));
                //user.setPassword((String) row.get("password"));
                user.setType_id((Integer) row.get("type_id"));
                user.setType_name((String) row.get("type_name")); // New field for user_type name
                user.setCreated_at((Timestamp)row.get("created_at"));
                user.setUpdated_at((Timestamp)row.get("updated_at"));

                userList.add(user);
            }

            if (userList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No users found", null, 404);
            } else {
                return ResponseEntity.ok(userList);
                //return new ApiResponse<>("Users retrieved successfully", userList, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error retrieving users", null, 500);
        }
    }
    public ResponseEntity<?> loginUser(User user) {
        String sql = "SELECT id, first_name, last_name, email, type_id, theme, remember FROM user WHERE email = ? AND password = ?";
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, user.getEmail(), user.getPassword());

            UserToken loggedInUser = new UserToken(); // Use UserToken directly
            loggedInUser.setId((Integer) result.get("id"));
            loggedInUser.setFirst_name((String) result.get("first_name"));
            loggedInUser.setLast_name((String) result.get("last_name"));
            loggedInUser.setEmail((String) result.get("email"));
            loggedInUser.setType_id((Integer) result.get("type_id"));
            String token = JWT.getJWTToken((String) result.get("email"), (Integer) result.get("id"));
            loggedInUser.setToken(token); // Set the token
            loggedInUser.setTheme((Boolean) result.get("theme"));
            loggedInUser.setRemember((Boolean) result.get("remember"));
            Integer typeId = (Integer) result.get("type_id");
            List<Paths> pathsList = listPath(typeId);
            loggedInUser.setPathsList(pathsList);

            return ResponseEntity.ok(loggedInUser);
            //return new ApiResponse<>("Welcome back " + result.get("email") + "!", loggedInUser, 200);
        } catch (EmptyResultDataAccessException e) {
            // User not found
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        //return new ApiResponse<>("Username or password do not exist", null, 404);
    }
        
    public List<Paths> listPath(Integer typeId){
            String sql = "select p.id, p.url, p.icon, p.name from user_path up JOIN paths p ON up.url_id = p.id where type_id= ? ";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, typeId);
            List<Paths> pathsList = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                Paths path = new Paths();
                path.setId((Integer) row.get("id"));
                path.setUrl((String) row.get("url"));
                path.setIcon((String) row.get("icon"));
                path.setName((String) row.get("name"));
                pathsList.add(path);
            }
            return pathsList;
    }

    public ResponseEntity<List<Company>> getUserCompanies(Integer userId) {
        String adminCheckSql = "SELECT ut.id FROM user u JOIN user_type ut ON u.type_id = ut.id WHERE u.id = ?";
        String allCompaniesSql = "SELECT DISTINCT c.id, c.name, c.location, c.flag, c.industry_id FROM company c";
        String userCompaniesSql = "SELECT DISTINCT c.id, c.name, c.location, c.flag, c.industry_id " +
                "FROM user u " +
                "JOIN user_type ut ON u.type_id = ut.id " +
                "JOIN workspace_user wu ON u.id = wu.user_id " +
                "JOIN workspace w ON wu.workspace_id = w.id " +
                "JOIN company c ON w.company_id = c.id " +
                "WHERE u.id = ?";

        try {
            // Check if the user is an admin
            Integer userTypeId = jdbcTemplate.queryForObject(adminCheckSql, Integer.class, userId);
            List<Map<String, Object>> rows;

            if (userTypeId != null && userTypeId == 1) {
                // User is an admin, get all companies
                rows = jdbcTemplate.queryForList(allCompaniesSql);
            } else {
                // User is not an admin, get companies associated with the user
                rows = jdbcTemplate.queryForList(userCompaniesSql, userId);
            }

            List<Company> companyList = new ArrayList<>();

            for (Map<String, Object> row : rows) {
                Company company = new Company();
                company.setId((Integer) row.get("id"));
                company.setName((String) row.get("name"));
                company.setLocation((String) row.get("location"));
                company.setFlag((String) row.get("flag"));

                companyList.add(company);
            }

            if (companyList.isEmpty()) { 
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No companies found for the given user", null, 404);
            } else {
                return ResponseEntity.ok(companyList);
                //return new ApiResponse<>("Companies retrieved successfully", companyList, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error retrieving companies", null, 500);
        }
    }

    public ResponseEntity<List<String>>getUserWorkspaces(Integer userId){
        String sql = "select w.name from workspace w join workspace_user wu on w.id=wu.workspace_id where wu.user_id=?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);
        List <String> workspaces=new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String name = (String) row.get("name"); 
            workspaces.add(name);
        }
        return ResponseEntity.ok(workspaces);
    }

    public ResponseEntity<List<User>> getUsersByTypeId() {
        int typeId = 1;
        try {
            String sql = "SELECT id, email, type_id FROM `user` WHERE type_id = ?";
            List<User> userList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), typeId);

            // Set password to null for each user
            for (User user : userList) {
                user.setPassword(null);
            }
            return ResponseEntity.ok(userList);
            //return new ApiResponse<>("Users retrieved successfully by type ID", userList, 200);
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving users by type ID: " + e.getLocalizedMessage();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>(errorMessage, null, 500);
        }
    }


    public ResponseEntity<String> updateUserTheme(boolean status) {
        SessionM sessionM = JWT.getSession(request);
        Integer userId = sessionM.getIdUsuario();
        String sql = "UPDATE user SET theme=? WHERE id=?";

        try {
            int updatedRows = jdbcTemplate.update(sql,status,userId);

            if (updatedRows > 0) {
                return ResponseEntity.ok("User theme status: "+status);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or no updates applied");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user");
        }
    }
}
