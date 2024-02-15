package com.api.leadify.entity;

import java.util.UUID;

public class Campaign {
    private UUID id;
    private String campaign_name;
    private UUID workspace_id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCampaign_name() {
        return campaign_name;
    }

    public void setCampaign_name(String campaign_name) {
        this.campaign_name = campaign_name;
    }

    public UUID getWorkspace_id() {
        return workspace_id;
    }

    public void setWorkspace_id(UUID workspace_id) {
        this.workspace_id = workspace_id;
    }
}
