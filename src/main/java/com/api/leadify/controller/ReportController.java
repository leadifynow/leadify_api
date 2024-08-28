package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.ReportsDao;
import com.api.leadify.entity.Report;
import com.api.leadify.entity.ReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/stats/1/{workspace}/{dates}")
    public ResponseEntity<?> getLeadsGeneral(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getDataReport(workspace, dates);
    }

    /* @GetMapping("/stats/2/{workspace}/{dates}")
    public ResponseEntity<?> getBookedGeneral (@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getDataReport(workspace, dates);
    }
         @GetMapping("/stats/3/{workspace}/{dates}")
    public ResponseEntity<?> getEmailsGeneral (@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getDataReport(workspace, dates);
    }
         @GetMapping("/stats/4/{workspace}/{dates}")
    public ResponseEntity<?> getMeetsGeneral (@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getDataReport(workspace, dates);
    }
         @GetMapping("/stats/5/{workspace}/{dates}")
    public ResponseEntity<?> getLeadsWorkspace(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getDataReport(workspace, dates);
    }
         @GetMapping("/stats/6/{workspace}/{dates}")
    public ResponseEntity<?> getBookedWorkspace (@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getDataReport(workspace, dates);
    }
         @GetMapping("/stats/7/{workspace}/{dates}")
    public ResponseEntity<?> getEmailsWorkspace (@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getDataReport(workspace, dates);
    } 
         @GetMapping("/stats/8/{workspace}/{dates}")
    public ResponseEntity<?> getMeetsWorkspace (@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getDataReport(workspace, dates);
    }*/

    @GetMapping("/data/1/{workspace}/{dates}")
    public List<String> getParamAppoint(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getAppoint(workspace, dates);
    }
    @GetMapping("/data/2/{workspace}/{dates}")
    public List<String> getParamCamp(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getCampign(workspace, dates);
    }
    @GetMapping("/data/3/{workspace}/{dates}")
    public List<String> getParamBookeed(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getTimes(workspace, dates);
    }
    @GetMapping("/data/4/{workspace}/{dates}")
    public List<String> getParamStage(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates) {
        return reportsDao.getStage(workspace, dates);
    }

    @GetMapping("/filter/appointment/{workspace}/{dates}")
    public ResponseEntity<?> getFilterAppointment(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates, @RequestParam String CampName) {
        return reportsDao.getFilterAppointments(workspace, dates,CampName);
    }

    @GetMapping("/filter/campaign/{workspace}/{dates}")
    public ResponseEntity<?> getFilterCampaign(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates, @RequestParam String CampName) {
        return reportsDao.getFilterCampaign(workspace, dates,CampName);
    }

    @GetMapping("/filter/email/{workspace}/{dates}")
    public ResponseEntity<?> getFilterEmails(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates, @RequestParam Integer bookedNum) {
        return reportsDao.getFilterEmail(workspace, dates,bookedNum);
    }

    /*
     * @GetMapping("/filter/step/{workspace}/{dates}")
    public ResponseEntity<?> getFilterSteps(@PathVariable("workspace") String workspace, @PathVariable("dates") String[] dates, @RequestParam String Stage) {
        return reportsDao.getFilterStage(workspace, dates,StageId);
    }
     */
}
