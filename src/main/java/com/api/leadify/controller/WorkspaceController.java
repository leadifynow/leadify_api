package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.Workspace;
import com.api.leadify.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/workspace")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    @Autowired
    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }
    @GetMapping("/getWorkspaces")
    public ApiResponse<List<Workspace>> getWorkspaces() {
        return workspaceService.getAllWorkspaces();
    }
    @PutMapping("/updateWorkspace")
    public ApiResponse<Workspace> updateWorkspace(@RequestBody Workspace workspace) {
        return workspaceService.updateWorkspace(workspace);
    }
    @GetMapping("/getByCompanyId")
    public ApiResponse<List<Workspace>> getWorkspacesByCompanyId(@RequestParam int companyId) {
        return workspaceService.getWorkspacesByCompanyId(companyId);
    }
}