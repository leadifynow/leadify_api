package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.UserType;
import com.api.leadify.service.UserTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/user_types")
public class UserTypeController {
    private final UserTypeService userTypeService;

    @Autowired
    public UserTypeController(UserTypeService userTypeService) {
        this.userTypeService = userTypeService;
    }

    @GetMapping("/getAll")
    public ApiResponse<List<UserType>> getUserTypes() {
        return userTypeService.getUserTypes();
    }

    @PostMapping("/create")
    public void createdUserTpye(@RequestBody UserType userType) {
        userTypeService.createUserType(userType);
    }
}
