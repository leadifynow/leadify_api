package com.api.leadify.entity;

import java.util.List;

public class ReportResponse {
    private List<Report> reports;
    private String workspace;

    public ReportResponse(List<Report> reports, String workspace) {
        this.reports = reports;
        this.workspace = workspace;
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
}
