package com.api.leadify.dao;

import com.api.leadify.entity.Booked;
import com.api.leadify.entity.Interested;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
@Slf4j
@Repository
public class BookedDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BookedDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ApiResponse<Void> createBooked(Booked booked, int companyId) {
        try {
            JsonNode payloadNode = booked.getPayload();

            // Extracting basic booking information
            String email = payloadNode.get("email").asText();
            String firstName = payloadNode.get("first_name").asText();
            String lastName = payloadNode.get("last_name").asText();
            String name = payloadNode.get("name").asText();
            String textReminderNumber = payloadNode.get("text_reminder_number").asText();
            String timezone = payloadNode.get("timezone").asText();
            Integer company_id = companyId;
            JsonNode forEvent = payloadNode.get("scheduled_event");
            String event_name = forEvent.get("name").asText();
            String meetingDateTimeStr = forEvent.get("start_time").asText();
            LocalDateTime meetingDateTime = LocalDateTime.parse(meetingDateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            String formattedMeetingDateTime = meetingDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            JsonNode event_members = forEvent.get("event_memberships");
            String publicist = null;
            JsonNode firstMember = event_members.get(0);  // Get the object at the first position
            publicist = firstMember.get("user_name").asText();

            UUID workspaceId = null;
            if (event_name.equals("Mindful Agency - Initial Consultation")
                    || event_name.equals("Mindful Agency - Strategy Consultation")
                    || event_name.equals("Mindful Agency - Premium Consultation")
                    || event_name.equals("Mindful Agency - Elite Consultation")) {
                String sql = "SELECT id FROM workspace WHERE name = ?";
                try {
                    // Execute the SELECT query and retrieve the ID
                    workspaceId = jdbcTemplate.queryForObject(sql, UUID.class, "Mindful Agency - Lauren");
                } catch (EmptyResultDataAccessException e) {
                    // Handle case when no workspace with the given name is found
                    System.out.println("No workspace found for the given name.");
                } catch (DataAccessException e) {
                    // Handle other data access exceptions
                    e.printStackTrace();
                }
            } else if (event_name.equals("Mindful Agency - Initial PR Consultation")
                    || event_name.equals("Mindful Agency - Strategy PR Consultation")
                    || event_name.equals("Mindful Agency - Premium PR Consultation")
                    || event_name.equals("Mindful Agency - Elite PR Consultation")) {
                String sql = "SELECT id FROM workspace WHERE name = ?";
                try {
                    // Execute the SELECT query and retrieve the ID
                    workspaceId = jdbcTemplate.queryForObject(sql, UUID.class, "Mindful Agency - Natalie");
                } catch (EmptyResultDataAccessException e) {
                    // Handle case when no workspace with the given name is found
                    System.out.println("No workspace found for the given name.");
                } catch (DataAccessException e) {
                    // Handle other data access exceptions
                    e.printStackTrace();
                }
            } else if (event_name.equals("Mindful Agency - Discovery Call") || event_name.equals("Mindful Agency - Follow-Up Call")){
                try {
                    workspaceId = null;
                }catch (EmptyResultDataAccessException e) {
                    // Handle case when no workspace with the given name is found
                    System.out.println("No workspace found for the given name.");
                } catch (DataAccessException e) {
                    // Handle other data access exceptions
                    e.printStackTrace();
                }
                // If the event name doesn't match any of the specified conditions, return an appropriate response
            } else {
                return new ApiResponse<>("Invalid event name", null, 400);
            }
            /// cambiar esto a un count como el de interesado
            // Check if the email exists in the interested table
            String interestedIdQuery = "SELECT id FROM interested WHERE lead_email = ? AND booked = 0";
            Integer interestedId = null;
            try {
                List<Integer> interestedIds = jdbcTemplate.queryForList(interestedIdQuery, Integer.class, email);
                if (interestedIds.size() == 1) {
                    interestedId = interestedIds.get(0);
                    String workspaceQuery = "SELECT workspace from interested where id = ?";
                    UUID workspace = UUID.fromString(jdbcTemplate.queryForObject(workspaceQuery, String.class, interestedId));
                    String updateInterestedSql = "UPDATE interested SET booked = 1 WHERE id = ?";
                    jdbcTemplate.update(updateInterestedSql, interestedId);
                    workspaceId = workspace;
                }
            } catch (EmptyResultDataAccessException e) {
                interestedId = null;
            }

            // Inserting basic booking information into the database
            String sql = "INSERT INTO booked (email, first_name, last_name, name, text_reminder_number, timezone, interested_id, company_id, workspace_id, event_name, publicist, meeting_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            Integer finalInterestedId = interestedId;
            UUID finalWorkspaceId = workspaceId;
            String finalPublicist = publicist;
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, email);
                ps.setString(2, firstName);
                ps.setString(3, lastName);
                ps.setString(4, name);
                ps.setString(5, textReminderNumber);
                ps.setString(6, timezone);
                if (finalInterestedId != null) {
                    ps.setInt(7, finalInterestedId);
                } else {
                    ps.setNull(7, Types.INTEGER);
                }
                ps.setInt(8, company_id);
                if (finalWorkspaceId != null) {
                    ps.setString(9, finalWorkspaceId.toString());
                } else {
                    ps.setNull(9, Types.INTEGER);
                }
                ps.setString(10, event_name);
                ps.setString(11, finalPublicist);
                ps.setString(12, formattedMeetingDateTime);
                return ps;
            }, keyHolder);

            // Retrieve the auto-generated key (booked_id)
            int bookedId = keyHolder.getKey().intValue();

// Extracting questions and answers
            JsonNode questionsAndAnswersNode = payloadNode.get("questions_and_answers");
            if (questionsAndAnswersNode != null && questionsAndAnswersNode.isArray()) {
                for (JsonNode qaNode : questionsAndAnswersNode) {
                    String question = qaNode.get("question").asText();
                    String answer = qaNode.get("answer").asText();

                    // Inserting question and answer into the database with the retrieved booked_id
                    String qaSql = "INSERT INTO questions_and_answers (question, answer, booked_id) " +
                            "VALUES (?, ?, ?)";
                    jdbcTemplate.update(qaSql, question, answer, bookedId);

                    // Check if the question is "Please provide your business name."
                    if ("Please provide your business name.".equals(question)) {
                        // Update the business column in the booked table
                        String updateBusinessSql = "UPDATE booked SET business = ? WHERE id = ?";
                        jdbcTemplate.update(updateBusinessSql, answer, bookedId);
                    }

                    // Check if the question is "How did you hear about Mindful Agency?"
                    if ("How did you hear about Mindful Agency?".equals(question)) {
                        // Update the referral column in the booked table
                        String updateReferralSql = "UPDATE booked SET referral = ? WHERE id = ?";
                        jdbcTemplate.update(updateReferralSql, answer, bookedId);
                    }
                    if ("Please provide your website".equals(question)) {
                        // Update the website column in the booked table
                        String updateWebsiteSql = "UPDATE booked SET website = ? WHERE id = ?";
                        jdbcTemplate.update(updateWebsiteSql, answer, bookedId);
                    }
                }
            }

            return new ApiResponse<>("Booked created successfully", null, 201);
        } catch (Exception e) {
            log.error("An error occurred: {}", e.getMessage(), e);
            JsonNode payloadNode = booked.getPayload();
            String errorMessage = e.getMessage();
            String insertQuery = "INSERT INTO error_log (error_message, data) VALUES (?, ?)";
            jdbcTemplate.update(insertQuery, errorMessage, payloadNode.toString());
            throw new RuntimeException("An error occurred: " + errorMessage, e);
        }
    }
    public ApiResponse<PaginatedResponse<List<Booked>>> getAllBookedByCompanyId(int companyId, String workspaceId, int page, int pageSize, int filterType, String startDate, String endDate) {
        try {
            // Construct base SQL query without filter
            String baseSql = "SELECT COUNT(*) FROM booked WHERE company_id = ? AND (workspace_id = ? OR workspace_id IS NULL) AND deleted = 0";

            // Construct SQL query based on filterType parameter
            String countSql;
            if (filterType == 1) {
                countSql = baseSql;
            } else if (filterType == 2) {
                countSql = baseSql + " AND interested_id IS NOT NULL";
            } else if (filterType == 3) {
                countSql = baseSql + " AND interested_id IS NULL";
            } else {
                throw new IllegalArgumentException("Invalid filterType: " + filterType);
            }

            // Add filter conditions for startDate and endDate if they are not null
            if (startDate != null && endDate != null) {
                countSql += " AND created_at BETWEEN ? AND ?";
            }

            if (startDate != null && !startDate.isEmpty()) {
                startDate = startDate.substring(0, 10) + "T00:00:00.000Z";
            }

// For endDate, set time to 23:59:59.999
            if (endDate != null && !endDate.isEmpty()) {
                endDate = endDate.substring(0, 10) + "T23:59:59.999Z";
            }

            // Query to count total bookings
            int totalItems = 0;
            if (startDate != null && endDate != null) {
                totalItems = jdbcTemplate.queryForObject(countSql, Integer.class, companyId, workspaceId, startDate, endDate);
            } else {
                totalItems = jdbcTemplate.queryForObject(countSql, Integer.class, companyId, workspaceId);
            }

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

            // Construct SQL query based on filterType parameter for retrieving paginated bookings
            String sql;
            if (filterType == 1) {
                sql = "SELECT * FROM booked WHERE company_id = ? AND (workspace_id = ? OR workspace_id IS NULL) AND deleted = 0 ";
            } else if (filterType == 2) {
                sql = "SELECT * FROM booked WHERE company_id = ? AND (workspace_id = ? OR workspace_id IS NULL) AND deleted = 0 " +
                        "AND interested_id IS NOT NULL ";
            } else if (filterType == 3) {
                sql = "SELECT * FROM booked WHERE company_id = ? AND (workspace_id = ? OR workspace_id IS NULL) AND deleted = 0 " +
                        "AND interested_id IS NULL ";
            } else {
                throw new IllegalArgumentException("Invalid filterType: " + filterType);
            }

            // Add filter conditions for startDate and endDate if they are not null
            if (startDate != null && endDate != null) {
                sql += "AND created_at BETWEEN ? AND ? ";
            }

            // Adding ORDER BY clause for most recent bookings
            sql += "ORDER BY created_at DESC LIMIT ? OFFSET ?";

            // Retrieve paginated bookings based on the constructed SQL query
            List<Booked> bookedList = null;
            if (startDate != null && endDate != null) {
                bookedList = jdbcTemplate.query(sql, new Object[]{companyId, workspaceId, startDate, endDate, pageSize, offset}, new BeanPropertyRowMapper<>(Booked.class));
            } else {
                bookedList = jdbcTemplate.query(sql, new Object[]{companyId, workspaceId, pageSize, offset}, new BeanPropertyRowMapper<>(Booked.class));
            }

            // Check if there is a next page
            boolean hasNextPage = page < totalPages;

            // Prepare paginated response
            PaginatedResponse<List<Booked>> paginatedResponse = new PaginatedResponse<>(bookedList, page, pageSize, totalItems, totalPages, hasNextPage);

            if (bookedList.isEmpty()) {
                return new ApiResponse<>("No bookings found for the given company ID and workspace ID", null, 404);
            } else {
                return new ApiResponse<>("Bookings retrieved successfully", paginatedResponse, 200);
            }
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving bookings by company ID and workspace ID. Details: " + e.getLocalizedMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<List<Booked>> searchBookedRecords(String searchTerm, int companyId, String workspace) {
        try {
            String sql = "SELECT i.id, i.event_type AS event_name, i.workspace AS workspace_id, i.campaign_id AS campaign_id, i.campaign_name AS campaign_name, i.lead_email AS email, i.title, i.email AS second_email, " +
                    "i.website, i.industry, i.lastName AS last_name, i.firstName AS first_name, i.number_of_employees AS business, i.companyName AS name, i.linkedin_url, i.stage_id, i.notes, 0 as booked " +
                    "FROM interested i " +
                    "JOIN workspace w ON i.workspace = w.id " +
                    "JOIN company c ON w.company_id = c.id " +
                    "WHERE (i.id = ? OR i.lead_email LIKE ? OR i.firstName LIKE ? OR i.lastName LIKE ? OR i.notes LIKE ?) AND c.id = ? AND i.workspace = ?";
            String searchTermLike = "%" + searchTerm + "%";
            List<Booked> bookedList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booked.class), searchTerm, searchTermLike, searchTermLike, searchTermLike, searchTermLike, companyId, workspace);
            return new ApiResponse<>("Booked records retrieved successfully", bookedList, 200);
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving booked records: " + e.getLocalizedMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<Booked> updateBookedAndInterested(int interestedId, int bookedId) {
        try {
            // Update interested_id in the booked table
            String updateBookedSql = "UPDATE booked SET interested_id = ? WHERE id = ?";
            int updatedBookedRows = jdbcTemplate.update(updateBookedSql, interestedId, bookedId);

            // Update booked column to 1 in the interested table
            String updateInterestedSql = "UPDATE interested SET booked = 1 WHERE id = ?";
            int updatedInterestedRows = jdbcTemplate.update(updateInterestedSql, interestedId);

            if (updatedBookedRows > 0 && updatedInterestedRows > 0) {
                // Query the updated record from the booked table
                String selectBookedSql = "SELECT * FROM booked WHERE id = ?";
                Booked updatedBooked = jdbcTemplate.queryForObject(selectBookedSql, new Object[]{bookedId}, new BeanPropertyRowMapper<>(Booked.class));

                if (updatedBooked != null) {
                    return new ApiResponse<>("Booked and interested records updated successfully", updatedBooked, 200);
                } else {
                    return new ApiResponse<>("Failed to retrieve updated booked record", null, 500);
                }
            } else {
                return new ApiResponse<>("No records updated", null, 404);
            }
        } catch (DataAccessException e) {
            String errorMessage = "Error updating booked and interested records: " + Objects.requireNonNullElse(e.getLocalizedMessage(), "Unknown error");
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<Interested> getInterestedByBookedId(int bookedId) {
        try {
            String sql = "SELECT id FROM interested WHERE id = (SELECT interested_id FROM booked WHERE id = ?)";
            Integer interestedId = jdbcTemplate.queryForObject(sql, Integer.class, bookedId);

            if (interestedId != null) {
                String interestedSql = "SELECT * FROM interested WHERE id = ?";
                Interested interested = jdbcTemplate.queryForObject(interestedSql, new BeanPropertyRowMapper<>(Interested.class), interestedId);
                return new ApiResponse<>("Interested record retrieved successfully", interested, 200);
            } else {
                return new ApiResponse<>("No interested record found for the given booked id", null, 404);
            }
        } catch (EmptyResultDataAccessException e) {
            return new ApiResponse<>("No interested record found for the given booked id", null, 404);
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving interested record: " + Objects.requireNonNullElse(e.getLocalizedMessage(), "Unknown error");
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<Void> resetInterestedAndBooked(int interestedId) {
        try {
            // Reset booked = 0 in the interested table
            String updateInterestedSql = "UPDATE interested SET booked = 0 WHERE id = ?";
            int updatedInterestedRows = jdbcTemplate.update(updateInterestedSql, interestedId);

            // Reset interested_id to null in the booked table
            String updateBookedSql = "UPDATE booked SET interested_id = NULL WHERE interested_id = ?";
            int updatedBookedRows = jdbcTemplate.update(updateBookedSql, interestedId);

            if (updatedInterestedRows > 0 || updatedBookedRows > 0) {
                return new ApiResponse<>("Interested and booked records reset successfully", null, 200);
            } else {
                return new ApiResponse<>("No records updated", null, 404);
            }
        } catch (DataAccessException e) {
            String errorMessage = "Error resetting interested and booked records: " + Objects.requireNonNullElse(e.getLocalizedMessage(), "Unknown error");
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<PaginatedResponse<List<Booked>>> findByCompanyIdAndWorkspaceId(int companyId, String workspaceId, String searchParam, int page, int pageSize) {
        try {
            String countQuery = "SELECT COUNT(*) FROM booked WHERE company_id = ? AND (workspace_id = ? OR workspace_id IS NULL) and deleted = 0";
            String query = "SELECT * FROM booked WHERE company_id = ? AND (workspace_id = ? OR workspace_id IS NULL) and deleted = 0";

            // Append search condition if provided
            if (searchParam != null && !searchParam.isEmpty()) {
                countQuery += " AND (email LIKE '%" + searchParam + "%' OR first_name LIKE '%" + searchParam + "%' OR last_name LIKE '%" + searchParam + "%' OR name LIKE '%" + searchParam + "%' OR event_name LIKE '%" + searchParam + "%' OR business LIKE '%" + searchParam + "%')";
                query += " AND (email LIKE '%" + searchParam + "%' OR first_name LIKE '%" + searchParam + "%' OR last_name LIKE '%" + searchParam + "%' OR name LIKE '%" + searchParam + "%' OR event_name LIKE '%" + searchParam + "%' OR business LIKE '%" + searchParam + "%')";
            }

            // Get total count
            int totalItems = jdbcTemplate.queryForObject(countQuery, Integer.class, companyId, workspaceId);

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

            // Apply pagination to the query
            String paginatedQuery = query + " LIMIT ? OFFSET ?";
            List<Booked> bookedList = jdbcTemplate.query(paginatedQuery, new Object[]{companyId, workspaceId, pageSize, offset}, new BeanPropertyRowMapper<>(Booked.class));

            // Check if there is a next page
            boolean hasNextPage = page < totalPages;

            // Prepare paginated response
            PaginatedResponse<List<Booked>> paginatedResponse = new PaginatedResponse<>(bookedList, page, pageSize, totalItems, totalPages, hasNextPage);

            if (bookedList.isEmpty()) {
                return new ApiResponse<>("No bookings found for the given company ID and workspace ID", null, 404);
            } else {
                return new ApiResponse<>("Bookings retrieved successfully", paginatedResponse, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Error retrieving bookings by company ID and workspace ID", null, 500);
        }
    }
    public ApiResponse<Void> createManual(Booked booked) {
        try {
            // Parse the meeting date
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);
            Date parsedDate = dateFormat.parse(booked.getMeeting_date());
            Timestamp meetingTimestamp = new Timestamp(parsedDate.getTime());
            String check = "SELECT id FROM interested where lead_email = ? AND (workspace = ? OR workspace IS NULL) AND booked = 0";
            Boolean updateHappen = false;
            Integer interestedId = null;

// Perform the query to get a list of all matching ids
            List<Integer> interestedIds = jdbcTemplate.queryForList(check, Integer.class, booked.getEmail(), booked.getWorkspace_id());

// Check if only one interestedId is obtained
            if (interestedIds.size() == 1) {
                interestedId = interestedIds.get(0);
                String queryToUpdateInterested = "UPDATE interested SET booked = 1 WHERE id = ?";
                jdbcTemplate.update(queryToUpdateInterested, interestedId);
                updateHappen = true;
            } else {
                // Handle the case where more than one result is obtained
                // You can log an error, throw a custom exception, or handle it in any appropriate way
            }
            // Insert new booking
            String insertBookingSql = "INSERT INTO booked (email, name, company_id, workspace_id, event_name, referral, meeting_date, interested_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            int insertedRows = jdbcTemplate.update(insertBookingSql,
                    booked.getEmail(),
                    booked.getName(),
                    booked.getCompany_id(),
                    booked.getWorkspace_id(),
                    booked.getEvent_name(),
                    booked.getReferral(),
                    meetingTimestamp,
                    interestedId
            );

            if (insertedRows > 0) {
                return new ApiResponse<>(updateHappen ? "Booking created and matched successfully" : "Booking created successfully", null, 200);
            } else {
                return new ApiResponse<>("Failed to create booking", null, 400);
            }
        } catch (DataAccessException e) {
            String errorMessage = "Error creating booking: " + Objects.requireNonNullElse(e.getLocalizedMessage(), "Unknown error");
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public ApiResponse<Void> deleteBooked(int bookedId) {
        String updateQuery = "UPDATE booked SET deleted = true WHERE id = ?";

        try {
            int rowsAffected = jdbcTemplate.update(updateQuery, bookedId);
            if (rowsAffected > 0) {
                return new ApiResponse<>("Booked item marked as deleted successfully", null, 200);
            } else {
                return new ApiResponse<>("No booked item found with the given ID", null, 404);
            }
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace(); // You may want to log the exception instead
            return new ApiResponse<>("Failed to mark booked item as deleted: " + e.getMessage(), null, 500);
        }
    }
}