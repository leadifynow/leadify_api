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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
            System.out.println(interested);
            // Extract required values
            UUID workspaceId = interested.getWorkspace();
            UUID campaignId = interested.getCampaign_id();
            String campaignName = interested.getCampaign_name();
            String leadEmail = interested.getLead_email();

            // Check if the email already exists in the interested table
            String emailExistsQuery = "SELECT COUNT(*) FROM interested WHERE lead_email = ? AND workspace = ?";
            int emailCount = jdbcTemplate.queryForObject(emailExistsQuery, Integer.class, leadEmail, workspaceId.toString());

            if (emailCount > 0 && !Objects.equals(campaignName, "Didn't Close Re-Engage Campaign")
                    && !Objects.equals(campaignName, "No Shows/Ghosted/ Didn’t close | Subscription")
                    && !Objects.equals(campaignName, "Previous Clients | Subscription")) {
                // Email already exists, do nothing
                return;
            }

            // Check if the workspace exists
            if (!workspaceDao.workspaceExists(workspaceId)) {
                // If it doesn't exist, add it
                Workspace newWorkspace = new Workspace();
                newWorkspace.setId(workspaceId);
                newWorkspace.setName("Default Workspace Name");
                workspaceDao.createWorkspace(newWorkspace);

                // Create the Main stage
                Stage mainStage = new Stage();
                mainStage.setWorkspace_id(workspaceId);
                mainStage.setName("Main");
                mainStage.setFollowup(3);
                ResponseEntity<Integer> createMainStageResponse = stageDao.createStage(mainStage);

                // Check if "Not a Fit" stage exists
                ResponseEntity<Integer> notFitStageIdResponse = stageDao.getStageIdByName(workspaceId, "Not a Fit");
                if (notFitStageIdResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                    // Create the "Not a Fit" stage if it doesn't exist
                    Stage notFitStage = new Stage();
                    notFitStage.setWorkspace_id(workspaceId);
                    notFitStage.setName("Not a Fit");
                    notFitStage.setFollowup(0);
                    ResponseEntity<Integer> createNotFitStageResponse = stageDao.createStage(notFitStage);

                    if (createNotFitStageResponse.getStatusCode() != HttpStatus.OK) {
                        // Handle error if stage creation fails
                        System.out.println("Error creating Not a Fit stage: " + createNotFitStageResponse);
                        return; // Exit method
                    }
                }

                // Create the "Custom date" stage
                Stage customDateStage = new Stage();
                customDateStage.setWorkspace_id(workspaceId);
                customDateStage.setName("Custom date");
                ResponseEntity<Integer> createCustomDateStageResponse = stageDao.createStage(customDateStage);

                if (createCustomDateStageResponse.getStatusCode() != HttpStatus.OK) {
                    // Handle error if stage creation fails
                    System.out.println("Error creating Custom date stage: " + createCustomDateStageResponse);
                    return; // Exit method
                }
            } else {
                // Retrieve the ID of the stage with the lowest position for the existing workspace
                ResponseEntity<Integer> minPositionStageResponse = stageDao.getMinPositionStageId(workspaceId);
                if (minPositionStageResponse.getStatusCode() == HttpStatus.OK) { // Corrected condition here
                    Integer minPositionStageId = minPositionStageResponse.getBody();
                    interested.setStage_id(minPositionStageId);
                } else {
                    // Handle error if retrieving the minimum position stage fails
                    System.out.println("Error retrieving minimum position stage: " + minPositionStageResponse);
                    return; // Exit method
                }
            }

            // Check if the campaign exists
            if(!campaignDao.campaignExists(campaignId)) {
                // If it doesn't exist, add it
                Campaign newCampaign = new Campaign();
                newCampaign.setId(campaignId);
                newCampaign.setWorkspace_id(workspaceId);
                newCampaign.setCampaign_name(campaignName);
                campaignDao.createCampaign(newCampaign);
            }

            // Insert into the interested table
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
                    ps.setObject(15, interested.getStage_id()); // Use the stage_id from interested
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
            String emailExistsInBookedQuery = "SELECT COUNT(*) FROM booked WHERE email = ? AND (workspace_id = ? OR workspace_id IS NULL) AND interested_id IS NULL";
            int emailExistsInBooked = jdbcTemplate.queryForObject(emailExistsInBookedQuery, Integer.class, leadEmail, interested.getWorkspace().toString());

            if (emailExistsInBooked == 1) {
                // If the email exists in booked table, update booked status to 1
                String updateInterestedQuery = "UPDATE interested SET booked = 1 WHERE lead_email = ?";
                jdbcTemplate.update(updateInterestedQuery, leadEmail);

                String updateBookedQuery = "UPDATE booked SET interested_id = ? WHERE email = ? AND interested_id IS NULL";
                jdbcTemplate.update(updateBookedQuery, interestedId, leadEmail);

                // Update workspace_id in the booked table based on the interested table
                String updateWorkspaceSql = "UPDATE booked SET workspace_id = (SELECT workspace FROM interested WHERE lead_email = booked.email) WHERE email = ? AND workspace_id IS NULL";
                jdbcTemplate.update(updateWorkspaceSql, leadEmail);
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
    public ResponseEntity<PaginatedResponse<List<Interested>>> getAllByWorkspaceId(UUID workspaceId, int page, int pageSize) {
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No interested items found for the given workspace ID or they are already booked or managed", null, 404);
            } else {
                return ResponseEntity.ok(paginatedResponse);
                //return new ApiResponse<>("Interested items retrieved successfully", paginatedResponse, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log or handle the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error retrieving interested items", null, 500);
        }
    }
    public ResponseEntity<Page<Interested>> getInterested(
            UUID workspaceId, int page, int pageSize, Boolean booked, Integer stageId, String sortBy) {
        try {
            // Adjust page number because Spring Data pages are zero-based
            int adjustedPage = Math.max(page - 1, 0);
            pageSize = Math.max(pageSize, 1);
            int offset = adjustedPage * pageSize;

            // Set default value for 'booked' if it is null
            if (booked == null) {
                booked = false;
            }

            // Build the SQL query with named parameters
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT i.* ")
                    .append("FROM interested i ")
                    .append("LEFT JOIN stage s ON i.stage_id = s.id ")
                    .append("WHERE i.workspace = :workspace ")
                    .append("AND i.manager IS NULL ")
                    .append("AND i.booked = :booked ");

            // Set query parameters
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("workspace", workspaceId.toString());
            params.addValue("booked", booked ? 1 : 0);

            // Add stageId condition if the stageId parameter is not null
            if (stageId != null) {
                sqlBuilder.append("AND i.stage_id = :stageId ");
                params.addValue("stageId", stageId);
            } else {
                // Add excludedStageNames condition only when stageId is null
                List<String> excludedStageNames =
                        Arrays.asList("Not a Fit", "Completed", "Phone Call", "Other");
                sqlBuilder.append("AND (s.name IS NULL OR s.name NOT IN (:excludedStageNames)) ");
                params.addValue("excludedStageNames", excludedStageNames);
            }

            // Optionally remove or adjust the filtering condition
            // Comment out or delete the following line to include all leads
            // sqlBuilder.append("AND (i.stage_id IS NULL OR i.next_update IS NULL OR i.next_update <= CURDATE()) ");

            // Determine the ORDER BY clause based on sortBy
            String orderByClause;
            if (sortBy == null) {
                // Changed default sorting to newest leads first
                orderByClause = "ORDER BY i.created_at DESC ";
            } else {
                switch (sortBy) {
                    case "Newest Leads":
                        orderByClause = "ORDER BY i.created_at DESC ";
                        break;
                    case "Oldest Leads":
                        orderByClause = "ORDER BY i.created_at ASC ";
                        break;
                    case "Last modified":
                        orderByClause = "ORDER BY i.updated_at DESC ";
                        break;
                    case "Next update":
                        // Custom ordering for 'Next update'
                        orderByClause = "ORDER BY "
                                + "CASE "
                                + "WHEN i.next_update < CURDATE() THEN 0 " // Overdue updates
                                + "WHEN i.next_update = CURDATE() THEN 1 " // Updates due today
                                + "WHEN i.next_update > CURDATE() THEN 2 " // Future updates
                                + "ELSE 3 " // Null or undefined
                                + "END, "
                                + "i.next_update ASC ";
                        break;
                    case "Next Day":
                        // Filter to get leads that need an update the next day
                        sqlBuilder.append("AND i.next_update = CURDATE() + INTERVAL 1 DAY ");
                        orderByClause = "ORDER BY i.next_update ASC ";
                        break;
                    default:
                        // Invalid sortBy value, default to 'Newest Leads'
                        orderByClause = "ORDER BY i.created_at DESC ";
                        break;
                }
            }

            // Append ORDER BY, LIMIT, OFFSET
            sqlBuilder.append(orderByClause);
            sqlBuilder.append("LIMIT :limit OFFSET :offset");

            params.addValue("limit", pageSize);
            params.addValue("offset", offset);

            String sql = sqlBuilder.toString();

            // Use NamedParameterJdbcTemplate for named parameters and IN clause handling
            NamedParameterJdbcTemplate namedJdbcTemplate =
                    new NamedParameterJdbcTemplate(jdbcTemplate);

            // Execute the query to get the data
            List<Interested> interestedList =
                    namedJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Interested.class));

            // Fetch total count for pagination
            StringBuilder countSqlBuilder = new StringBuilder();
            countSqlBuilder.append("SELECT COUNT(*) ")
                    .append("FROM interested i ")
                    .append("LEFT JOIN stage s ON i.stage_id = s.id ")
                    .append("WHERE i.workspace = :workspace ")
                    .append("AND i.manager IS NULL ")
                    .append("AND i.booked = :booked ");

            // Add stageId condition to the count query if stageId parameter is not null
            if (stageId != null) {
                countSqlBuilder.append("AND i.stage_id = :stageId ");
                // stageId parameter is already added to params
            } else {
                // Add excludedStageNames condition only when stageId is null
                countSqlBuilder.append("AND (s.name IS NULL OR s.name NOT IN (:excludedStageNames)) ");
                // excludedStageNames parameter is already added to params
            }

            // Optionally remove or adjust the filtering condition in the count query
            // Comment out or delete the following line if removed from the main query
            // countSqlBuilder.append("AND (i.stage_id IS NULL OR i.next_update IS NULL OR i.next_update <= CURDATE()) ");

            String countSql = countSqlBuilder.toString();

            int totalItems = namedJdbcTemplate.queryForObject(countSql, params, Integer.class);

            // Create Pageable instance
            Pageable pageable = PageRequest.of(adjustedPage, pageSize);

            // Create Page instance
            Page<Interested> interestedPage =
                    new PageImpl<>(interestedList, pageable, totalItems);

            // Return the result
            return ResponseEntity.ok(interestedPage);

        } catch (Exception e) {
            e.printStackTrace();
            // Log or handle the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
    public ResponseEntity<String> updateStage2(Integer interestedId, Integer stageId) {
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

                return new ResponseEntity<>("Stage updated successfully", null, 200);
            } else {
                return new ResponseEntity<>("No interested item found with the given ID", null, 404);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating stage for interested item", null, 500);
        }
    }
    public ResponseEntity<String> updateStageArray(JsonNode stageUpdates) {
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

            return new ResponseEntity<>("Updated " + updatedStagesCount + " stages", null, 200);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating stages for interested items", null, 500);
        }
    }
    public ResponseEntity<Void> updateManager(int interestedId, int managerId) {
        try {
            // Update the manager for the interested record
            String updateSql = "UPDATE interested SET manager = ? WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(updateSql, managerId, interestedId);

            if (rowsAffected > 0) {
                // Insert a notification for the manager
                String insertNotificationSql = "INSERT INTO notifications (title, description, user_id) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertNotificationSql, "New lead assigned", "You have been assigned as manager for a new lead", managerId);
                
                return ResponseEntity.ok().build();
                //return new ResponseEntity<>("Manager updated successfully for the interested record, and a notification has been sent", null, 200);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ResponseEntity<>("No interested record found with the given ID", null, 404);
            }
        } catch (DataAccessException e) {
            //String errorMessage = "Error updating manager for the interested record: " + Objects.requireNonNullElse(e.getLocalizedMessage(), "Unknown error");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ResponseEntity<>(errorMessage, null, 500);
        }
    }
    public ResponseEntity<String> updateInterestedNotes(int interestedId, String newNotes) {
        try {
            // Check if the interested exists
            newNotes = (Objects.equals(newNotes, "null")) ? "" : newNotes;
            String checkInterestedSql = "SELECT COUNT(*) FROM interested WHERE id = ?";
            int count = jdbcTemplate.queryForObject(checkInterestedSql, Integer.class, interestedId);
            if (count == 0) {
                return new ResponseEntity<>("Interested not found", null, 404);
            }

            // Update notes
            String updateNotesSql = "UPDATE interested SET notes = ? WHERE id = ?";
            jdbcTemplate.update(updateNotesSql, newNotes, interestedId);

            return new ResponseEntity<>("Notes updated successfully", null, 200);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity<>("Interested not found", null, 404);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating notes", null, 500);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Unexpected error", null, 500);
        }
    }
    public ResponseEntity<List<Interested>> searchInterestedRecords(String searchTerm, UUID workspaceId) {
        try {
            String sql = "SELECT * FROM interested WHERE workspace = ? AND (lead_email LIKE ? OR firstName LIKE ? OR lastName LIKE ? OR notes LIKE ?)";
            String searchTermLike = "%" + searchTerm + "%";
            List<Interested> interestedList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Interested.class),
                    workspaceId.toString(), searchTermLike, searchTermLike, searchTermLike, searchTermLike);
            return ResponseEntity.ok(interestedList);
            //return new ApiResponse<>("Interested records retrieved successfully", interestedList, 200);
        } catch (DataAccessException e) {
            //String errorMessage = "Error retrieving interested records: " + e.getLocalizedMessage();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ResponseEntity<String> updateNextUpdateDate(Integer interestedId, LocalDate nextUpdateDate) {
        try {
            String sql = "UPDATE interested SET next_update = ? WHERE id = ?";
            int affectedRows = jdbcTemplate.update(sql, nextUpdateDate, interestedId);

            if (affectedRows > 0) {
                return new ResponseEntity<>("Next update date updated successfully", null, 200);
            } else {
                return new ResponseEntity<>("No interested item found with the given ID", null, 404);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating next update date for interested item", null, 500);
        }
    }
    public ResponseEntity<Interested> createManualInterested(Interested interested) {
        try {
            // Insert the new Interested record
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
                    null,  // Assuming stage_id is an Integer
                    interested.getNotes(),
                    false, // Default booked to false
                    null,  // manager
                    null   // next_update
            );

            // Get the generated interested_id
            String getInterestedIdQuery = "SELECT LAST_INSERT_ID()";
            int interestedId = jdbcTemplate.queryForObject(getInterestedIdQuery, Integer.class);

            // Fetch the newly created Interested record
            String fetchNewInterestedQuery = "SELECT * FROM interested WHERE id = ?";
            Interested newInterested = jdbcTemplate.queryForObject(fetchNewInterestedQuery, new BeanPropertyRowMapper<>(Interested.class), interestedId);

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

            return ResponseEntity.status(HttpStatus.CREATED).body(newInterested);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    public ResponseEntity<Void> deleteById(Integer id) {
        try {
            String sql = "DELETE FROM interested WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, id);
            if (rowsAffected > 0) {
                return ResponseEntity.ok().build();
                //return new ApiResponse<>("Record deleted successfully", null, 200);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No record found with the given ID", null, 404);
            }
        } catch (EmptyResultDataAccessException e) {
            // Handle case where no record is found with the given ID
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            //return new ApiResponse<>("No record found with the given ID", null, 404);
        } catch (DataAccessException e) {
            // Handle other database-related exceptions
            //String errorMessage = "Error deleting record: " + e.getLocalizedMessage();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ResponseEntity<List<Interested>> getAllByManagerAndBookedIsZero(Integer manager) {
        try {
            String query = "SELECT i.*, w.name AS workspaceName " +
                    "FROM interested i " +
                    "JOIN workspace w ON i.workspace = w.id " +
                    "WHERE i.manager = ? AND i.booked = 0 " +
                    "ORDER BY i.created_at DESC";

            List<Interested> interestedList = jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Interested.class), manager);

            if (interestedList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No interested items found for the given manager where booked is 0", null, 404);
            } else {
                return ResponseEntity.ok(interestedList);
                //return new ApiResponse<>("Interested items retrieved successfully", interestedList, 200);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            // Log or handle the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error retrieving interested items", null, 500);
        }
    }
    public ResponseEntity<String> updateStageAndNextUpdateArray(JsonNode stageUpdates) {
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

            return new ResponseEntity<>("Updated " + updatedStagesCount + " stages with next update date", null, 200);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating stages and next update date for interested items: " + e.getMessage(), null, 500);
        }
    }



    public ResponseEntity<Page<Interested>> getInterestedWithOutFilter(
        UUID workspaceId, int page, int pageSize, String search) {
    try {
        // Adjust page number because Spring Data pages are zero-based
        int adjustedPage = Math.max(page - 1, 0);
        pageSize = Math.max(pageSize, 1);
        int offset = adjustedPage * pageSize;

        // Build the SQL query with named parameters
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT i.* ")
                .append("FROM interested i ")
                .append("LEFT JOIN stage s ON i.stage_id = s.id ")
                .append("WHERE i.workspace = :workspace ")
                .append("AND i.manager IS NULL ")
                .append("AND (i.stage_id IS NULL OR i.next_update <= CURDATE()) ");

        // Set query parameters
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("workspace", workspaceId.toString());

        //Add Search Parameter 
        if (search != null && !search.trim().isEmpty()) {
            try {
                Long searchId = Long.parseLong(search);
                sqlBuilder.append("AND CAST(i.id AS CHAR) LIKE :searchId ");
                params.addValue("searchId", search + "%");
            } catch (NumberFormatException e) {
                sqlBuilder.append("AND i.email LIKE :searchEmail ");
                params.addValue("searchEmail", "%" + search + "%");
            }
        }

        // Append LIMIT, OFFSET
        sqlBuilder.append("LIMIT :limit OFFSET :offset");

        params.addValue("limit", pageSize);
        params.addValue("offset", offset);

        String sql = sqlBuilder.toString();

        // Use NamedParameterJdbcTemplate for named parameters and IN clause handling
        NamedParameterJdbcTemplate namedJdbcTemplate =
                new NamedParameterJdbcTemplate(jdbcTemplate);

        // Execute the query to get the data
        List<Interested> interestedList =
                namedJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Interested.class));

        // Fetch total count for pagination
        StringBuilder countSqlBuilder = new StringBuilder();
        countSqlBuilder.append("SELECT COUNT(*) ")
                .append("FROM interested i ")
                .append("LEFT JOIN stage s ON i.stage_id = s.id ")
                .append("WHERE i.workspace = :workspace ")
                .append("AND i.manager IS NULL ")
                .append("AND (i.stage_id IS NULL OR i.next_update <= CURDATE()) ");


        if (search != null && !search.trim().isEmpty()) {
            try {
                Long searchId = Long.parseLong(search);
                countSqlBuilder.append("AND CAST(i.id AS CHAR) LIKE :searchId ");
            } catch (NumberFormatException e) {
                countSqlBuilder.append("AND i.email LIKE :searchEmail ");
                }
        }

        String countSql = countSqlBuilder.toString();

        int totalItems = namedJdbcTemplate.queryForObject(countSql, params, Integer.class);

        // Create Pageable instance
        Pageable pageable = PageRequest.of(adjustedPage, pageSize);

        // Create Page instance
        Page<Interested> interestedPage =
                new PageImpl<>(interestedList, pageable, totalItems);

        // Return the result
        return ResponseEntity.ok(interestedPage);

    } catch (Exception e) {
        e.printStackTrace();
        // Log or handle the exception
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

}
