package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.PaginatedResponse;
import com.api.leadify.entity.Booked;
import com.api.leadify.entity.Interested;
import com.api.leadify.service.BookedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/booked")
public class BookedController {

    private final BookedService bookedService;

    @Autowired
    public BookedController(BookedService bookedService) {
        this.bookedService = bookedService;
    }

    @PostMapping("/{company_id}")
    public ApiResponse<Void> createBooked(@PathVariable("company_id") int companyId, @RequestBody Booked booked) {
        return bookedService.createBooked(companyId, booked);
    }
    @GetMapping("/getByCompany/{companyId}/{workspace_id}/{page}/{pageSize}/{filterType}")
    public ApiResponse<PaginatedResponse<List<Booked>>> getAllBookedByCompanyId(@PathVariable("companyId") int companyId, @PathVariable("workspace_id") String workspace_id, @PathVariable int page, @PathVariable int pageSize, @PathVariable int filterType) {
        return bookedService.getAllBookedByCompanyId(companyId, workspace_id, page, pageSize, filterType);
    }
    @GetMapping("/search")
    public ApiResponse<List<Booked>> searchBookedRecords(@RequestParam String searchTerm, @RequestParam int companyId) {
        return bookedService.searchBookedRecords(searchTerm, companyId);
    }
    @PutMapping("/update/{interestedId}/{bookedId}")
    public ApiResponse<Booked> updateBookedAndInterested(@PathVariable int interestedId, @PathVariable int bookedId) {
        return bookedService.updateBookedAndInterested(interestedId, bookedId);
    }
    @GetMapping("/getInterestedByBookedId/{bookedId}")
    public ApiResponse<Interested> getInterestedByBookedId(@PathVariable int bookedId) {
        return bookedService.getInterestedByBookedId(bookedId);
    }
    @PutMapping("/reset/{interestedId}")
    public ApiResponse<Void> resetInterestedAndBooked(@PathVariable int interestedId) {
        return bookedService.resetInterestedAndBooked(interestedId);
    }
    @GetMapping("/booked/{companyId}/{workspaceId}/{searchParam}/{page}/{pageSize}")
    public ApiResponse<PaginatedResponse<List<Booked>>> findByCompanyIdAndWorkspaceId(
            @PathVariable int companyId,
            @PathVariable(required = false) String workspaceId,
            @PathVariable(required = false) String searchParam,
            @PathVariable int page,
            @PathVariable int pageSize
    ) {
        return bookedService.findByCompanyIdAndWorkspaceId(companyId, workspaceId, searchParam, page, pageSize);
    }
    @PostMapping("/manual_creation")
    public ApiResponse<Void> createManualBooking (@RequestBody Booked booked) {
        return bookedService.createManualBooking(booked);
    }
    @PostMapping("delete/{bookedId}")
    public ApiResponse<Void> deleteBooked(@PathVariable int bookedId) {
        return  bookedService.deleteBooked(bookedId);
    }
}
