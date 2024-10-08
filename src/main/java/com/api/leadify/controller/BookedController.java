package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.BookedDao;
import com.api.leadify.dao.PaginatedResponse;
import com.api.leadify.entity.Booked;
import com.api.leadify.entity.EventName;
import com.api.leadify.entity.Interested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/booked")
public class BookedController {
    private final BookedDao bookedDao;

    @Autowired
    public BookedController(BookedDao bookedDao) {
        this.bookedDao = bookedDao;
    }

    @PostMapping("/{company_id}")
    public ResponseEntity<ApiResponse<?>> createBooked(@PathVariable("company_id") int companyId, @RequestBody Booked booked) {
        return bookedDao.createBooked(booked, companyId);
    }
    @GetMapping("/getByCompany/{companyId}/{workspace_id}/{page}/{pageSize}/{filterType}")
    public ResponseEntity<PaginatedResponse<List<Booked>>> getAllBookedByCompanyId(@PathVariable("companyId") int companyId, @PathVariable("workspace_id") String workspace_id, @PathVariable int page, @PathVariable int pageSize, @PathVariable int filterType,
                                                                                @RequestParam(required = false) String startDate,
                                                                                @RequestParam(required = false) String endDate) {
        return bookedDao.getAllBookedByCompanyId(companyId, workspace_id, page, pageSize, filterType, startDate, endDate);
    }
    @GetMapping("/get")
    public ResponseEntity<Page<Booked>> getAllBookedByCompanyId(
            @RequestParam(required = false) Integer companyId,
            @RequestParam String workspaceId,
            @RequestParam(defaultValue = "All") String match, // Match filter
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "workspace") String filterBy, // Filter by workspace or company
            @RequestParam(defaultValue = "Newest") String sortBy, // Sort by Newest, Oldest, or Last Updated
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        return bookedDao.getBooked(companyId, workspaceId, pageable, match, startDate, endDate, filterBy, sortBy);
    }
    @GetMapping("/searchAllBooked")
    public ResponseEntity<Page<Booked>> searchAllBookedByWorkspaceId(
            @RequestParam String workspaceId,
            @RequestParam(defaultValue = "", required = false) String search, // Search by Id or email
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        return bookedDao.SearchAllBooked(workspaceId, pageable, search);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Booked>> searchBookedRecords(@RequestParam String searchTerm, @RequestParam String workspace) {
        return bookedDao.searchInterestedToMatch(searchTerm, workspace);
    }
    @PutMapping("/update/{interestedId}/{bookedId}")
    public ResponseEntity<Booked> updateBookedAndInterested(@PathVariable int interestedId, @PathVariable int bookedId) {
        return bookedDao.updateBookedAndInterested(interestedId, bookedId);
    }
    @GetMapping("/getInterestedByBookedId/{bookedId}")
    public ResponseEntity<Interested> getInterestedByBookedId(@PathVariable int bookedId) {
        return bookedDao.getInterestedByBookedId(bookedId);
    }
    @PutMapping("/reset/{interestedId}")
    public ResponseEntity<Void> resetInterestedAndBooked(@PathVariable int interestedId) {
        return bookedDao.resetInterestedAndBooked(interestedId);
    }
    @GetMapping("/booked/{companyId}/{workspaceId}/{searchParam}/{page}/{pageSize}")
    public ResponseEntity<PaginatedResponse<List<Booked>>> findByCompanyIdAndWorkspaceId(
            @PathVariable int companyId,
            @PathVariable(required = false) String workspaceId,
            @PathVariable(required = false) String searchParam,
            @PathVariable int page,
            @PathVariable int pageSize
    ) {
        try {
            return bookedDao.findByCompanyIdAndWorkspaceId(companyId, workspaceId, searchParam, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PostMapping("/manual_creation")
    public ResponseEntity<?> createManualBooking (@RequestBody Booked booked) {
        return bookedDao.createManual(booked);
    }
    @PostMapping("delete/{bookedId}")
    public ResponseEntity<Void> deleteBooked(@PathVariable int bookedId) {
        return bookedDao.deleteBooked(bookedId);
    }
    @PutMapping("/update")
    public ResponseEntity<Booked> updateBooked(@RequestBody Booked booked) {
        return bookedDao.updateBooked(booked);
    }

    @GetMapping("/getEventName/{workspaceId}")
    public ResponseEntity<List<EventName>> getEventName(@PathVariable String workspaceId) {
        return bookedDao.getEventName(workspaceId);
    }

    @PutMapping("/updateEventName")
    public ResponseEntity<EventName> updateEventName(@RequestBody EventName event) {
        return bookedDao.updateEventName(event);
    }

}
