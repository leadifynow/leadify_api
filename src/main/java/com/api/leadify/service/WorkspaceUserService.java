package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.WorkspaceUserDao;
import com.api.leadify.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceUserService {
    private final WorkspaceUserDao workspaceUserDao;

    @Autowired
    public WorkspaceUserService(WorkspaceUserDao workspaceUserDao) { this.workspaceUserDao = workspaceUserDao; }

    public ApiResponse<List<?>> getByWorkspaceId(UUID workspaceId) {
        return workspaceUserDao.getByWorkspaceId(workspaceId);
    }
    public ApiResponse<String> deleteByUserId(int userId, UUID workspaceId) {
        return workspaceUserDao.deleteByUserId(userId, workspaceId);
    }
    public ApiResponse<String> addUserToWorkspace(int userId, UUID workspaceId) {
        return workspaceUserDao.addUserToWorkspace(userId, workspaceId);
    }
    public List<User> searchUsersNotInWorkspace(String searchTerm, UUID workspaceId) {
        return workspaceUserDao.searchUsersNotInWorkspace(searchTerm, workspaceId);
    }
}
