package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.ReportsDao;
import com.api.leadify.entity.Report;
import com.api.leadify.entity.ReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final ReportsDao reportsDao;

    @Autowired
    public ReportService(ReportsDao reportsDao) {
        this.reportsDao = reportsDao;
    }

    public ApiResponse<ReportResponse> getReport(String workspace, String[] dates) {
        return reportsDao.getReport(workspace, dates);
    }

}
