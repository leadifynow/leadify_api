package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.Report;
import com.api.leadify.entity.ReportResponse;
import com.api.leadify.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/report")
public class ReportController {
    private final ReportService reportService;
    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/main/{workspace}/{dates}")
    public ApiResponse<ReportResponse> getMainReport(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportService.getReport(workspace, dates);
    }

}
