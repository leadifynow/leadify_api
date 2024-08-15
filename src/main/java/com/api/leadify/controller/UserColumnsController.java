package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.UserColumnsDao;
import com.api.leadify.entity.UserColumns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/user_columns")
public class UserColumnsController {

    private final UserColumnsDao userColumnsDao;

    @Autowired
    public UserColumnsController(UserColumnsDao userColumnsDao) {
        this.userColumnsDao = userColumnsDao;
    }

    @GetMapping("/{userId}/{workspaceId}")
    public ResponseEntity<List<UserColumns>> getUserColumnsByUserIdAndWorkspaceId(@PathVariable Integer userId, @PathVariable String workspaceId) {
        return userColumnsDao.getUserColumnsByUserIdAndWorkspaceId(userId, workspaceId);
    }
    @PutMapping("/update")
    public ResponseEntity<UserColumns> updateUserColumns(@RequestBody UserColumns userColumns) {
        return userColumnsDao.updateUserColumns(userColumns);
    }
}