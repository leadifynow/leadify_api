package com.api.leadify.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.UUID;

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

    public Boolean getBooked() {
        return booked;
    }

    public void setBooked(Boolean booked) {
        this.booked = booked;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    private Integer stage_id;
    private Timestamp created_at;
    private Integer manager;
    private Timestamp next_update;
    private String workspaceName;

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public Timestamp getNext_update() {
        return next_update;
    }

    public void setNext_update(Timestamp next_update) {
        this.next_update = next_update;
    }

    public Integer getManager() {
        return manager;
    }

    public void setManager(Integer manager) {
        this.manager = manager;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public Integer getStage_id() {
        return stage_id;
    }

    public void setStage_id(Integer stage_id) {
        this.stage_id = stage_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public UUID getWorkspace() {
        return workspace;
    }

    public void setWorkspace(UUID workspace) {
        this.workspace = workspace;
    }

    public UUID getCampaign_id() {
        return campaign_id;
    }

    public void setCampaign_id(UUID campaign_id) {
        this.campaign_id = campaign_id;
    }

    public String getCampaign_name() {
        return campaign_name;
    }

    public void setCampaign_name(String campaign_name) {
        this.campaign_name = campaign_name;
    }

    public String getLead_email() {
        return lead_email;
    }

    public void setLead_email(String lead_email) {
        this.lead_email = lead_email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getNumber_of_employees() {
        return number_of_employees;
    }

    public void setNumber_of_employees(String number_of_employees) {
        this.number_of_employees = number_of_employees;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLinkedin_url() {
        return linkedin_url;
    }

    public void setLinkedin_url(String linkedin_url) {
        this.linkedin_url = linkedin_url;
    }
}
