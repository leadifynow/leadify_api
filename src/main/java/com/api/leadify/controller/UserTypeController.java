package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.UserTypeDao;
import com.api.leadify.entity.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/user_types")
public class UserTypeController {
   private final UserTypeDao userTypeDao;

    @Autowired
    public UserTypeController(UserTypeDao userTypeDao) {
        this.userTypeDao = userTypeDao;
    }

    @GetMapping("/getAll")
    public ResponseEntity <List<UserType>> getUserTypes() {
        return userTypeDao.getUserTypes();
    }

    @PostMapping("/create")
    public void createdUserTpye(@RequestBody UserType userType) {
        userTypeDao.createdUserType(userType);

    }
}
