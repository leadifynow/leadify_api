package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.User;
import com.api.leadify.service.WorkspaceUserService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/workspace_user")
public class WorkspaceUserController {
    private final WorkspaceUserService workspaceUserService;

    @Autowired
    public WorkspaceUserController(WorkspaceUserService workspaceUserService) { this.workspaceUserService = workspaceUserService; }

    @GetMapping("/getByWorkspaceId/{workspaceId}")
    public ApiResponse<List<?>> getWorkspaceUsersByWorkspaceId(@PathVariable UUID workspaceId) {
        return workspaceUserService.getByWorkspaceId(workspaceId);
    }
    @DeleteMapping("/deleteByUserId/{userId}")
    public ApiResponse<String> deleteWorkspaceUserByUserId(@PathVariable int userId, @PathVariable UUID workspaceId) {
        return workspaceUserService.deleteByUserId(userId, workspaceId);
    }
    @PostMapping("/addUserToWorkspace")
    public ApiResponse<String> addUserToWorkspace(@RequestParam int userId, @RequestParam UUID workspaceId) {
        System.out.println(userId);
        return workspaceUserService.addUserToWorkspace(userId, workspaceId);
    }
    @GetMapping("/searchUsers")
    public ApiResponse<List<User>> searchUsers(@RequestParam String searchTerm, @RequestParam UUID workspaceId) {
        List<User> users = workspaceUserService.searchUsersNotInWorkspace(searchTerm, workspaceId);

        if (users != null) {
            return new ApiResponse<>("Users retrieved successfully", users, 200);
        } else {
            return new ApiResponse<>("Error retrieving users", null, 500);
        }
    }
}
