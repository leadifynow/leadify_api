package com.api.leadify.dao;

import com.api.leadify.entity.Campaign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class CampaignDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CampaignDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean campaignExists(UUID campaignId) {
        String sql = "SELECT COUNT(*) FROM campaign WHERE id = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, campaignId.toString());
        return count >= 1;
    }

    public void createCampaign(Campaign campaign) {
        String sql = "INSERT INTO campaign (id, campaign_name, workspace_id) values(?, ? , ?)";
        jdbcTemplate.update(sql, campaign.getId().toString(), campaign.getCampaign_name(), campaign.getWorkspace_id().toString());
    }
}
