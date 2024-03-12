package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.Company;
import com.api.leadify.entity.User;
import com.api.leadify.entity.UserToken;
import com.api.leadify.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/delete/{userId}")
    public ApiResponse<String> deleteUser(@PathVariable Integer userId) {
        return userService.deleteUser(userId);
    }
    @PutMapping("/update")
    public ApiResponse<User> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }
    @PostMapping("/create")
    public ApiResponse<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
    @GetMapping("/getUsers")
    public ApiResponse<List<User>> getUsers() {
        return userService.getUsers();
    }
    @PostMapping(value = "/login", produces = "application/json")
    @ResponseBody
    public ApiResponse<UserToken> login(@RequestBody User user) {
        return userService.loginUser(user);
    }
    @GetMapping("/getUserCompanies/{userId}")
    public ApiResponse<List<Company>> getUserCompanies(@PathVariable Integer userId) {
        return userService.getUserCompanies(userId);
    }
    @GetMapping("/managers")
    public ApiResponse<List<User>> getUsersByTypeId() {
        return userService.getUsersByTypeId();
    }
}
