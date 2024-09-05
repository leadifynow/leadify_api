package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.WorkspaceDao;
import com.api.leadify.entity.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class WorkspaceService {
    private final WorkspaceDao workspaceDao;

    @Autowired
    public WorkspaceService(WorkspaceDao workspaceDao) {
        this.workspaceDao = workspaceDao;
    }

    public ApiResponse<List<Workspace>> getAllWorkspaces() {
        return workspaceDao.getAll();
    }

    public ApiResponse<Workspace> updateWorkspace(Workspace workspace) {
        return workspaceDao.updateWorkspace(workspace);
    }

    public ApiResponse<List<Workspace>> getWorkspacesByCompanyId(int companyId, HttpServletRequest request) {
        // Pass the request to retrieve the user from JWT token
        return workspaceDao.getWorkspacesByCompanyId(companyId, request);
    }
}
