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
        String campaignQuery = "SELECT i.campaign_name, COUNT(*) AS count FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.created_at BETWEEN ? AND ? AND w.company_id = ? GROUP BY i.campaign_name";
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
            Integer interestedNullCampaign = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NULL AND i.created_at BETWEEN ? AND ? AND w.company_id = ?", Integer.class, workspace, startDate, endDate, companyId);
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
        String appointmentsByCampaignsql="SELECT distinct i.campaign_name, b.name FROM booked b INNER JOIN interested i ON b.interested_id = i.id JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ?  AND b.interested_id IS NOT NULL AND b.meeting_date BETWEEN ? AND ? AND b.deleted =0 AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        List<Map<String, Object>> appointmentsByCampaignList = new ArrayList<>();
        Map<String, List<String>> campaignToNamesMap = new HashMap<>();
        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

            List<Map<String, Object>> appointmentsByCampaignRows = jdbcTemplate.queryForList(appointmentsByCampaignsql, workspace, startDate, endDate, companyId);

            for (Map<String, Object> row : appointmentsByCampaignRows) {
                String campaignName = (String) row.get("campaign_name");
                String bookedName = (String) row.get("name");
                if (campaignName != null && bookedName != null) {
                    List<String> namesList = campaignToNamesMap.getOrDefault(campaignName, new ArrayList<>());
                    namesList.add(bookedName);
                    campaignToNamesMap.put(campaignName, namesList);
                }
            }

     for (Map.Entry<String, List<String>> entry : campaignToNamesMap.entrySet()) {
        Map<String, Object> appointmentData = new HashMap<>();
        appointmentData.put("names", entry.getValue());
        appointmentData.put("campaignName", entry.getKey());
        appointmentsByCampaignList.add(appointmentData); 
     }
        return ResponseEntity.ok(appointmentsByCampaignList);
}

    public ResponseEntity<List<Map<String, Object>>> getCampaignData (String workspace, String[] dates)
    { 
        String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
        String campaignQuery = "SELECT distinct i.firstName,i.lastName, i.campaign_name FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        String nullCampaignsQuery="SELECT distinct i.firstName,i.lastName FROM interested i JOIN workspace w ON i.workspace = w.id WHERE i.workspace = ? AND i.campaign_name IS NULL AND i.created_at BETWEEN ? AND ? AND w.company_id = ?";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime startDate = LocalDateTime.parse(dates[0], formatter);
        LocalDateTime endDate = LocalDateTime.parse(dates[1], formatter);
        List<Map<String, Object>> campaignDataList = new ArrayList<>();

        Map<String, List<String>> campaignToNamesMap = new HashMap<>();

        startDate = startDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endDate = endDate.withHour(23).withMinute(59).withSecond(59).withNano(59);
        Integer companyId = null;
            companyId = jdbcTemplate.queryForObject(getCompanyIdQuery, Integer.class, workspace);
            if (companyId == null) {
                throw new RuntimeException("Company ID not found for workspace: " + workspace);
            }

            List<Map<String, Object>> campaignRows = jdbcTemplate.queryForList(campaignQuery, workspace, startDate, endDate, companyId);
            for (Map<String, Object> row : campaignRows) {
                String campaignName = (String) row.get("campaign_name");
                String interestedfirstName = (String) row.get("firstName");
                String interestedlastName = (String) row.get("lastName");
                if (campaignName != null && (interestedfirstName != null || interestedlastName != null)) {
                    List<String> namesList = campaignToNamesMap.getOrDefault(campaignName, new ArrayList<>());
                    String fullname=interestedfirstName+" "+interestedlastName;
                    namesList.add(fullname);
                    campaignToNamesMap.put(campaignName, namesList);
                }
            }

            // Fetch count of interested where campaign_name is null
            List<Map<String, Object>> interestedNullCampaign = jdbcTemplate.queryForList(nullCampaignsQuery,workspace, startDate, endDate, companyId);
            if (interestedNullCampaign != null) {
                for (Map<String, Object> row : interestedNullCampaign ) {
                    String interestedfirstName = (String) row.get("firstName");
                    String interestedlastName = (String) row.get("lastName");
                    if (interestedfirstName != null || interestedlastName != null) {
                        List<String> namesList = campaignToNamesMap.getOrDefault("Not specified", new ArrayList<>());
                        String fullname=interestedfirstName+" "+interestedlastName;
                        namesList.add(fullname);
                        campaignToNamesMap.put("Not specified", namesList);
                    }
                }
            }
        
     for (Map.Entry<String, List<String>> entry : campaignToNamesMap.entrySet()) {
        Map<String, Object> campaignData = new HashMap<>();
        campaignData.put("Interested Names", entry.getValue());
        campaignData.put("campaignName", entry.getKey());
        campaignDataList.add(campaignData); 
     }
        return ResponseEntity.ok(campaignDataList);
}

    public ResponseEntity<List<Map<String, Object>>> getEmailOccurrences (String workspace, String[] dates)
{ 
    String getCompanyIdQuery = "SELECT company_id FROM workspace WHERE id = ?";
    String emailOccurrencesQuery = "SELECT b.email, COUNT(*) AS count FROM booked b JOIN workspace w ON b.workspace_id = w.id WHERE b.workspace_id = ? AND b.created_at BETWEEN ? AND ? AND b.deleted = 0 AND w.company_id = ? GROUP BY b.email";
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

            // Iterate through the email occurrences and calculate percentages
            Map<Integer, Integer> emailOccurrencesMap = new HashMap<>();
            for (Map<String, Object> emailOccurrence : emailOccurrencesRows) {
                int occurrenceCount = ((Number) emailOccurrence.get("count")).intValue();
                String email = (String) emailOccurrence.get("email");
                emails.add(email);
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

                emailOccurrenceData.put("emails", emails);
                // Add the email occurrence data to the list
                emailOccurrencesOutputList.add(emailOccurrenceData);
            }

    return ResponseEntity.ok(emailOccurrencesOutputList);
}

}

