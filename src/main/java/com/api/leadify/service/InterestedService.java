package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.InterestedDao;
import com.api.leadify.entity.Interested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class InterestedService {
    private final InterestedDao interestedDao;

    @Autowired
    public InterestedService(InterestedDao interestedDao) {
        this.interestedDao = interestedDao;
    }
    public void createInterested(Interested interested) {
        interestedDao.createInterested(interested);
    }
    public void updateStage(Integer stageId, Integer interestedId) {
        interestedDao.updateStage(stageId, interestedId);
    }
    public List<Interested> getAll() {
        return interestedDao.getAll();
    }
    public ApiResponse<List<Interested>> getAllByWorkspaceId(UUID workspaceId) {
        return interestedDao.getAllByWorkspaceId(workspaceId);
    }
    public ApiResponse<String> updateStage2(Integer interestedId, Integer stageId) {
        return interestedDao.updateStage2(interestedId, stageId);
    }
    public ApiResponse<Void> updateManager(int interestedId, int managerId) {
        return interestedDao.updateManager(interestedId, managerId);
    }
    public ApiResponse<String> updateInterestedNotes(int interestedId, String newNotes) {
        // Call the DAO method to update interested notes
        return interestedDao.updateInterestedNotes(interestedId, newNotes);
    }
    public ApiResponse<List<Interested>> searchInterestedRecords(String searchTerm, UUID workspaceId) {
        return interestedDao.searchInterestedRecords(searchTerm, workspaceId);
    }
    public ApiResponse<String> updateNextUpdateDate(Integer interestedId, LocalDate nextUpdateDate) {
        return interestedDao.updateNextUpdateDate(interestedId, nextUpdateDate);
    }
    public ApiResponse<Void> createManualInterested(Interested interested) {
        return interestedDao.createManualInterested(interested);
    }
    public ApiResponse<Void> deleteInterestedById(Integer id) {
        try {
            return interestedDao.deleteById(id);
        } catch (Exception e) {
            // Handle any unexpected exceptions
            String errorMessage = "Error deleting interested record: " + e.getMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
}
