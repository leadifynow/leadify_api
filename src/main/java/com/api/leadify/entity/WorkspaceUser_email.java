package com.api.leadify.entity;

public class WorkspaceUser_email extends WorkspaceUser{
    private String email;

    // getters and setters for email

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }
}
