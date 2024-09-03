package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.WorkspaceDao;
import com.api.leadify.entity.Workspace;
import com.api.leadify.entity.WorkspaceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/workspace")
public class WorkspaceController {
   private final WorkspaceDao workspaceDao;

    @Autowired
    public WorkspaceController(WorkspaceDao workspaceDao) { this.workspaceDao = workspaceDao; }


    @GetMapping("/getWorkspaces")
    public ResponseEntity<?> getWorkspaces( 
        @RequestParam(required = false, defaultValue = "") String ClientName,
        @RequestParam(required = false, defaultValue = "0") Integer GroupOpc,
        @RequestParam(required = false, defaultValue = "0") Integer orderBy)  {
        return workspaceDao.getAllOld(ClientName,GroupOpc,orderBy);
    }
    @PutMapping("/updateWorkspace")
    public ResponseEntity<Workspace> updateWorkspace(@RequestBody Workspace workspace) {
        return workspaceDao.updateWorkspace(workspace);
    }
    @GetMapping("/getByCompanyId")
    public ResponseEntity<List<Workspace>> getWorkspacesByCompanyId(@RequestParam int companyId) {
        return workspaceDao.getWorkspacesByCompanyId(companyId);
    }

    @PutMapping("/favoriteWorkspace/{workspaceId}/{status}")
    public ResponseEntity<String> updateFavWorkspace(@PathVariable String workspaceId,@PathVariable boolean status) {
        return workspaceDao.updateFavWorkspace(workspaceId,status);
    }
}