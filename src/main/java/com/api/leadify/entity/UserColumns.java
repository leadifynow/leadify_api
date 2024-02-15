package com.api.leadify.entity;

import org.springframework.beans.factory.annotation.Autowired;

public class UserColumns {
    private Integer id;
    private Boolean first_name;
    private Boolean last_name;
    private Boolean number_of_employees;
    private Boolean linkedin;
    private Boolean title;
    private Boolean campaign_name;
    private Boolean company_name;
    private Boolean interest_date;
    private Boolean notes;
    private Boolean website;
    private Integer user_id;
    private String workspace_id;
    private Boolean industry;
    private Boolean manager;
    private Boolean next_update;

    public String getWorkspace_id() {
        return workspace_id;
    }

    public void setWorkspace_id(String workspace_id) {
        this.workspace_id = workspace_id;
    }

    public Boolean getNext_update() {
        return next_update;
    }

    public void setNext_update(Boolean next_update) {
        this.next_update = next_update;
    }

    public Boolean getManager() {
        return manager;
    }

    public void setManager(Boolean manager) {
        this.manager = manager;
    }

    public Boolean getIndustry() {
        return industry;
    }

    public void setIndustry(Boolean industry) {
        this.industry = industry;
    }

    public UserColumns() {
        // Empty constructor
    }
    public UserColumns(Integer user_id, String workspaceId) {
        this.user_id = user_id;
        this.workspace_id = workspaceId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getFirst_name() {
        return first_name;
    }

    public void setFirst_name(Boolean first_name) {
        this.first_name = first_name;
    }

    public Boolean getLast_name() {
        return last_name;
    }

    public void setLast_name(Boolean last_name) {
        this.last_name = last_name;
    }

    public Boolean getNumber_of_employees() {
        return number_of_employees;
    }

    public void setNumber_of_employees(Boolean number_of_employees) {
        this.number_of_employees = number_of_employees;
    }

    public Boolean getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(Boolean linkedin) {
        this.linkedin = linkedin;
    }

    public Boolean getTitle() {
        return title;
    }

    public void setTitle(Boolean title) {
        this.title = title;
    }

    public Boolean getCampaign_name() {
        return campaign_name;
    }

    public void setCampaign_name(Boolean campaign_name) {
        this.campaign_name = campaign_name;
    }

    public Boolean getCompany_name() {
        return company_name;
    }

    public void setCompany_name(Boolean company_name) {
        this.company_name = company_name;
    }

    public Boolean getInterest_date() {
        return interest_date;
    }

    public void setInterest_date(Boolean interest_date) {
        this.interest_date = interest_date;
    }

    public Boolean getNotes() {
        return notes;
    }

    public void setNotes(Boolean notes) {
        this.notes = notes;
    }

    public Boolean getWebsite() {
        return website;
    }

    public void setWebsite(Boolean website) {
        this.website = website;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getWorkspaceId() {
        return workspace_id;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspace_id = workspaceId;
    }
}
