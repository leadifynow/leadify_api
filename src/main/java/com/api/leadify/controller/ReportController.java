package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.ReportsDao;
import com.api.leadify.entity.Report;
import com.api.leadify.entity.ReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/report")
public class ReportController {
     private final ReportsDao reportsDao;

    @Autowired
    public ReportController(ReportsDao reportsDao) {
        this.reportsDao = reportsDao;
    }

    @GetMapping("/main/{workspace}/{dates}")
    public ResponseEntity<ReportResponse> getMainReport(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getReport(workspace, dates);
    }

    @GetMapping("/appointments/{workspace}/{dates}")
    public ResponseEntity<?> getAppointments(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getAppointmentsByCampaignRows(workspace, dates);
    }

    @GetMapping("/campaign/{workspace}/{dates}")
    public ResponseEntity<?> getCampaignData (@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getCampaignData(workspace, dates);
    }

    @GetMapping("/emailOccurrence/{workspace}/{dates}")
    public ResponseEntity<?> getEmailOccurrence (@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getEmailOccurrences(workspace, dates);
    }

    @GetMapping("/stageData/{workspace}/{dates}")
    public ResponseEntity<?> getStageData (@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getstageData(workspace, dates);
    }
}
