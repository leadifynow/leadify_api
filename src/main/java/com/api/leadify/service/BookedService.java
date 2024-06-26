package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.BookedDao;
import com.api.leadify.dao.PaginatedResponse;
import com.api.leadify.entity.Booked;
import com.api.leadify.entity.Interested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookedService {
    private final BookedDao bookedDao;

    @Autowired
    public BookedService(BookedDao bookedDao) {
        this.bookedDao = bookedDao;
    }

    public ApiResponse<Void> createBooked(int companyId, Booked booked) {
        return bookedDao.createBooked(booked, companyId);
    }
    public ApiResponse<PaginatedResponse<List<Booked>>> getAllBookedByCompanyId(int companyId, String workspaceId, int page, int pageSize, int filterType, String startDate, String endDate) {
        return bookedDao.getAllBookedByCompanyId(companyId, workspaceId, page, pageSize, filterType, startDate, endDate);
    }
    public ApiResponse<List<Booked>> searchBookedRecords(String searchTerm, int companyId, String workspace) {
        return bookedDao.searchBookedRecords(searchTerm, companyId, workspace);
    }
    public ApiResponse<Booked> updateBookedAndInterested(int interestedId, int bookedId) {
        return bookedDao.updateBookedAndInterested(interestedId, bookedId);
    }
    public ApiResponse<Interested> getInterestedByBookedId(int bookedId) {
        return bookedDao.getInterestedByBookedId(bookedId);
    }
    public ApiResponse<Void> resetInterestedAndBooked(int interestedId) {
        return bookedDao.resetInterestedAndBooked(interestedId);
    }
    public ApiResponse<PaginatedResponse<List<Booked>>> findByCompanyIdAndWorkspaceId(int companyId, String workspaceId, String searchParam, int page, int pageSize) {
        try {
            return bookedDao.findByCompanyIdAndWorkspaceId(companyId, workspaceId, searchParam, page, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Error retrieving bookings by company ID and workspace ID", null, 500);
        }
    }
    public ApiResponse<Void> createManualBooking(Booked booked) {
        return bookedDao.createManual(booked);
    }
    public ApiResponse<Void> deleteBooked(int bookedId) {
        return bookedDao.deleteBooked(bookedId);
    }
}
