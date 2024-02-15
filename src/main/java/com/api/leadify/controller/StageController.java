package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.Stage;
import com.api.leadify.service.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/stages")
public class StageController {
    private final StageService stageService;

    @Autowired
    public StageController(StageService stageService) {
        this.stageService = stageService;
    }

    @GetMapping("/getByWorkspaceId/{workspaceId}")
    public ApiResponse<List<Stage>> getStagesByWorkspaceId(@PathVariable UUID workspaceId) {
        return stageService.getStagesByWorkspaceId(workspaceId);
    }
    @PostMapping("/updatePositions")
    public ApiResponse<String> updatePositions(@RequestBody List<Stage> stages) {
        return stageService.updatePositions(stages);
    }
    @PostMapping("/createStage")
    public ApiResponse<Integer> createStage(@RequestBody Stage newStage) {
        return stageService.createStage(newStage);
    }
}