package com.api.leadify.entity;

import java.util.UUID;

public abstract class WorkspaceUser {
    private int id;
    private UUID workspace_id;
    private int user_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getWorkspace_id() {
        return workspace_id;
    }

    public void setWorkspace_id(UUID workspace_id) {
        this.workspace_id = workspace_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public abstract String getEmail();

    public abstract void setEmail(String email);
}
