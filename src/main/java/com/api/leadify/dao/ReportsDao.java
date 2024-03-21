package com.api.leadify.dao;

import com.api.leadify.entity.Report;
import com.api.leadify.entity.ReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReportsDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReportsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ApiResponse<ReportResponse> getReport(String workspace, String[] dates) {
        String totalInterestedQuery = "SELECT COUNT(*) FROM interested WHERE workspace = ? AND created_at BETWEEN ? AND ?";
        String totalBookedMatchedQuery = "SELECT COUNT(*) FROM booked WHERE interested_id IS NOT NULL AND workspace_id = ? AND created_at BETWEEN ? AND ?";
        String uniqueEmailBookedMatchQuery = "SELECT COUNT(DISTINCT email) FROM booked WHERE interested_id IS NOT NULL AND workspace_id = ? AND created_at BETWEEN ? AND ?";
        String totalInterestedAndBookedNonMatchedQuery = "SELECT COUNT(*) FROM booked WHERE interested_id IS NULL AND workspace_id = ? AND created_at BETWEEN ? AND ?";
        String totalBookedQuery = "SELECT COUNT(*) FROM booked WHERE created_at BETWEEN ? AND ?";
        String uniqueEmailGeneralQuery = "SELECT COUNT(DISTINCT email) FROM booked WHERE created_at BETWEEN ? AND ?";
        String workspaceNameQuery = "SELECT name FROM workspace WHERE id = ?";
        String allCallsQuery = "SELECT COUNT(*) FROM booked WHERE meeting_date BETWEEN ? AND ?";
        String allCallsBookedQuery = "SELECT COUNT(*) FROM booked WHERE workspace_id = ? AND interested_id IS NOT NULL AND meeting_date BETWEEN ? AND ?";
        String allInterestedQuery = "SELECT COUNT(*) FROM interested WHERE created_at BETWEEN ? AND ?";
        String stagesQuery = "SELECT id, name FROM stage WHERE workspace_id = ? ORDER BY position_workspace";
        String campaignQuery = "SELECT campaign_name, COUNT(*) AS count FROM interested WHERE workspace = ? AND created_at BETWEEN ? AND ? GROUP BY campaign_name";
        String appointmentsByCampaignQuery = "SELECT campaign_name, COUNT(*) AS count FROM booked b INNER JOIN interested i ON b.interested_id = i.id WHERE i.workspace = ? AND interested_id IS NOT NULL AND b.meeting_date BETWEEN ? AND ? GROUP BY campaign_name";

        Integer totalInterested = null;
        Integer totalBookedMatched = null;
        Integer uniqueEmailsBookedMatched = null;
        Integer totalInterestedAndBookedNonMatched = null;
        Integer totalBooked = null;
        Integer uniqueEmailGeneral = null;
        String workspaceName = null;
        Integer allCalls = null;
        Integer allCallsBooked = null;
        Integer allInterested = null;
        List<Map<String, Object>> campaignDataList = new ArrayList<>();
        List<Map<String, Object>> stageDataList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        List<Map<String, Object>> appointmentsByCampaignList = new ArrayList<>();

        // Set time to 00:00:00.000 for both start and end dates
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);

        try {
            workspaceName = jdbcTemplate.queryForObject(workspaceNameQuery, String.class, workspace);
            totalInterested = jdbcTemplate.queryForObject(totalInterestedQuery, Integer.class, workspace, startDate, endDate);
            totalBookedMatched = jdbcTemplate.queryForObject(totalBookedMatchedQuery, Integer.class, workspace, startDate, endDate);
            uniqueEmailsBookedMatched = jdbcTemplate.queryForObject(uniqueEmailBookedMatchQuery, Integer.class, workspace, startDate, endDate);
            totalInterestedAndBookedNonMatched = jdbcTemplate.queryForObject(totalInterestedAndBookedNonMatchedQuery, Integer.class, workspace, startDate, endDate);
            totalBooked = jdbcTemplate.queryForObject(totalBookedQuery, Integer.class, startDate, endDate);
            uniqueEmailGeneral = jdbcTemplate.queryForObject(uniqueEmailGeneralQuery, Integer.class, startDate, endDate);
            allCalls = jdbcTemplate.queryForObject(allCallsQuery, Integer.class, startDate, endDate);
            allCallsBooked = jdbcTemplate.queryForObject(allCallsBookedQuery, Integer.class, workspace, startDate, endDate);
            allInterested = jdbcTemplate.queryForObject(allInterestedQuery, Integer.class, startDate, endDate);

            // Fetch stage data
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(stagesQuery, workspace);
            for (Map<String, Object> row : rows) {
                Integer stageId = (Integer) row.get("id");
                String stageName = (String) row.get("name");
                // Count interested per stage
                Integer interestedPerStage = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM interested WHERE workspace = ? AND stage_id = ? AND created_at BETWEEN ? AND ?", Integer.class, workspace, stageId, startDate, endDate);
                Map<String, Object> stageData = new HashMap<>();
                stageData.put("stageName", stageName);
                stageData.put("interestedCount", interestedPerStage);
                stageDataList.add(stageData);
            }
            Integer interestedNullStage = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM interested WHERE workspace = ? AND stage_id IS NULL AND created_at BETWEEN ? AND ?", Integer.class, workspace, startDate, endDate);
            if (interestedNullStage != null && interestedNullStage > 0) {
                Map<String, Object> nullStageData = new HashMap<>();
                nullStageData.put("stageName", "Not in stage");
                nullStageData.put("interestedCount", interestedNullStage);
                stageDataList.add(nullStageData);
            }

            List<Map<String, Object>> campaignRows = jdbcTemplate.queryForList(campaignQuery, workspace, startDate, endDate);
            for (Map<String, Object> row : campaignRows) {
                String campaignName = (String) row.get("campaign_name");
                Integer interestedCount = ((Number) row.get("count")).intValue();
                Map<String, Object> campaignData = new HashMap<>();
                campaignData.put("campaignName", campaignName != null ? campaignName : "Not specified");
                campaignData.put("interestedCount", interestedCount);
                campaignDataList.add(campaignData);
            }

            // Fetch count of interested where campaign_name is null
            Integer interestedNullCampaign = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM interested WHERE workspace = ? AND campaign_name IS NULL AND created_at BETWEEN ? AND ?", Integer.class, workspace, startDate, endDate);
            if (interestedNullCampaign != null && interestedNullCampaign > 0) {
                Map<String, Object> nullCampaignData = new HashMap<>();
                nullCampaignData.put("campaignName", "Not specified");
                nullCampaignData.put("interestedCount", interestedNullCampaign);
                campaignDataList.add(nullCampaignData);
            }

            // Logic for appointments by campaign
            List<Map<String, Object>> appointmentsByCampaignRows = jdbcTemplate.queryForList(appointmentsByCampaignQuery, workspace, startDate, endDate);
            for (Map<String, Object> row : appointmentsByCampaignRows) {
                String campaignName = (String) row.get("campaign_name");
                Integer appointmentCount = ((Number) row.get("count")).intValue();
                Map<String, Object> appointmentData = new HashMap<>();
                appointmentData.put("campaignName", campaignName != null ? campaignName : "Not specified");
                appointmentData.put("appointmentCount", appointmentCount);
                appointmentsByCampaignList.add(appointmentData);
            }
            // Calculate percentages for appointments by campaign
            for (Map<String, Object> appointment : appointmentsByCampaignList) {
                int appointmentCount = (int) appointment.get("appointmentCount");
                double percentage = ((double) appointmentCount / allInterested) * 100;
                DecimalFormat df = new DecimalFormat("#.##");
                String formattedPercentage = df.format(percentage) + "%";
                appointment.put("percentage", formattedPercentage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Couldn't generate report", null, 500);
        }

        double leadsPercentage = ((double) totalInterested / allInterested * 100);
        double bookedPercentage = ((double) totalBookedMatched / totalBooked) * 100;
        double uniqueEmailsPercentage = ((double) uniqueEmailsBookedMatched / uniqueEmailGeneral) * 100;
        double callsPercentage = ((double) allCallsBooked / allCalls) * 100;

        Report reportGeneral = new Report();
        reportGeneral.setLeads(allInterested);
        reportGeneral.setBooked(totalBooked);
        reportGeneral.setUniqueEmails(uniqueEmailGeneral);
        reportGeneral.setMeets(allCalls);
        reportGeneral.setName("General");

        Report report = new Report();
        report.setLeads(totalInterested);
        report.setBooked(totalBookedMatched);
        report.setUniqueEmails(uniqueEmailsBookedMatched);
        report.setMeets(allCallsBooked);
        report.setName(workspaceName);

        Report reportPercentage = new Report();
        reportPercentage.setLeads(leadsPercentage);
        reportPercentage.setBooked(bookedPercentage);
        reportPercentage.setUniqueEmails(uniqueEmailsPercentage);
        reportPercentage.setMeets(callsPercentage);
        reportPercentage.setName("% of Total");

        List<Report> reports = List.of(reportGeneral, report, reportPercentage);

        ReportResponse reportResponse = new ReportResponse(reports, workspaceName, stageDataList, campaignDataList, appointmentsByCampaignList);
        reportResponse.calculateCampaignPercentages(totalInterested);
        return new ApiResponse<>("success", reportResponse, 200);
    }
}

