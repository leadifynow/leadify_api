package com.api.leadify.controller;

import com.api.leadify.dao.StageDao;
import com.api.leadify.entity.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/stages")
public class StageController {
     private final StageDao stageDao;

    @Autowired
    public StageController(StageDao stageDao) {
        this.stageDao = stageDao;
    }

    @GetMapping("/getByWorkspaceId/{workspaceId}")
    public ResponseEntity<List<Stage>> getStagesByWorkspaceId(@PathVariable UUID workspaceId) {
        return stageDao.getStagesByWorkspaceId(workspaceId);
    }
    @PostMapping("/updatePositions")
    public ResponseEntity<String> updatePositions(@RequestBody List<Stage> stages) {
        return stageDao.updatePositions(stages);
    }
    @PostMapping("/createStage")
    public ResponseEntity<Integer> createStage(@RequestBody Stage newStage) {
        return stageDao.createStage(newStage);
    }
}