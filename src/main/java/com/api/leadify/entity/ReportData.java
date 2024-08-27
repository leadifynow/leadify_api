package com.api.leadify.entity;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportData {
    private List<Map<String, Object>> leads_general;
    private List<Map<String, Object>> booked_general;
    private List<Map<String, Object>>uniqueEmails_general;
    private List<Map<String, Object>>meets_general;

    private List<Map<String, Object>> leads_workspace;
    private List<Map<String, Object>> booked_workspace;
    private List<Map<String, Object>>uniqueEmails_workspace;
    private List<Map<String, Object>>meets_workspace;

}
