package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.WorkspaceUserDao;
import com.api.leadify.entity.User;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/workspace_user")
public class WorkspaceUserController {
   private final WorkspaceUserDao workspaceUserDao;

    @Autowired
    public WorkspaceUserController(WorkspaceUserDao workspaceUserDao) { this.workspaceUserDao = workspaceUserDao; }

    @GetMapping("/getByWorkspaceId/{workspaceId}")
    public ResponseEntity<List<?>> getWorkspaceUsersByWorkspaceId(@PathVariable UUID workspaceId) {
        return workspaceUserDao.getByWorkspaceId(workspaceId);
    }

    @DeleteMapping("/deleteByUserId/{userId}")
    public ResponseEntity<String> deleteWorkspaceUserByUserId(@PathVariable int userId) {
        return workspaceUserDao.deleteByUserId(userId);
    }
    @PostMapping("/addUserToWorkspace")
    public ResponseEntity<String> addUserToWorkspace(@RequestParam int userId, @RequestParam UUID workspaceId) {
        System.out.println(userId);
        return workspaceUserDao.addUserToWorkspace(userId, workspaceId);
    }
    @GetMapping("/searchUsers")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String searchTerm, @RequestParam UUID workspaceId) {
        List<User> users = workspaceUserDao.searchUsersNotInWorkspace(searchTerm, workspaceId);

        if (users != null) {
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
