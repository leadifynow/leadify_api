package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.Booked;
import com.api.leadify.entity.Interested;
import com.api.leadify.service.BookedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/getByCompany/{companyId}")
    public ApiResponse<List<Booked>> getAllBookedByCompanyId(@PathVariable("companyId") int companyId) {
        return bookedService.getAllBookedByCompanyId(companyId);
    }
    @GetMapping("/search")
    public ApiResponse<List<Booked>> searchBookedRecords(@RequestParam String searchTerm, @RequestParam int companyId) {
        return bookedService.searchBookedRecords(searchTerm, companyId);
    }
    @PutMapping("/update/{interestedId}/{bookedId}")
    public ApiResponse<Void> updateBookedAndInterested(@PathVariable int interestedId, @PathVariable int bookedId) {
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
}
