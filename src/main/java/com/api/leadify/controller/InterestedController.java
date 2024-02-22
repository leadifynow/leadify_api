package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.Interested;
import com.api.leadify.service.InterestedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/interested")
public class InterestedController {
    private final InterestedService interestedService;

    @Autowired
    public InterestedController(InterestedService interestedService) {
        this.interestedService = interestedService;
    }
    @PostMapping("/create")
    public void createInterested(@RequestBody Interested interested) {
        interestedService.createInterested(interested);
    }
    @PostMapping("/update_stage")
    public void updateStage(@RequestParam(name = "stage_id") Integer stageId, @RequestParam(name = "interestedId") Integer interestedId) {
        interestedService.updateStage(stageId, interestedId);
    }
    @GetMapping("/getAll")
    public List<Interested> getAll() {
        return interestedService.getAll();
    }
    @GetMapping("/getAllByWorkspaceId/{workspaceId}")
    public ApiResponse<List<Interested>> getAllByWorkspaceId(@PathVariable UUID workspaceId) {
        return interestedService.getAllByWorkspaceId(workspaceId);
    }
    @PutMapping("/updateStage/{interestedId}/{stageId}")
    public ApiResponse<String> updateStage2(@PathVariable Integer interestedId, @PathVariable Integer stageId) {
        return interestedService.updateStage2(interestedId, stageId);
    }
    @PutMapping("/updateManager/{interestedId}/{managerId}")
    public ApiResponse<Void> updateManager(@PathVariable int interestedId, @PathVariable int managerId) {
        return interestedService.updateManager(interestedId, managerId);
    }
    @PutMapping("/updateNotes/{interestedId}/{newNotes}")
    public ApiResponse<String> updateInterestedNotes(@PathVariable int interestedId, @PathVariable String newNotes) {
        return interestedService.updateInterestedNotes(interestedId, newNotes);
    }
    @GetMapping("/search")
    public ApiResponse<List<Interested>> searchInterestedRecords(@RequestParam String searchTerm, @RequestParam UUID workspaceId) {
        return interestedService.searchInterestedRecords(searchTerm, workspaceId);
    }
    @PutMapping("/date/{interestedId}/{nextUpdateDate}")
    public ApiResponse<String> updateNextUpdateDate(
            @PathVariable Integer interestedId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextUpdateDate) {
        return interestedService.updateNextUpdateDate(interestedId, nextUpdateDate);
    }
    @PostMapping("/createManual")
    public ApiResponse<Void> createManualInterested(@RequestBody Interested interested) {
        return  interestedService.createManualInterested(interested);
    }
}
