package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.UserDao;
import com.api.leadify.entity.Company;
import com.api.leadify.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }
    public ApiResponse<String> deleteUser(Integer userId) {
        return userDao.deleteUser(userId);
    }
    public ApiResponse<User> updateUser(User user) {
        return userDao.updateUser(user);
    }
    public ApiResponse<User> createUser(User user) {
        return  userDao.createUser(user);
    }
    public ApiResponse<List<User>> getUsers() {
        return userDao.getUsers();
    }
    public ApiResponse<User> loginUser(User user) {
        return userDao.loginUser(user);
    }
    public ApiResponse<List<Company>> getUserCompanies(Integer userId) {
        return userDao.getUserCompanies(userId);
    }
    public ApiResponse<List<User>> getUsersByTypeId() {
        return userDao.getUsersByTypeId();
    }
}
