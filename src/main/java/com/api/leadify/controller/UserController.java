package com.api.leadify.controller;

import com.api.leadify.dao.UserDao;
import com.api.leadify.entity.Company;
import com.api.leadify.entity.User;
import com.api.leadify.entity.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {
    private final UserDao userDao;

    @Autowired
    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        return userDao.deleteUser(userId);
    }
    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        return userDao.updateUser(user);
    }
    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return userDao.createUser(user);
    }
    @GetMapping("/getUsers")
    public ResponseEntity<List<User>> getUsers(@RequestParam (required = false, defaultValue = "") String search, @RequestParam (required = false,defaultValue = "0") Integer SortOpc,@RequestParam (required = false,defaultValue = "0") Integer GroupOpc) {
        return userDao.getUsers(search,SortOpc,GroupOpc);
    }

    @GetMapping("/getPass/{userId}")
    public ResponseEntity<String> getUserPassword(@PathVariable Integer userId) {
        return userDao.getUserPassword(userId);
    }

    @PostMapping(value = "/login", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody User user) {
        return userDao.loginUser(user);
    }
    @GetMapping("/getUserCompanies/{userId}")
    public ResponseEntity<List<Company>> getUserCompanies(@PathVariable Integer userId) {
        return userDao.getUserCompanies(userId);
    }

    @GetMapping("/getUserWorkspaces/{userId}")
    public ResponseEntity<List<String>> getUserWorkspaces(@PathVariable Integer userId) {
        return userDao.getUserWorkspaces(userId);
    }

    @GetMapping("/managers")
    public ResponseEntity<List<User>> getUsersByTypeId() {
        return userDao.getUsersByTypeId();
    }

    @PutMapping("/theme/{status}")
    public ResponseEntity<String> updateUserTheme(@PathVariable boolean status) {
        return userDao.updateUserTheme(status);
    }
}
