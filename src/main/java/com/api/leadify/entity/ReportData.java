package com.api.leadify.entity;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportData {
    public generalReport general;
    public reportUnique report;
    public percentageReport percentage;
    public infoNames names;

    @Getter
    @Setter
    public static class percentageReport {
        private double leads;
        private double booked;
        private double uniqueEmails;
        private String name;
        private double meets;
    }

    @Getter
    @Setter
    public static class infoNames{
        private List<Map<String, Object>> leads;
        private List<Map<String, Object>> booked;
        private List<Map<String, Object>>uniqueEmails;
        private List<Map<String, Object>>meets;
    }

    @Getter
    @Setter
    public static class generalReport{
        public percentageReport info;
        public infoNames names;
    }

    @Getter
    @Setter
    public static class reportUnique{
        public percentageReport info;
        public infoNames names;
    }
}
