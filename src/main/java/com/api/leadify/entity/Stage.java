package com.api.leadify.entity;

import java.util.UUID;

public class Stage {
    private int id;
    private String name;
    private String description;
    private UUID workspace_id;
    private Integer position_workspace;
    private String color;
    private Integer followup;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getFollowup() {
        return followup;
    }

    public void setFollowup(Integer followup) {
        this.followup = followup;
    }

    public UUID getWorkspace_id() {
        return workspace_id;
    }

    public void setWorkspace_id(UUID workspace_id) {
        this.workspace_id = workspace_id;
    }

    public Integer getPosition_workspace() {
        return position_workspace;
    }

    public void setPosition_workspace(Integer position_workspace) {
        this.position_workspace = position_workspace;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
