package com.api.leadify.dao;

import com.api.leadify.entity.Report;
import com.api.leadify.entity.ReportResponse;
import com.api.leadify.entity.WorkspaceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class ReportsDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReportsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<ReportResponse> getReport(String workspace, String[] dates) {
        // Queries updated to include company_id by joining workspace
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String totalInterestedQuery = "SELECT COUNT(*) FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        String totalBookedMatchedQuery = "SELECT COUNT(*) FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.interested_id IS NOT NULL AND b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ?";
        String uniqueEmailBookedMatchQuery = "SELECT COUNT(DISTINCT b.email) FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.interested_id IS NOT NULL AND b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ?";
        String totalInterestedAndBookedNonMatchedQuery = "SELECT COUNT(*) FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.interested_id IS NULL AND b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ?";
        String totalBookedQuery = "SELECT COUNT(*) FROM booked WHERE created_at BETWEEN ? AND ? AND deleted = 0 AND company_id = ?";
        String uniqueEmailGeneralQuery = "SELECT COUNT(DISTINCT b.email) FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ?";
        String workspaceNameQuery = "SELECT name FROM workspace WHERE id = ?";
        String allCallsQuery = "SELECT COUNT(*) FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.meeting_date BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ?";
        String allCallsBookedQuery = "SELECT COUNT(*) FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.workspace_id = ? AND b.interested_id IS NOT NULL AND b.meeting_date BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ?";
        String allInterestedQuery = "SELECT COUNT(*) FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        String stagesQuery = "SELECT id, name FROM stage WHERE workspace_id = ? ORDER BY position_workspace";
        String campaignQuery = "SELECT i.campaign_name, COUNT(*) AS count FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NOT NULL AND i.campaign_name <> '' AND i.created_at BETWEEN ? AND ? AND w.company_id = ? GROUP BY i.campaign_name";
        String appointmentsByCampaignQuery = "SELECT i.campaign_name, COUNT(*) AS count FROM booked b INNER JOIN interested i ON b.interested_id = i.id JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND b.interested_id IS NOT NULL AND b.meeting_date BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ? GROUP BY i.campaign_name";
        String emailOccurrencesQuery = "SELECT b.email, COUNT(*) AS count FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ? GROUP BY b.email";


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
        List<Map<String, Object>> emailOccurrencesOutputList = new ArrayList<>();

        // Set time to 00:00:00.000 for both start and end dates
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);

        Integer companyId = null;
        try {
            // Fetch the company_id for the given workspace
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

            workspaceName = jdbcTemplate.queryForObject(workspaceNameQuery, String.class, workspace);
            totalInterested = jdbcTemplate.queryForObject(totalInterestedQuery, Integer.class, workspace, startDate, endDate, companyId);
            totalBookedMatched = jdbcTemplate.queryForObject(totalBookedMatchedQuery, Integer.class, workspace, startDate, endDate, companyId);
            uniqueEmailsBookedMatched = jdbcTemplate.queryForObject(uniqueEmailBookedMatchQuery, Integer.class, workspace, startDate, endDate, companyId);
            totalInterestedAndBookedNonMatched = jdbcTemplate.queryForObject(totalInterestedAndBookedNonMatchedQuery, Integer.class, workspace, startDate, endDate, companyId);
            totalBooked = jdbcTemplate.queryForObject(totalBookedQuery, Integer.class, startDate, endDate, companyId);
            uniqueEmailGeneral = jdbcTemplate.queryForObject(uniqueEmailGeneralQuery, Integer.class, startDate, endDate, companyId);
            allCalls = jdbcTemplate.queryForObject(allCallsQuery, Integer.class, startDate, endDate, companyId);
            allCallsBooked = jdbcTemplate.queryForObject(allCallsBookedQuery, Integer.class, workspace, startDate, endDate, companyId);
            allInterested = jdbcTemplate.queryForObject(allInterestedQuery, Integer.class, startDate, endDate, companyId);

            List<Map<String, Object>> emailOccurrencesRows = jdbcTemplate.queryForList(emailOccurrencesQuery, workspace, startDate, endDate, companyId);

            // Iterate through the email occurrences and calculate percentages
            Map<Integer, Integer> emailOccurrencesMap = new HashMap<>();
            for (Map<String, Object> emailOccurrence : emailOccurrencesRows) {
                int occurrenceCount = ((Number) emailOccurrence.get("count")).intValue();

                // Update the count for the occurrence in the map
                emailOccurrencesMap.put(occurrenceCount, emailOccurrencesMap.getOrDefault(occurrenceCount, 0) + 1);
            }

            // Clear the existing list
            emailOccurrencesOutputList.clear();

            // Iterate through the map and add email occurrences to the list
            for (Map.Entry<Integer, Integer> entry : emailOccurrencesMap.entrySet()) {
                int occurrenceCount = entry.getKey();
                int emailCount = entry.getValue();

                // Create a new map entry for the email occurrence
                Map<String, Object> emailOccurrenceData = new HashMap<>();
                emailOccurrenceData.put("count", emailCount);
                emailOccurrenceData.put("text", "Booked " + occurrenceCount + " time" + (occurrenceCount > 1 ? "s" : ""));

                // Calculate percentage based on the total count of occurrences
                double totalOccurrences = emailOccurrencesMap.values().stream().mapToInt(Integer::intValue).sum();
                double percentage = ((double) emailCount / totalOccurrences) * 100;
                DecimalFormat df = new DecimalFormat("#.##");
                String formattedPercentage = df.format(percentage) + "%";
                emailOccurrenceData.put("percentage", formattedPercentage);

                // Add the email occurrence data to the list
                emailOccurrencesOutputList.add(emailOccurrenceData);
            }

            // Fetch stage data
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(stagesQuery, workspace);
            for (Map<String, Object> row : rows) {
                Integer stageId = (Integer) row.get("id");
                String stageName = (String) row.get("name");
                // Count interested per stage
                Integer interestedPerStage = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.stage_id = ? AND i.created_at BETWEEN ? AND ? AND w.company_id = ?", Integer.class, workspace, stageId, startDate, endDate, companyId);
                Map<String, Object> stageData = new HashMap<>();
                stageData.put("stageName", stageName);
                stageData.put("interestedCount", interestedPerStage);
                stageDataList.add(stageData);
            }
            Integer interestedNullStage = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.stage_id IS NULL AND i.created_at BETWEEN ? AND ? AND w.company_id = ?", Integer.class, workspace, startDate, endDate, companyId);
            if (interestedNullStage != null && interestedNullStage > 0) {
                Map<String, Object> nullStageData = new HashMap<>();
                nullStageData.put("stageName", "Not in stage");
                nullStageData.put("interestedCount", interestedNullStage);
                stageDataList.add(nullStageData);
            }

            List<Map<String, Object>> campaignRows = jdbcTemplate.queryForList(campaignQuery, workspace, startDate, endDate, companyId);
            for (Map<String, Object> row : campaignRows) {
                String campaignName = (String) row.get("campaign_name");
                Integer interestedCount = ((Number) row.get("count")).intValue();
                Map<String, Object> campaignData = new HashMap<>();
                campaignData.put("campaignName", campaignName != null ? campaignName : "Not specified");
                campaignData.put("interestedCount", interestedCount);
                campaignDataList.add(campaignData);
            }

            // Fetch count of interested where campaign_name is null
            Integer interestedNullCampaign = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NULL OR i.campaign_name = ''  AND i.created_at BETWEEN ? AND ? AND w.company_id = ?", Integer.class, workspace, startDate, endDate, companyId);
            if (interestedNullCampaign != null && interestedNullCampaign > 0) {
                Map<String, Object> nullCampaignData = new HashMap<>();
                nullCampaignData.put("campaignName", "Not specified");
                nullCampaignData.put("interestedCount", interestedNullCampaign);
                campaignDataList.add(nullCampaignData);
            }

            // Logic for appointments by campaign
            List<Map<String, Object>> appointmentsByCampaignRows = jdbcTemplate.queryForList(appointmentsByCampaignQuery, workspace, startDate, endDate, companyId);
            for (Map<String, Object> row : appointmentsByCampaignRows) {
                String campaignName = (String) row.get("campaign_name");
                Integer appointmentCount = ((Number) row.get("count")).intValue();
                Map<String, Object> appointmentData = new HashMap<>();
                appointmentData.put("campaignName", campaignName != null ? campaignName : "Not specified");
                appointmentData.put("appointmentCount", appointmentCount);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Couldn't generate report", null, 500);
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

        ReportResponse reportResponse = new ReportResponse(reports, workspaceName, stageDataList, campaignDataList, appointmentsByCampaignList, emailOccurrencesOutputList);
        reportResponse.calculateCampaignPercentages(totalInterested);
        return ResponseEntity.ok(reportResponse);
        //return new ApiResponse<>("success", reportResponse, 200);
    }

    public ResponseEntity<List<Map<String, Object>>> getAppointmentsByCampaignRows (String workspace, String[] dates)
    { 
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String appointmentsByCampaignsql="SELECT distinct b.id as booked_id, b.name, b.email, i.campaign_name, i.campaign_id, b.created_at, b.meeting_date FROM booked b INNER JOIN interested i ON b.interested_id = i.id JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ?  AND b.interested_id IS NOT NULL AND b.meeting_date BETWEEN ? AND ? AND b.deleted =0 AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        List<Map<String, Object>> appointmentsByCampaignRows = jdbcTemplate.queryForList(appointmentsByCampaignsql, workspace, startDate, endDate, companyId);

        return ResponseEntity.ok(appointmentsByCampaignRows);
}

    public ResponseEntity<List<Map<String, Object>>> getCampaignData (String workspace, String[] dates)
    { 
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String campaignQuery = "SELECT distinct i.id as interested_id, CONCAT(i.firstName , ' ', i.lastName) as name ,i.email, i.campaign_name, i.campaign_id , i.created_at  FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NOT NULL AND i.campaign_name <> '' AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        String nullCampaignsQuery="SELECT distinct i.id as interested_id, CONCAT(i.firstName , ' ', i.lastName) as name ,i.email , i.created_at FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NULL OR i.campaign_name = '' AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

            List<Map<String, Object>> campaignRows = jdbcTemplate.queryForList(campaignQuery, workspace, startDate, endDate, companyId);

            List<Map<String, Object>> interestedNullCampaign = jdbcTemplate.queryForList(nullCampaignsQuery,workspace, startDate, endDate, companyId);

            if (interestedNullCampaign != null) {
                for (Map<String, Object> row : interestedNullCampaign ) {
                        if (!row.containsKey("campaign_name")) {
                            row.put("campaign_id", null); 
                            row.put("campaign_name", "Not specified"); 
                        }
                }
            }

        campaignRows.addAll(interestedNullCampaign);

        return ResponseEntity.ok(campaignRows);
}

    public ResponseEntity<List<Map<String, Object>>> getEmailOccurrences (String workspace, String[] dates)
{ 
    String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
    String emailOccurrencesQuery = "SELECT b.id as booked_id, b.name, b.email,i.campaign_id ,i.campaign_name ,b.created_at, COUNT(*) AS times_booked FROM booked b INNER JOIN interested i ON b.interested_id = i.id JOIN workspace w ON b.workspace_id = w.id WHERE b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ? GROUP BY b.email";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
    LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
    List<Map<String, Object>> emailOccurrencesOutputList = new ArrayList<>();
    List<String> emails = new ArrayList<>();
    startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
    endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
    Integer companyId = null;
        companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
        if (companyId == null) {
            throw new RuntimeException("Company ID not found for workspace: " + workspace);
        }

         List<Map<String, Object>> emailOccurrencesRows = jdbcTemplate.queryForList(emailOccurrencesQuery, workspace, startDate, endDate, companyId);
         return ResponseEntity.ok( emailOccurrencesRows);
}

    public ResponseEntity<List<Map<String, Object>>> getstageData(String workspace, String[] dates)
{ 
    String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
    String stagesQuery = "SELECT id, name FROM stage WHERE workspace_id = ? ORDER BY position_workspace";
    String nullStageQuery= "SELECT distinct i.id as interested_id, CONCAT(i.firstName , ' ', i.lastName) as name ,i.email, i.campaign_name, i.campaign_id , i.created_at FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.stage_id IS NULL AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
    String getInterestedStage="SELECT distinct i.id as interested_id, CONCAT(i.firstName , ' ', i.lastName) as name ,i.email, i.campaign_name, i.campaign_id , i.created_at, i.stage_id FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.stage_id = ? AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
    LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
    List<Map<String, Object>> stageDataList = new ArrayList<>();
    List<Map<String, Object>> interestedPerStage= new ArrayList<>();
    startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
    endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
    Integer companyId = null;
        companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
        if (companyId == null) {
            throw new RuntimeException("Company ID not found for workspace: " + workspace);
        }


        List<Map<String, Object>> rows = jdbcTemplate.queryForList(stagesQuery, workspace);
        for (Map<String, Object> row : rows) {
                Integer stageId = (Integer) row.get("id");
                String stageName = (String) row.get("name");
                interestedPerStage = jdbcTemplate.queryForList(getInterestedStage,workspace, stageId, startDate, endDate, companyId);
                for (Map<String, Object> name : interestedPerStage) {
                    if (!name.containsKey("stage_name")) {
                    name.put("stage_name", stageName); 
                    }
                    stageDataList.add(name);
                }
        }

        List<Map<String, Object>> interestedNullStage = jdbcTemplate.queryForList(nullStageQuery,workspace, startDate, endDate, companyId);
        if (interestedNullStage != null) {
            for (Map<String, Object> name : interestedNullStage) {
                if (!name.containsKey("stage_name")) {
                    name.put("stage_id", null); 
                    name.put("stage_name", "Not in stage"); 
                }
                
            }          
        }

    stageDataList.addAll(interestedNullStage);
    return ResponseEntity.ok(stageDataList);
}



    public ResponseEntity<List<Map<String, Object>>> getLeadsGeneral (String workspace, String[] dates){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";

        String allInterestedQueryNames = "SELECT i.id as interested_id, CONCAT(i.firstName,' ',i.lastName) as name,i.email, i.created_at FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        List<Map<String, Object>> LeadsGeneral = jdbcTemplate.queryForList(allInterestedQueryNames,startDate, endDate, companyId);

        return ResponseEntity.ok(LeadsGeneral);
    }

    public ResponseEntity<List<Map<String, Object>>> getBookedGeneral (String workspace, String[] dates){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String totalBookedQueryNames = "SELECT id as booked_id, name, email, created_at FROM booked WHERE created_at BETWEEN ? AND ?  AND deleted = 0 AND company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        List<Map<String, Object>> totalBookedGeneral = jdbcTemplate.queryForList(totalBookedQueryNames,startDate, endDate, companyId);

        return ResponseEntity.ok(totalBookedGeneral);
    }

    public ResponseEntity<List<Map<String, Object>>> getUniqueEmailsGeneral (String workspace, String[] dates){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String uniqueEmailGeneralQueryNames = 
        "WITH UniqueNamesEmails AS (\n" +
        "    SELECT MIN(b.id) AS min_id, b.name, b.email \n" +
        "    FROM booked b \n" +
        "    JOIN workspace w ON b.workspace_id = w.id \n" +
        "    WHERE b.created_at BETWEEN ? AND ? \n" +
        "      AND b.deleted = 0 \n" +
        "      AND w.company_id = ? \n" +
        "    GROUP BY b.name, b.email \n" +
        ") \n" +
        "SELECT b.id AS booked_id, b.name, b.email, b.created_at \n" +
        "FROM booked b \n" +
        "JOIN UniqueNamesEmails une ON b.id = une.min_id \n" +
        "JOIN workspace w ON b.workspace_id = w.id \n" +
        "WHERE b.created_at BETWEEN ? AND ? \n" +
        "  AND b.deleted = 0 \n" +
        "  AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

            Object[] params = new Object[] {
                startDate,  
                endDate,    
                companyId,  
                startDate,  
                endDate,    
                companyId  
            };

            List<Map<String, Object>> uniqueEmailGeneral = jdbcTemplate.queryForList(uniqueEmailGeneralQueryNames, params);

        return ResponseEntity.ok(uniqueEmailGeneral );
    }

    public ResponseEntity<List<Map<String, Object>>> getMeetsGeneral (String workspace, String[] dates){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String allCallsQueryNames = "SELECT b.id as booked_id, b.name, b.email, b.created_at FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.meeting_date BETWEEN ? AND ?  AND b.deleted = 0 AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        List<Map<String, Object>> MeetsGeneral = jdbcTemplate.queryForList(allCallsQueryNames,startDate, endDate, companyId);
        return ResponseEntity.ok(MeetsGeneral);
    }

    public ResponseEntity<List<Map<String, Object>>> getLeadsWorkspace (String workspace, String[] dates){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String totalInterestedQueryNames = "SELECT i.id as interested_id, CONCAT(i.firstName,' ',i.lastName) as name,i.email, i.created_at FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }
        List<Map<String, Object>> LeadsWorkspace = jdbcTemplate.queryForList(totalInterestedQueryNames, workspace, startDate, endDate, companyId);
        return ResponseEntity.ok(LeadsWorkspace);
    }

    public ResponseEntity<List<Map<String, Object>>> getBookedWorkspace (String workspace, String[] dates){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String totalBookedMatchedQueryNames = "SELECT b.id as booked_id, b.name, b.email, b.created_at FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.interested_id IS NOT NULL AND b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        List<Map<String, Object>> BookedWorkspace = jdbcTemplate.queryForList(totalBookedMatchedQueryNames, workspace, startDate, endDate, companyId);
        return ResponseEntity.ok(BookedWorkspace);
    }

    public ResponseEntity<List<Map<String, Object>>> getUniqueEmailsWorkspace (String workspace, String[] dates){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String uniqueEmailBookedMatchQueryNames = "SELECT DISTINCT b.id as booked_id, b.name, b.email, b.created_at FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.interested_id IS NOT NULL AND b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        List<Map<String, Object>> UniqueEmailsWorkspace = jdbcTemplate.queryForList(uniqueEmailBookedMatchQueryNames,workspace, startDate, endDate, companyId);
        return ResponseEntity.ok(UniqueEmailsWorkspace);
    }

    public ResponseEntity<List<Map<String, Object>>> getMeetsWorkspace (String workspace, String[] dates){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String allCallsBookedQueryNames = "SELECT b.id as booked_id, b.name, b.email, b.created_at FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.workspace_id = ? AND b.interested_id IS NOT NULL AND b.meeting_date BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }
        List<Map<String, Object>> MeetsWorkspace = jdbcTemplate.queryForList(allCallsBookedQueryNames,workspace, startDate, endDate, companyId);
        return ResponseEntity.ok(MeetsWorkspace);
    }



    public List<String> getAppoint(String workspace, String[] dates) {
        String IdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String sql="SELECT distinct i.campaign_name FROM booked b INNER JOIN interested i ON b.interested_id = i.id JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ?  AND b.interested_id IS NOT NULL AND b.meeting_date BETWEEN ? AND ? AND b.deleted =0 AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(IdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        List<Map<String, Object>> row = jdbcTemplate.queryForList(sql, workspace, startDate, endDate, companyId);

        List<String> campaigns = new ArrayList<>();
            for (Map<String, Object> map : row) {
                String camp = (String) map.get("campaign_name");
                campaigns.add(camp);
            }
        return campaigns;  
    }
    
    public List<String> getCampign(String workspace, String[] dates) {
        String IdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String sql="SELECT distinct i.campaign_name FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NOT NULL AND i.campaign_name <> '' AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        String sqlnull="SELECT distinct COUNT(*) FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NULL OR i.campaign_name = '' AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(IdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        
        List<Map<String, Object>> row = jdbcTemplate.queryForList(sql, workspace, startDate, endDate, companyId);
        Integer nullrow = jdbcTemplate.queryForObject(sqlnull,Integer.class,workspace, startDate, endDate, companyId);
        if (nullrow>0) {
            Map<String, Object> newCamp = new HashMap<>();
            newCamp.put("campaign_name", "Not specified");
            row.add(newCamp);
        }

        List<String> campaigns = new ArrayList<>();
            for (Map<String, Object> map : row) {
                String camp = (String) map.get("campaign_name");
                campaigns.add(camp);
            }
        return campaigns;
    }

    public List<String> getTimes(String workspace, String[] dates) {
        String IdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String sql="SELECT distinct COUNT(*) AS times_booked FROM booked b INNER JOIN interested i ON b.interested_id = i.id JOIN workspace w ON b.workspace_id = w.id WHERE b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ? GROUP BY b.email";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(IdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        List<Map<String, Object>> row = jdbcTemplate.queryForList(sql, workspace, startDate, endDate, companyId);

        List<String> timesBooked = new ArrayList<>();
        for (Map<String, Object> map : row) {
            Long times = (Long) map.get("times_booked");
            String timesString=times.toString();
            timesBooked.add(timesString);
        }
        return timesBooked;  
    }

    public List<String> getStage(String workspace, String[] dates) {
        String IdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String sql="SELECT distinct s.name as stage_name FROM interested i INNER JOIN stage s ON i.stage_id=s.id JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.stage_id IS NOT NULL AND i.created_at BETWEEN ? AND ? AND w.company_id = ? order by i.stage_id";
        String sqlnull="SELECT distinct COUNT(*) FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.stage_id IS NULL AND i.created_at BETWEEN ? AND ? AND w.company_id = ? ";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(IdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

        List<Map<String, Object>> row = jdbcTemplate.queryForList(sql, workspace, startDate, endDate, companyId);
        Integer nullrow = jdbcTemplate.queryForObject(sqlnull,Integer.class,workspace, startDate, endDate, companyId);
        if (nullrow > 0) {
            Map<String, Object> newCamp = new HashMap<>();
            newCamp.put("stage_name", "Not in stage"); 
            row.add(newCamp);
        }

        List<String> stages = new ArrayList<>();
        for (Map<String, Object> map : row) {
            String stage = (String) map.get("stage_name");
            stages.add(stage);
        }
    return stages; 
    }


    public ResponseEntity<List<Map<String,Object>>> getFilterAppointments( String workspace, String[] dates, String CampName){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String getCampignId="SELECT id FROM campaign WHERE campaign_name = ?";
        String appointmentsByCampaignsql="SELECT distinct b.id as booked_id, b.name, b.email, i.campaign_name, i.campaign_id, b.created_at, b.meeting_date FROM booked b INNER JOIN interested i ON b.interested_id = i.id JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ?  AND b.interested_id IS NOT NULL AND b.meeting_date BETWEEN ? AND ? AND b.deleted =0 AND w.company_id = ? AND i.campaign_id= ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }
        String campId = null;
            campId = jdbcTemplate.queryForObject(getCampignId, String.class, CampName);
            if (campId == null) {
                throw new RuntimeException("Campaign ID not found for campaign name: " + CampName);
            }

        List<Map<String, Object>> appointmentsByCampaignRows = jdbcTemplate.queryForList(appointmentsByCampaignsql, workspace, startDate, endDate, companyId,campId);

        return ResponseEntity.ok(appointmentsByCampaignRows);
    }

    public ResponseEntity<List<Map<String,Object>>> getFilterCampaign( String workspace, String[] dates, String CampName){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String getCampignId="SELECT id FROM campaign WHERE campaign_name = ?";
        String campaignQuery = "SELECT distinct i.id as interested_id, CONCAT(i.firstName , ' ', i.lastName) as name ,i.email, i.campaign_name, i.campaign_id , i.created_at  FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NOT NULL AND i.campaign_name <> '' AND i.created_at BETWEEN ? AND ? AND w.company_id = ? AND i.campaign_id=?";
        String nullCampaignsQuery="SELECT distinct i.id as interested_id, CONCAT(i.firstName , ' ', i.lastName) as name ,i.email , i.created_at FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NULL OR i.campaign_name = '' AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }
        
        List<Map<String, Object>> CampaignRows =new ArrayList<>();
        if(!CampName.equals("Not specified")){
            String campId = null;
            campId = jdbcTemplate.queryForObject(getCampignId, String.class, CampName);
            if (campId == null) {
                throw new RuntimeException("Campaign ID not found for campaign name: " + CampName);
            }

            CampaignRows = jdbcTemplate.queryForList(campaignQuery, workspace, startDate, endDate, companyId,campId);
        }
        else{
            CampaignRows = jdbcTemplate.queryForList(nullCampaignsQuery,workspace, startDate, endDate, companyId);
            if (CampaignRows != null) {
                for (Map<String, Object> row : CampaignRows) {
                        if (!row.containsKey("campaign_name")) {
                            row.put("campaign_id", null); 
                            row.put("campaign_name", "Not specified"); 
                        }
                }
            }
        }

        return ResponseEntity.ok(CampaignRows);
    }

    public ResponseEntity<List<Map<String,Object>>> getFilterEmail( String workspace, String[] dates, Integer Num){
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String bookedTimesSql="SELECT b.id as booked_id, b.name, b.email,i.campaign_id ,i.campaign_name ,b.created_at, COUNT(*) AS times_booked FROM booked b INNER JOIN interested i ON b.interested_id = i.id JOIN workspace w ON b.workspace_id = w.id WHERE b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ? GROUP BY b.email HAVING COUNT(*) = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }
        List<Map<String, Object>> TimesBookedRows = jdbcTemplate.queryForList(bookedTimesSql, workspace, startDate, endDate, companyId, Num);
        return ResponseEntity.ok(TimesBookedRows);
    }

   public ResponseEntity<List<Map<String,Object>>> getFilterStage( String workspace, String[] dates, String Stage){
    String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
    String sqlnull= "SELECT distinct i.id as interested_id, CONCAT(i.firstName , ' ', i.lastName) as name ,i.email, i.campaign_name, i.campaign_id , i.created_at FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.stage_id IS NULL AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
    String sql="SELECT distinct i.id as interested_id, CONCAT(i.firstName , ' ', i.lastName) as name ,i.email, i.campaign_name, i.campaign_id , i.created_at, i.stage_id, s.name as stage_name FROM interested i INNER JOIN stage s ON i.stage_id=s.id JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND s.name=? AND i.created_at BETWEEN ? AND ? AND w.company_id =? ;";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
    LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
    startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
    endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
    Integer companyId = null;
        companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
        if (companyId == null) {
            throw new RuntimeException("Company ID not found for workspace: " + workspace);
        }

    List<Map<String, Object>> StageRows =new ArrayList<>();
    if(!Stage.equals("Not in stage")){
        StageRows = jdbcTemplate.queryForList(sql,workspace, Stage, startDate, endDate, companyId);
    }
    else{
        StageRows = jdbcTemplate.queryForList(sqlnull, workspace, startDate, endDate, companyId);
        if (StageRows != null) {
            for (Map<String, Object> name : StageRows) {
                if (!name.containsKey("stage_name")) {
                    name.put("stage_id", null); 
                    name.put("stage_name", "Not in stage"); 
                } 
            }          
        }
    }

    return ResponseEntity.ok(StageRows);
    }
     
}

