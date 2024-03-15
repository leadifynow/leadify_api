package com.api.leadify.dao;

import com.api.leadify.entity.Report;
import com.api.leadify.entity.ReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;

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
        String totalBookedQuery = "SELECT COUNT(*) FROM booked WHERE workspace_id = ? AND created_at BETWEEN ? AND ?";
        String uniqueEmailGeneralQuery = "SELECT COUNT(DISTINCT email) FROM booked WHERE workspace_id = ? AND created_at BETWEEN ? AND ?";
        String workspaceNameQuery = "SELECT name FROM workspace WHERE id = ?";
        String allCallsQuery = "SELECT COUNT(*) FROM booked WHERE workspace_id = ? AND meeting_date BETWEEN ? AND ?";
        String allCallsBookedQuery = "SELECT COUNT(*) FROM booked WHERE workspace_id = ? AND interested_id IS NOT NULL AND meeting_date BETWEEN ? AND ?";


        Integer totalInterested = null;
        Integer totalBookedMatched = null;
        Integer uniqueEmailsBookedMatched = null;
        Integer totalInterestedAndBookedNonMatched = null;
        Integer totalBooked = null;
        Integer uniqueEmailGeneral = null;
        String workpsaceName = null;
        Integer allCalls = null;
        Integer allCallsBooked = null;

        try {
            workpsaceName = jdbcTemplate.queryForObject(workspaceNameQuery, String.class, workspace);
            totalInterested = jdbcTemplate.queryForObject(totalInterestedQuery, Integer.class, workspace, dates[0], dates[1]);
            totalBookedMatched = jdbcTemplate.queryForObject(totalBookedMatchedQuery, Integer.class, workspace, dates[0], dates[1]);
            uniqueEmailsBookedMatched = jdbcTemplate.queryForObject(uniqueEmailBookedMatchQuery, Integer.class, workspace, dates[0], dates[1]);
            totalInterestedAndBookedNonMatched = jdbcTemplate.queryForObject(totalInterestedAndBookedNonMatchedQuery, Integer.class, workspace, dates[0], dates[1]);
            totalBooked = jdbcTemplate.queryForObject(totalBookedQuery, Integer.class, workspace, dates[0], dates[1]);
            uniqueEmailGeneral = jdbcTemplate.queryForObject(uniqueEmailGeneralQuery, Integer.class, workspace, dates[0], dates[1]);
            allCalls = jdbcTemplate.queryForObject(allCallsQuery, Integer.class, workspace, dates[0], dates[1]);
            allCallsBooked = jdbcTemplate.queryForObject(allCallsBookedQuery, Integer.class, workspace, dates[0], dates[1]);
        } catch (Exception e) {
            // Handle any exceptions here
            e.printStackTrace();
            return new ApiResponse<>("Couldn't generate report", null, 500);
        }

        double leadsPercentage = ((double) totalInterested / (totalInterested + totalInterestedAndBookedNonMatched)) * 100;
        double bookedPercentage = ((double) totalBookedMatched / totalBooked) * 100;
        double uniqueEmailsPercentage = ((double) uniqueEmailsBookedMatched / uniqueEmailGeneral) * 100;
        double callsPercentage = ((double) allCallsBooked / allCalls) * 100;

        Report reportGeneral = new Report();
        reportGeneral.setLeads(totalInterested + totalInterestedAndBookedNonMatched);
        reportGeneral.setBooked(totalBooked);
        reportGeneral.setUniqueEmails(uniqueEmailGeneral);
        reportGeneral.setMeets(allCalls);
        reportGeneral.setName("General");

        Report report = new Report();
        report.setLeads(totalInterested);
        report.setBooked(totalBookedMatched);
        report.setUniqueEmails(uniqueEmailsBookedMatched);
        report.setMeets(allCallsBooked);
        report.setName("Leadify");

        Report reportPercentage = new Report();
        reportPercentage.setLeads(leadsPercentage);
        reportPercentage.setBooked(bookedPercentage);
        reportPercentage.setUniqueEmails(uniqueEmailsPercentage);
        reportPercentage.setMeets(callsPercentage);
        reportPercentage.setName("% of Total");

        List<Report> reports = List.of(reportGeneral, report, reportPercentage);

        ReportResponse reportResponse = new ReportResponse(reports, workpsaceName);
        return new ApiResponse<>("success", reportResponse, 200);
    }
}
