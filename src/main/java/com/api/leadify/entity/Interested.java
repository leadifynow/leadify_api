package com.api.leadify.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class Interested {
    private int id;
    private String event_type;
    private UUID workspace;
    private UUID campaign_id;
    private String campaign_name;
    private String lead_email;
    @JsonProperty("Title")
    private String title;
    private String email;
    private String website;
    @JsonProperty("Industry")
    private String industry;
    private String lastName;
    private String firstName;
    @JsonProperty("# Employees")
    private String number_of_employees;
    private String companyName;
    @JsonProperty("Person Linkedin Url")
    private String linkedin_url;
    private String notes;
    private Boolean booked;
    private Integer stage_id;
    private Date created_at; // Changed type to Date
    private Integer manager;
    private Date next_update;
    private String workspaceName;
}
