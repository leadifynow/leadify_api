package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.StageDao;
import com.api.leadify.entity.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StageService {
    private final StageDao stageDao;

    @Autowired
    public StageService(StageDao stageDao) {
        this.stageDao = stageDao;
    }

    public ApiResponse<List<Stage>> getStagesByWorkspaceId(UUID workspaceId) {
        return stageDao.getStagesByWorkspaceId(workspaceId);
    }
    public ApiResponse<String> updatePositions(List<Stage> stages) {
        return stageDao.updatePositions(stages);
    }
    public ApiResponse<Integer> createStage(Stage newStage) {
        return stageDao.createStage(newStage);
    }
}