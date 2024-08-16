package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.InterestedDao;
import com.api.leadify.dao.PaginatedResponse;
import com.api.leadify.entity.Interested;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/interested")
public class InterestedController {
   private final InterestedDao interestedDao;

    @Autowired
    public InterestedController(InterestedDao interestedDao) {
        this.interestedDao = interestedDao;
    }

    @PostMapping("/create")
    public void createInterested(@RequestBody Interested interested) {
        interestedDao.createInterested(interested);
    }
    @PostMapping("/update_stage")
    public void updateStage(@RequestParam(name = "stage_id") Integer stageId, @RequestParam(name = "interestedId") Integer interestedId) {
        interestedDao.updateStage(stageId, interestedId);
    }
    @GetMapping("/getAll")
    public List<Interested> getAll() {
        return interestedDao.getAll();
    }
    @GetMapping("/getAllByWorkspaceId/{workspaceId}/{page}/{pageSize}")
    public ResponseEntity<PaginatedResponse<List<Interested>>> getAllByWorkspaceId(@PathVariable UUID workspaceId, @PathVariable int page, @PathVariable int pageSize) {
        return interestedDao.getAllByWorkspaceId(workspaceId, page, pageSize);
    }
    @PutMapping("/updateStage/{interestedId}/{stageId}")
    public ResponseEntity<String> updateStage2(@PathVariable Integer interestedId, @PathVariable Integer stageId) {
        return interestedDao.updateStage2(interestedId, stageId);
    }
    @PutMapping("/updateManager/{interestedId}/{managerId}")
    public ResponseEntity<Void> updateManager(@PathVariable int interestedId, @PathVariable int managerId) {
        return interestedDao.updateManager(interestedId, managerId);
    }
    @PutMapping("/updateNotes/{interestedId}")
    public ResponseEntity<String> updateInterestedNotes(@PathVariable int interestedId, @RequestParam(required = false) String newNotes) {
        return interestedDao.updateInterestedNotes(interestedId, newNotes);
    }
    @GetMapping("/search")
    public ResponseEntity<List<Interested>> searchInterestedRecords(@RequestParam String searchTerm, @RequestParam UUID workspaceId) {
        return interestedDao.searchInterestedRecords(searchTerm, workspaceId);
    }
    @PutMapping("/date/{interestedId}/{nextUpdateDate}")
    public ResponseEntity<String> updateNextUpdateDate(
            @PathVariable Integer interestedId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextUpdateDate) {
        return interestedDao.updateNextUpdateDate(interestedId, nextUpdateDate);
    }
    @PostMapping("/createManual")
    public ResponseEntity<Void> createManualInterested(@RequestBody Interested interested) {
        return interestedDao.createManualInterested(interested);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterestedById(@PathVariable Integer id) {
        try {
            return interestedDao.deleteById(id);
        } catch (Exception e) {
            // Handle any unexpected exceptions
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/manager/{manager}")
    public ResponseEntity<List<Interested>> getAllByManagerAndBookedIsZero(@PathVariable Integer manager) {
        return interestedDao.getAllByManagerAndBookedIsZero(manager);
    }
    @PutMapping("/updateStages")
    public ResponseEntity<String> updateStageArray(@RequestBody JsonNode stageUpdates) {
        return  interestedDao.updateStageArray(stageUpdates);
    }
    @PutMapping("/updateStageCustomDate")
    public ResponseEntity<String> updateStageAndNextUpdateArray(@RequestBody JsonNode stageUpdates) {
        return interestedDao.updateStageAndNextUpdateArray(stageUpdates);
    }
}
