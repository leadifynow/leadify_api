package com.api.leadify.entity;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class Booked {
    private int id;
    private String email;
    private String first_name;
    private String last_name;
    private String name;
    private String text_reminder_number;
    private String timezone;
    private Integer interested_id;
    private Integer company_id;

    public Integer getCompany_id() {
        return company_id;
    }

    public void setCompany_id(Integer company_id) {
        this.company_id = company_id;
    }

    private JsonNode payload;
    private List<QuestionsAndAnswers> questionsAndAnswers;

    // Getters and setters for all fields

    public List<QuestionsAndAnswers> getQuestionsAndAnswers() {
        return questionsAndAnswers;
    }

    public void setQuestionsAndAnswers(List<QuestionsAndAnswers> questionsAndAnswers) {
        this.questionsAndAnswers = questionsAndAnswers;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText_reminder_number() {
        return text_reminder_number;
    }

    public void setText_reminder_number(String text_reminder_number) {
        this.text_reminder_number = text_reminder_number;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getInterested_id() {
        return interested_id;
    }

    public void setInterested_id(Integer interested_id) {
        this.interested_id = interested_id;
    }
}
