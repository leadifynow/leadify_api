package com.api.leadify.entity;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class ReportResponse {
    private List<Report> reports;
    private String workspace;
    private List<Map<String, Object>> stageData;
    private List<Map<String, Object>> campaignData;
    private List<Map<String, Object>> appointmentsByCampaignData;
    private List<Map<String, Object>> emailOccurrences;

    public ReportResponse(List<Report> reports, String workspace, List<Map<String, Object>> stageData, List<Map<String, Object>> campaignData, List<Map<String, Object>> appointmentsByCampaignData, List<Map<String, Object>> emailOccurrences) {
        this.reports = reports;
        this.workspace = workspace;
        this.stageData = stageData;
        this.campaignData = campaignData;
        this.appointmentsByCampaignData = appointmentsByCampaignData;
        this.emailOccurrences = emailOccurrences;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public List<Map<String, Object>> getStageData() {
        return stageData;
    }

    public void setStageData(List<Map<String, Object>> stageData) {
        this.stageData = stageData;
    }

    public List<Map<String, Object>> getCampaignData() {
        return campaignData;
    }

    public void setCampaignData(List<Map<String, Object>> campaignData) {
        this.campaignData = campaignData;
    }

    public List<Map<String, Object>> getAppointmentsByCampaignData() {
        return appointmentsByCampaignData;
    }

    public void setAppointmentsByCampaignData(List<Map<String, Object>> appointmentsByCampaignData) {
        this.appointmentsByCampaignData = appointmentsByCampaignData;
    }

    public List<Map<String, Object>> getEmailOccurrences() {
        return emailOccurrences;
    }

    public void setEmailOccurrences(List<Map<String, Object>> emailOccurrences) {
        this.emailOccurrences = emailOccurrences;
    }

    // Method to calculate the percentage for each campaign data
    public void calculateCampaignPercentages(int totalInterested) {
        DecimalFormat df = new DecimalFormat("#.##");
        for (Map<String, Object> campaign : campaignData) {
            int interestedCount = (int) campaign.get("interestedCount");
            double percentage = ((double) interestedCount / totalInterested) * 100;
            String formattedPercentage = df.format(percentage) + "%";
            campaign.put("percentage", formattedPercentage);
        }
    }
}
