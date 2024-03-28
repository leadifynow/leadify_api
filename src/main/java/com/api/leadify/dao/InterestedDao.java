package com.api.leadify.dao;

import com.api.leadify.entity.Campaign;
import com.api.leadify.entity.Interested;
import com.api.leadify.entity.Stage;
import com.api.leadify.entity.Workspace;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
@Slf4j
@Repository
public class InterestedDao {
    private final JdbcTemplate jdbcTemplate;
    private final WorkspaceDao workspaceDao;
    private final ObjectMapper objectMapper;
    private final CampaignDao campaignDao;
    private final StageDao stageDao;

    private static final Logger logger = LoggerFactory.getLogger(InterestedDao.class);

    @Autowired
    public InterestedDao(JdbcTemplate jdbcTemplate, WorkspaceDao workspaceDao, ObjectMapper objectMapper, CampaignDao campaignDao, StageDao stageDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.workspaceDao = workspaceDao;
        this.objectMapper = objectMapper;
        this.campaignDao = campaignDao;
        this.stageDao = stageDao;
    }
    public void createInterested(Interested interested) {

        try {
            // comentario de prueba
            // Sacamos los valores que necesitamos para validaciones, etc
            UUID workspaceId = interested.getWorkspace();
            UUID campaignId = interested.getCampaign_id();
            String campaignName = interested.getCampaign_name();
            String leadEmail = interested.getLead_email();

            // Check if the email already exists in the interested table
            String emailExistsQuery = "SELECT COUNT(*) FROM interested WHERE lead_email = ? AND workspace = ?";
            int emailCount = jdbcTemplate.queryForObject(emailExistsQuery, Integer.class, leadEmail, workspaceId.toString());

            if (emailCount > 0 && !Objects.equals(campaignName, "Didn't Close Re-Engage Campaign")) {
                // Email already exists, do nothing
                return;
            }

            // Primer si el existe el workspace
            if (!workspaceDao.workspaceExists(workspaceId)) {
                // Sino existe lo agregamos
                Workspace newWorkspace = new Workspace();
                newWorkspace.setId(workspaceId);
                newWorkspace.setName("Default Workspace Name");
                workspaceDao.createWorkspace(newWorkspace);

                // Create the Main stage
                Stage mainStage = new Stage();
                mainStage.setWorkspace_id(workspaceId);
                mainStage.setName("Main");
                mainStage.setFollowup(3);
                ApiResponse<Integer> createMainStageResponse = stageDao.createStage(mainStage);

//            if (createMainStageResponse.getCode() == 201) {
//                // Set the Main stage ID as the default stage_id for the Interested entity
//                interested.setStage_id(createMainStageResponse.getData());
//            } else {
//                // Handle error if stage creation fails
//                System.out.println("Error creating Main stage: " + createMainStageResponse.getMessage());
//                return; // Exit method
//            }

                // Check if "Not a Fit" stage exists
                ApiResponse<Integer> notFitStageIdResponse = stageDao.getStageIdByName(workspaceId, "Not a Fit");
                if (notFitStageIdResponse.getCode() == 404) {
                    // Create the "Not a Fit" stage if it doesn't exist
                    Stage notFitStage = new Stage();
                    notFitStage.setWorkspace_id(workspaceId);
                    notFitStage.setName("Not a Fit");
                    notFitStage.setFollowup(0);
                    ApiResponse<Integer> createNotFitStageResponse = stageDao.createStage(notFitStage);

                    if (createNotFitStageResponse.getCode() != 201) {
                        // Handle error if stage creation fails
                        System.out.println("Error creating Not a Fit stage: " + createNotFitStageResponse.getMessage());
                        return; // Exit method
                    }
                }

                // Create the "Custom date" stage
                Stage customDateStage = new Stage();
                customDateStage.setWorkspace_id(workspaceId);
                customDateStage.setName("Custom date");
                ApiResponse<Integer> createCustomDateStageResponse = stageDao.createStage(customDateStage);

                if (createCustomDateStageResponse.getCode() != 201) {
                    // Handle error if stage creation fails
                    System.out.println("Error creating Custom date stage: " + createCustomDateStageResponse.getMessage());
                    return; // Exit method
                }
            } else {
                // Retrieve the ID of the stage with the lowest position for the existing workspace
                ApiResponse<Integer> minPositionStageResponse = stageDao.getMinPositionStageId(workspaceId);
                if (minPositionStageResponse.getCode() == 200) {
                    interested.setStage_id(minPositionStageResponse.getData());
                } else {
                    // Handle error if retrieving the minimum position stage fails
                    System.out.println("Error retrieving minimum position stage: " + minPositionStageResponse.getMessage());
                    return; // Exit method
                }
            }

            // Checamos si existe la campaña
            if(!campaignDao.campaignExists(campaignId)) {
                // Sino existe la agregamos
                Campaign newCampaign = new Campaign();
                newCampaign.setId(campaignId);
                newCampaign.setWorkspace_id(workspaceId);
                newCampaign.setCampaign_name(campaignName);
                campaignDao.createCampaign(newCampaign);
            }

            // Insertamos en la table interested
            String insertQuery = "INSERT INTO interested (event_type, workspace, campaign_id, campaign_name, lead_email, title, email, " +
                    "website, industry, lastName, firstName, number_of_employees, companyName, linkedin_url, stage_id, booked) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            try {
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, interested.getEvent_type());
                    ps.setString(2, interested.getWorkspace().toString());
                    ps.setString(3, interested.getCampaign_id().toString());
                    ps.setString(4, interested.getCampaign_name());
                    ps.setString(5, interested.getLead_email());
                    ps.setString(6, interested.getTitle());
                    ps.setString(7, interested.getEmail());
                    ps.setString(8, interested.getWebsite());
                    ps.setString(9, interested.getIndustry());
                    ps.setString(10, interested.getLastName());
                    ps.setString(11, interested.getFirstName());
                    ps.setString(12, interested.getNumber_of_employees());
                    ps.setString(13, interested.getCompanyName());
                    ps.setString(14, interested.getLinkedin_url());
                    ps.setObject(15, null); // interested.getStage_id()
                    ps.setInt(16, 0); // booked status initially set to 0
                    return ps;
                }, keyHolder);
            } catch (DataAccessException e) {
                // Log the exception or handle it as required
                // You can throw a custom exception with an error message
                throw new RuntimeException("Failed to insert data into the database: " + e.getMessage());
            }

            // Get the generated interested_id
            int interestedId = keyHolder.getKey().intValue();
            // Check if the email exists in the booked table
            String emailExistsInBookedQuery = "SELECT COUNT(*) FROM booked WHERE email = ? AND (workspace_id = ? OR workspace_id IS NULL)";
            int emailExistsInBooked = jdbcTemplate.queryForObject(emailExistsInBookedQuery, Integer.class, leadEmail, interested.getWorkspace().toString());

            if (emailExistsInBooked > 0) {
                // If the email exists in booked table, update booked status to 1
                String updateInterestedQuery = "UPDATE interested SET booked = 1 WHERE lead_email = ?";
                jdbcTemplate.update(updateInterestedQuery, leadEmail);

                String updateBookedQuery = "UPDATE booked SET interested_id = ? WHERE email = ?";
                jdbcTemplate.update(updateBookedQuery, interestedId, leadEmail);
            }
        } catch (Exception e) {
            // Log the exception
            log.error("An error occurred: {}", e.getMessage(), e);

            // Serialize the Interested object to JSON
            String additionalData = serializeInterested(interested);

            // Save the error message and serialized Interested object to the error_log table
            String errorMessage = e.getMessage();
            String insertQuery = "INSERT INTO error_log (error_message, data) VALUES (?, ?)";
            jdbcTemplate.update(insertQuery, errorMessage, additionalData);

            // Rethrow the exception or handle it as appropriate
            throw new RuntimeException("An error occurred: " + errorMessage, e);
        }


    }
    private String serializeInterested(Interested interested) {
        try {
            // Use Jackson ObjectMapper to serialize the Interested object to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(interested);
        } catch (Exception e) {
            // Handle serialization exception
            log.error("Error serializing Interested object: {}", e.getMessage(), e);
            return null;
        }
    }
    public void updateStage(Integer stageId, Integer interestedId) {
        String sql = "UPDATE interested SET stage_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, stageId, interestedId);
    }
    public List<Interested> getAll() {
        String sql = "SELECT * FROM interested";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Interested.class));
    }
    public ApiResponse<PaginatedResponse<List<Interested>>> getAllByWorkspaceId(UUID workspaceId, int page, int pageSize) {
        try {
            // Query to retrieve all interested items based on workspace ID, where booked is 0 and manager is null
            String query = "SELECT * FROM interested WHERE workspace = ? AND booked = 0 AND manager IS NULL";

            // Execute the query and retrieve all interested items
            List<Interested> interestedList = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Interested.class), workspaceId.toString());

            // List to hold valid interested items
            List<Interested> validInterestedList = new ArrayList<>();

            // Iterate over the interested items to apply additional validations
            for (Interested interested : interestedList) {
                // Check if the next_update is today's date, null, or in the past,
                // and the stage is not "Not a Fit", "Completed", "Phone Call", or "Other"
                if ((interested.getNext_update() == null
                        || isNextUpdateToday(new Date(interested.getNext_update().getTime()))
                        || isNextUpdateInThePast(new Date(interested.getNext_update().getTime())))
                        && (interested.getStage_id() == null
                        || !isStageName(interested.getStage_id(), workspaceId, "Not a Fit"))
                        && (interested.getStage_id() == null
                        || !isStageName(interested.getStage_id(), workspaceId, "Completed"))
                        && (interested.getStage_id() == null
                        || !isStageName(interested.getStage_id(), workspaceId, "Phone Call"))
                        && (interested.getStage_id() == null
                        || !isStageName(interested.getStage_id(), workspaceId, "Other"))) {
                    // Add the interested item to the valid list
                    validInterestedList.add(interested);
                }
            }

//            // Convert Timestamps to Dates
//            for (Interested interested : validInterestedList) {
//                if (interested.getCreated_at() != null) {
//                    interested.setCreated_at(new Date(interested.getCreated_at().getTime()));
//                }
//                if (interested.getNext_update() != null) {
//                    interested.setNext_update(new Date(interested.getNext_update().getTime()));
//                }
//            }

            // Calculate total items after applying the filters
            int totalItems = validInterestedList.size();

            // Sort the valid interested items by the next_update date (oldest first), considering null values first
            Collections.sort(validInterestedList, Comparator.comparing(Interested::getNext_update,
                    Comparator.nullsFirst(Comparator.reverseOrder())));
            // Calculate total pages
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);

            // Validate page parameter
            if (page <= 0) {
                page = 1; // Default to the first page
            }

            // Validate pageSize parameter
            if (pageSize <= 0) {
                pageSize = 10; // Default page size
            }

            // Calculate offset
            int offset = (page - 1) * pageSize;

            // Apply pagination to the sorted and filtered list
            List<Interested> paginatedInterestedList = validInterestedList.subList(offset, Math.min(offset + pageSize, validInterestedList.size()));

            // Check if there is a next page
            boolean hasNextPage = page < totalPages;

            // Prepare paginated response
            PaginatedResponse<List<Interested>> paginatedResponse = new PaginatedResponse<>(paginatedInterestedList, page, pageSize, totalItems, totalPages, hasNextPage);

            if (paginatedInterestedList.isEmpty()) {
                return new ApiResponse<>("No interested items found for the given workspace ID or they are already booked or managed", null, 404);
            } else {
                return new ApiResponse<>("Interested items retrieved successfully", paginatedResponse, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log or handle the exception
            return new ApiResponse<>("Error retrieving interested items", null, 500);
        }
    }

    private boolean isStageName(int stageId, UUID workspaceId, String stageName) {
        int stageIdFromDB = getStageIdForName(stageName, workspaceId);
        return stageId == stageIdFromDB;
    }
    private boolean isNextUpdateInThePast(Date nextUpdateDate) {
        if (nextUpdateDate == null) {
            return false; // Treat null as future date
        }
        LocalDate nextUpdateLocalDate = nextUpdateDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return nextUpdateLocalDate.isBefore(LocalDate.now());
    }

    private boolean isNextUpdateToday(Date nextUpdateDate) {
        if (nextUpdateDate == null) {
            return true; // Treat null as today
        }
        LocalDate nextUpdateLocalDate = nextUpdateDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return nextUpdateLocalDate.isEqual(LocalDate.now());
    }
    private int getStageIdForName(String stageName, UUID workspaceId) {
        try {
            String stageIdSql = "SELECT id FROM stage WHERE workspace_id = ? AND name = ?";
            Integer stageId = jdbcTemplate.queryForObject(stageIdSql, Integer.class, workspaceId.toString(), stageName);
            return (stageId != null) ? stageId : -1; // Return -1 if not found
        } catch (DataAccessException e) {
            // Log or handle the exception
            return -1; // Return -1 in case of exception
        }
    }
    public ApiResponse<String> updateStage2(Integer interestedId, Integer stageId) {
        try {
            String sql = "UPDATE interested SET stage_id = ? WHERE id = ?";
            int affectedRows = jdbcTemplate.update(sql, stageId, interestedId);

            if (affectedRows > 0) {
                // Retrieve the followup value from the corresponding stage
                Integer followup = stageDao.getFollowupForStage(stageId);

                if (followup != null) {
                    // Calculate the next update date
                    LocalDateTime updatedAt = LocalDateTime.now(); // Assuming you have access to the updated_at date, here using current time
                    LocalDateTime nextUpdate = updatedAt.plusDays(followup);

                    // If the next update falls on a weekend, adjust it to the following Monday
                    DayOfWeek dayOfWeek = nextUpdate.getDayOfWeek();
                    if (dayOfWeek == DayOfWeek.SATURDAY) {
                        nextUpdate = nextUpdate.plusDays(2); // Move to Monday
                    } else if (dayOfWeek == DayOfWeek.SUNDAY) {
                        nextUpdate = nextUpdate.plusDays(1); // Move to Monday
                    }

                    // Update the next_update column in the interested table
                    String updateNextUpdateSql = "UPDATE interested SET next_update = ? WHERE id = ?";
                    jdbcTemplate.update(updateNextUpdateSql, Timestamp.valueOf(nextUpdate), interestedId);
                }

                return new ApiResponse<>("Stage updated successfully", null, 200);
            } else {
                return new ApiResponse<>("No interested item found with the given ID", null, 404);
            }
        } catch (Exception e) {
            return new ApiResponse<>("Error updating stage for interested item", null, 500);
        }
    }
    public ApiResponse<String> updateStageArray(JsonNode stageUpdates) {
        try {
            int updatedStagesCount = 0;

            for (JsonNode update : stageUpdates) {
                Integer interestedId = update.get("id").asInt();
                Integer stageId = update.get("stage_id").asInt();

                String sql = "UPDATE interested SET stage_id = ? WHERE id = ?";
                int affectedRows = jdbcTemplate.update(sql, stageId, interestedId);

                if (affectedRows > 0) {
                    // Retrieve the followup value from the corresponding stage
                    Integer followup = stageDao.getFollowupForStage(stageId);

                    if (followup != null) {
                        // Calculate the next update date
                        LocalDateTime updatedAt = LocalDateTime.now(); // Assuming you have access to the updated_at date, here using current time
                        LocalDateTime nextUpdate = updatedAt.plusDays(followup);

                        // If the next update falls on a weekend, adjust it to the following Monday
                        DayOfWeek dayOfWeek = nextUpdate.getDayOfWeek();
                        if (dayOfWeek == DayOfWeek.SATURDAY) {
                            nextUpdate = nextUpdate.plusDays(2); // Move to Monday
                        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
                            nextUpdate = nextUpdate.plusDays(1); // Move to Monday
                        }

                        // Update the next_update column in the interested table
                        String updateNextUpdateSql = "UPDATE interested SET next_update = ? WHERE id = ?";
                        jdbcTemplate.update(updateNextUpdateSql, Timestamp.valueOf(nextUpdate), interestedId);
                    }

                    updatedStagesCount++;
                }
            }

            return new ApiResponse<>("Updated " + updatedStagesCount + " stages", null, 200);
        } catch (Exception e) {
            return new ApiResponse<>("Error updating stages for interested items", null, 500);
        }
    }
    public ApiResponse<Void> updateManager(int interestedId, int managerId) {
        try {
            // Update the manager for the interested record
            String updateSql = "UPDATE interested SET manager = ? WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(updateSql, managerId, interestedId);

            if (rowsAffected > 0) {
                // Insert a notification for the manager
                String insertNotificationSql = "INSERT INTO notifications (title, description, user_id) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertNotificationSql, "New lead assigned", "You have been assigned as manager for a new lead", managerId);

                return new ApiResponse<>("Manager updated successfully for the interested record, and a notification has been sent", null, 200);
            } else {
                return new ApiResponse<>("No interested record found with the given ID", null, 404);
            }
        } catch (DataAccessException e) {
            String errorMessage = "Error updating manager for the interested record: " + Objects.requireNonNullElse(e.getLocalizedMessage(), "Unknown error");
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<String> updateInterestedNotes(int interestedId, String newNotes) {
        try {
            // Check if the interested exists
            newNotes = (Objects.equals(newNotes, "null")) ? "" : newNotes;
            String checkInterestedSql = "SELECT COUNT(*) FROM interested WHERE id = ?";
            int count = jdbcTemplate.queryForObject(checkInterestedSql, Integer.class, interestedId);
            if (count == 0) {
                return new ApiResponse<>("Interested not found", null, 404);
            }

            // Update notes
            String updateNotesSql = "UPDATE interested SET notes = ? WHERE id = ?";
            jdbcTemplate.update(updateNotesSql, newNotes, interestedId);

            return new ApiResponse<>("Notes updated successfully", null, 200);
        } catch (EmptyResultDataAccessException e) {
            return new ApiResponse<>("Interested not found", null, 404);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return new ApiResponse<>("Error updating notes", null, 500);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Unexpected error", null, 500);
        }
    }
    public ApiResponse<List<Interested>> searchInterestedRecords(String searchTerm, UUID workspaceId) {
        try {
            String sql = "SELECT * FROM interested WHERE workspace = ? AND (lead_email LIKE ? OR firstName LIKE ? OR lastName LIKE ? OR notes LIKE ?)";
            String searchTermLike = "%" + searchTerm + "%";
            List<Interested> interestedList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Interested.class),
                    workspaceId.toString(), searchTermLike, searchTermLike, searchTermLike, searchTermLike);
            return new ApiResponse<>("Interested records retrieved successfully", interestedList, 200);
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving interested records: " + e.getLocalizedMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<String> updateNextUpdateDate(Integer interestedId, LocalDate nextUpdateDate) {
        try {
            String sql = "UPDATE interested SET next_update = ? WHERE id = ?";
            int affectedRows = jdbcTemplate.update(sql, nextUpdateDate, interestedId);

            if (affectedRows > 0) {
                return new ApiResponse<>("Next update date updated successfully", null, 200);
            } else {
                return new ApiResponse<>("No interested item found with the given ID", null, 404);
            }
        } catch (Exception e) {
            return new ApiResponse<>("Error updating next update date for interested item", null, 500);
        }
    }
    public ApiResponse<Void> createManualInterested(Interested interested) {
        try {
            String emailExistsQuery = "SELECT COUNT(*) FROM interested WHERE lead_email = ? AND workspace = ?";
            int emailCount = jdbcTemplate.queryForObject(emailExistsQuery, Integer.class, interested.getLead_email(), interested.getWorkspace().toString());

            if (emailCount > 0) {
                return new ApiResponse<>("Lead already exists", null, 500);
            }

            String sql = "INSERT INTO interested (campaign_name, event_type, workspace, campaign_id, lead_email, email, lastName, firstName, companyName, stage_id, notes, booked, manager, next_update) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(sql,
                    interested.getCampaign_name(),
                    interested.getEvent_type(),
                    interested.getWorkspace().toString(),
                    interested.getCampaign_id().toString(),
                    interested.getLead_email(),
                    interested.getLead_email(),
                    interested.getLastName(),
                    interested.getFirstName(),
                    interested.getCompanyName(),
                    null, // Assuming stage_id is an Integer
                    interested.getNotes(),
                    false,
                    null,
                    null
            );

            // Get the generated interested_id
            String getInterestedIdQuery = "SELECT LAST_INSERT_ID()";
            int interestedId = jdbcTemplate.queryForObject(getInterestedIdQuery, Integer.class);
            // Check if the email exists in the booked table
            String emailExistsInBookedQuery = "SELECT COUNT(*) FROM booked WHERE email = ? AND (workspace_id = ? OR workspace_id IS NULL)";
            int emailExistsInBooked = jdbcTemplate.queryForObject(emailExistsInBookedQuery, Integer.class, interested.getLead_email(), interested.getWorkspace().toString());
            Boolean updateHappen = false;
            if (emailExistsInBooked > 0) {
                // If the email exists in booked table, update booked status to 1
                String updateInterestedQuery = "UPDATE interested SET booked = 1 WHERE lead_email = ?";
                jdbcTemplate.update(updateInterestedQuery, interested.getLead_email());

                String updateBookedQuery = "UPDATE booked SET interested_id = ? WHERE email = ?";
                jdbcTemplate.update(updateBookedQuery, interestedId, interested.getLead_email());

                updateHappen = true;
            }

            return new ApiResponse<>(updateHappen ? "Lead created and matched successfully" :"Lead created successfully", null, 201);
        } catch (DataAccessException e) {
            String errorMessage = "Error creating Lead: " + Objects.requireNonNullElse(e.getLocalizedMessage(), "Unknown error");
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<Void> deleteById(Integer id) {
        try {
            String sql = "DELETE FROM interested WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, id);
            if (rowsAffected > 0) {
                return new ApiResponse<>("Record deleted successfully", null, 200);
            } else {
                return new ApiResponse<>("No record found with the given ID", null, 404);
            }
        } catch (EmptyResultDataAccessException e) {
            // Handle case where no record is found with the given ID
            return new ApiResponse<>("No record found with the given ID", null, 404);
        } catch (DataAccessException e) {
            // Handle other database-related exceptions
            String errorMessage = "Error deleting record: " + e.getLocalizedMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<List<Interested>> getAllByManagerAndBookedIsZero(Integer manager) {
        try {
            String query = "SELECT i.*, w.name AS workspaceName " +
                    "FROM interested i " +
                    "JOIN workspace w ON i.workspace = w.id " +
                    "WHERE i.manager = ? AND i.booked = 0 " +
                    "ORDER BY i.created_at DESC";

            List<Interested> interestedList = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Interested.class), manager);

            if (interestedList.isEmpty()) {
                return new ApiResponse<>("No interested items found for the given manager where booked is 0", null, 404);
            } else {
                return new ApiResponse<>("Interested items retrieved successfully", interestedList, 200);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            // Log or handle the exception
            return new ApiResponse<>("Error retrieving interested items", null, 500);
        }
    }
    public ApiResponse<String> updateStageAndNextUpdateArray(JsonNode stageUpdates) {
        try {
            int updatedStagesCount = 0;

            for (JsonNode update : stageUpdates) {
                Integer interestedId = update.get("id").asInt();
                Integer stageId = update.get("stage_id").asInt();
                OffsetDateTime nextUpdateDateTime = OffsetDateTime.parse(update.get("next_update").asText());
                LocalDateTime nextUpdateDate = nextUpdateDateTime.toLocalDateTime();

                String sql = "UPDATE interested SET stage_id = ?, next_update = ? WHERE id = ?";
                int affectedRows = jdbcTemplate.update(sql, stageId, Timestamp.valueOf(nextUpdateDate), interestedId);

                if (affectedRows > 0) {
                    updatedStagesCount++;
                }
            }

            return new ApiResponse<>("Updated " + updatedStagesCount + " stages with next update date", null, 200);
        } catch (Exception e) {
            return new ApiResponse<>("Error updating stages and next update date for interested items: " + e.getMessage(), null, 500);
        }
    }
}
