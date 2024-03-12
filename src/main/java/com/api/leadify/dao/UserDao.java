package com.api.leadify.dao;

import com.api.leadify.entity.Company;
import com.api.leadify.entity.User;
import com.api.leadify.entity.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.api.leadify.jwt.JWT;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public ApiResponse<String> deleteUser(Integer userId) {
        String sql = "DELETE FROM user WHERE id = ?";

        try {
            int deletedRows = jdbcTemplate.update(sql, userId);

            if (deletedRows > 0) {
                return new ApiResponse<>("User deleted successfully", null, 200);
            } else {
                return new ApiResponse<>("User not found or already deleted", null, 404);
            }
        } catch (Exception e) {
            return new ApiResponse<>("Error deleting user", null, 500);
        }
    }
    public ApiResponse<User> updateUser(User updatedUser) {
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
                User updatedUserData = getUserById(updatedUser.getId());

                return new ApiResponse<>("User updated successfully", updatedUserData, 200);
            } else {
                return new ApiResponse<>("User not found or no updates applied", null, 404);
            }
        } catch (Exception e) {
            return new ApiResponse<>("Error updating user", null, 500);
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
    public ApiResponse<User> createUser(User user) {
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

            // Fetch the created user to include in the ApiResponse

            return new ApiResponse<>("User created successfully", null, 201);
        } catch (Exception e) {
            return new ApiResponse<>("Error creating user", null, 500);
        }
    }
    public ApiResponse<List<User>> getUsers() {
        String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.password, u.created_at, u.updated_at, u.type_id, ut.name as type_name " +
                "FROM user u " +
                "JOIN user_type ut ON u.type_id = ut.id " +
                "WHERE u.type_id = 2";

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

            List<User> userList = new ArrayList<>();

            for (Map<String, Object> row : rows) {
                User user = new User();
                user.setId((Integer) row.get("id"));
                user.setFirst_name((String) row.get("first_name"));
                user.setLast_name((String) row.get("last_name"));
                user.setEmail((String) row.get("email"));
                user.setPassword((String) row.get("password"));
                user.setType_id((Integer) row.get("type_id"));
                user.setType_name((String) row.get("type_name")); // New field for user_type name

                userList.add(user);
            }

            if (userList.isEmpty()) {
                return new ApiResponse<>("No users found", null, 404);
            } else {
                return new ApiResponse<>("Users retrieved successfully", userList, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return new ApiResponse<>("Error retrieving users", null, 500);
        }
    }
    public ApiResponse<UserToken> loginUser(User user) {
        String sql = "SELECT id, first_name, last_name, email, type_id FROM user WHERE email = ? AND password = ?";
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

            return new ApiResponse<>("Welcome back " + result.get("email") + "!", loggedInUser, 200);
        } catch (EmptyResultDataAccessException e) {
            // User not found
        }

        return new ApiResponse<>("Username or password do not exist", null, 404);
    }
    public ApiResponse<List<Company>> getUserCompanies(Integer userId) {
        String sql = "SELECT DISTINCT c.id, c.name, c.location, c.flag, c.industry_id " +
                "FROM user u " +
                "JOIN user_type ut ON u.type_id = ut.id " +
                "JOIN workspace_user wu ON u.id = wu.user_id " +
                "JOIN workspace w ON wu.workspace_id = w.id " +
                "JOIN company c ON w.company_id = c.id " +
                "WHERE u.id = ?";

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);

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
                return new ApiResponse<>("No companies found for the given user", null, 404);
            } else {
                return new ApiResponse<>("Companies retrieved successfully", companyList, 200);
            }
        } catch (EmptyResultDataAccessException e) {
            return new ApiResponse<>("Error retrieving companies", null, 500);
        }
    }
    public ApiResponse<List<User>> getUsersByTypeId() {
        int typeId = 1;
        try {
            String sql = "SELECT id, email, type_id FROM `user` WHERE type_id = ?";
            List<User> userList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), typeId);

            // Set password to null for each user
            for (User user : userList) {
                user.setPassword(null);
            }

            return new ApiResponse<>("Users retrieved successfully by type ID", userList, 200);
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving users by type ID: " + e.getLocalizedMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
}
