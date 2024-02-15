package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.UserColumns;
import com.api.leadify.service.UserColumnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/user_columns")
public class UserColumnsController {

    private final UserColumnsService userColumnsService;

    @Autowired
    public UserColumnsController(UserColumnsService userColumnsService) {
        this.userColumnsService = userColumnsService;
    }

    @GetMapping("/{userId}/{workspaceId}")
    public ApiResponse<List<UserColumns>> getUserColumnsByUserIdAndWorkspaceId(@PathVariable Integer userId, @PathVariable String workspaceId) {
        return userColumnsService.getUserColumnsByUserIdAndWorkspaceId(userId, workspaceId);
    }
    @PutMapping("/update")
    public ApiResponse<UserColumns> updateUserColumns(@RequestBody UserColumns userColumns) {
        return userColumnsService.updateUserColumns(userColumns);
    }
}