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

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    public ResponseEntity<ApiResponse<?>> createBooked(Booked booked, int companyId) {
        log.info("create booked");
        try {
            JsonNode payloadNode = booked.getPayload();
            log.info("Payload: {}", payloadNode);

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

            log.info("Extracted information: email={}, firstName={}, lastName={}, name={}, textReminderNumber={}, timezone={}, event_name={}, formattedMeetingDateTime={}, publicist={}",
                    email, firstName, lastName, name, textReminderNumber, timezone, event_name, formattedMeetingDateTime, publicist);

            UUID workspaceId = null;
            String sql;
            switch (event_name) {
                case "Mindful Agency - Strategy Consultation":
                    workspaceId = fetchWorkspaceId("Mindful Agency - Lauren");
                    break;
                case "Mindful Agency - Strategy PR Consultation":
                case "Mindful Agency - Connect":
                    workspaceId = fetchWorkspaceId("Mindful Agency - Natalie");
                    break;
                case "MediaBlitz - Discovery Call":
                    workspaceId = fetchWorkspaceId("Media Blitz - Michael");
                    break;
                case "Mindful Agency - Initial Consultation":
                    workspaceId = fetchWorkspaceId("Mindful Agency - Instagram");
                    break;
                case "Leadify - Discovery Call":
                    workspaceId = fetchWorkspaceId("Leadify - Chelsea");
                    break;
                case "Priority 1 Meeting":
                    workspaceId = fetchWorkspaceId("Royal Logistics - Vincent/Rachel");
                    break;
                case "Whizzbang Media - Discovery Call":
                    workspaceId = fetchWorkspaceId("Whizzbang Media - Olivia");
                    break;
                case "Mindful Agency - Initial PR Consultation":
                    workspaceId = fetchWorkspaceId("Mindful Agency - LinkedIn");
                    break;
                case "Mindful Agency - Discovery Call":
                case "Mindful Agency - Follow-Up Call":
                case "Mindful Agency - Lead Generation Consultation":
                case "Leadify - Consultation Call":
                    workspaceId = null;
                    log.info("No workspace needed for event '{}'", event_name);
                    break;
                default:
                    log.warn("Invalid event name: {}", event_name);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>("Invalid event name", null, 400));
            }

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
                    log.info("Interested ID: {}, Workspace ID: {}", interestedId, workspaceId);
                }
            } catch (EmptyResultDataAccessException e) {
                log.warn("No interested entry found for email: {}", email);
                interestedId = null;
            }

            // Inserting basic booking information into the database
            sql = "INSERT INTO booked (email, first_name, last_name, name, text_reminder_number, timezone, interested_id, company_id, workspace_id, event_name, publicist, meeting_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            Integer finalInterestedId = interestedId;
            UUID finalWorkspaceId = workspaceId;
            String finalPublicist = publicist;
            String finalSql = sql;
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
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
                    ps.setNull(9, Types.VARCHAR);
                }
                ps.setString(10, event_name);
                ps.setString(11, finalPublicist);
                ps.setString(12, formattedMeetingDateTime);
                return ps;
            }, keyHolder);

            // Retrieve the auto-generated key (booked_id)
            int bookedId = keyHolder.getKey().intValue();
            log.info("Booked ID: {}", bookedId);

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

                    // Update specific fields based on questions
                    switch (question) {
                        case "Please provide your business name.":
                            updateBookedField(bookedId, "business", answer);
                            break;
                        case "How did you hear about Mindful Agency?":
                            updateBookedField(bookedId, "referral", answer);
                            break;
                        case "Please provide your website":
                            updateBookedField(bookedId, "website", answer);
                            break;
                        default:
                            // Handle other questions if necessary
                            break;
                    }
                }
            }

            // Return success response
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Booked created successfully", null, 201));
        } catch (Exception e) {
            log.error("An error occurred: {}", e.getMessage(), e);
            JsonNode payloadNode = booked.getPayload();
            String errorMessage = e.getMessage();
            String insertQuery = "INSERT INTO error_log (error_message, data) VALUES (?, ?)";
            jdbcTemplate.update(insertQuery, errorMessage, payloadNode.toString());
            ApiResponse<?> errorResponse = new ApiResponse<>("An error occurred: " + errorMessage, null, 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    private UUID fetchWorkspaceId(String workspaceName) {
        String sql = "SELECT id FROM workspace WHERE name = ?";
        try {
            UUID workspaceId = jdbcTemplate.queryForObject(sql, UUID.class, workspaceName);
            log.info("Workspace ID for '{}': {}", workspaceName, workspaceId);
            return workspaceId;
        } catch (EmptyResultDataAccessException e) {
            log.warn("No workspace found for the given name '{}'.", workspaceName);
        } catch (DataAccessException e) {
            log.error("DataAccessException while fetching workspace '{}': ", workspaceName, e);
        }
        return null;
    }

    /**
     * Helper method to update a specific field in the 'booked' table.
     */
    private void updateBookedField(int bookedId, String fieldName, String value) {
        String updateSql = "UPDATE booked SET " + fieldName + " = ? WHERE id = ?";
        jdbcTemplate.update(updateSql, value, bookedId);
        log.info("Updated {} column for booked ID: {}", fieldName, bookedId);
    }
    public ResponseEntity<PaginatedResponse<List<Booked>>> getAllBookedByCompanyId(int companyId, String workspaceId, int page, int pageSize, int filterType, String startDate, String endDate) {
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No bookings found for the given company ID and workspace ID", null, 404);
            } else {
                return ResponseEntity.ok(paginatedResponse);
                //return new ApiResponse<>("Bookings retrieved successfully", paginatedResponse, 200);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ResponseEntity<Page<Booked>> getBooked(
            Integer companyId, String workspaceId, Pageable pageable, String match,
            String startDate, String endDate, String filterBy, String sortBy) {
        try {
            int page = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();
            int offset = page * pageSize;

            // Initialize base SQL and parameters
            StringBuilder baseSql = new StringBuilder();
            MapSqlParameterSource params = new MapSqlParameterSource();

            // Initialize NamedParameterJdbcTemplate once
            NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            // Handle filterBy logic
            if ("company".equalsIgnoreCase(filterBy)) {
                // Retrieve company_id from workspace table if filterBy is company
                String companySql = "SELECT company_id FROM workspace WHERE id = :workspaceId";
                Integer retrievedCompanyId = namedJdbcTemplate.queryForObject(companySql,
                        new MapSqlParameterSource("workspaceId", workspaceId), Integer.class);

                if (retrievedCompanyId == null) {
                    throw new IllegalArgumentException("No company found for the given workspaceId.");
                }

                // Apply the company filter
                baseSql.append(" FROM booked WHERE company_id = :companyId AND deleted = 0");
                params.addValue("companyId", retrievedCompanyId);

            } else if ("workspace".equalsIgnoreCase(filterBy)) {
                // Apply the workspace filter directly
                baseSql.append(" FROM booked WHERE workspace_id = :workspaceId AND deleted = 0");
                params.addValue("workspaceId", workspaceId);
            } else {
                throw new IllegalArgumentException("Invalid filterBy value. It must be either 'company' or 'workspace'.");
            }

            // Add filter conditions based on match parameter
            if ("Yes".equalsIgnoreCase(match)) {
                baseSql.append(" AND interested_id IS NOT NULL");
            } else if ("No".equalsIgnoreCase(match)) {
                baseSql.append(" AND interested_id IS NULL");
            } else if (!"All".equalsIgnoreCase(match)) {
                throw new IllegalArgumentException("Invalid match value. It must be 'All', 'Yes', or 'No'.");
            }

            // Time zone adjustment
            ZoneId userZoneId = ZoneId.of("America/Monterrey");

            // Add filter conditions for startDate and endDate if they are provided
            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                try {
                    // Parse dates in user's time zone
                    LocalDate startLocalDate = LocalDate.parse(startDate);
                    LocalDate endLocalDate = LocalDate.parse(endDate);

                    // Start of startDate at 00:00:00 in user's time zone
                    LocalDateTime startOfStartDate = startLocalDate.atStartOfDay();
                    // End of endDate at 23:59:59.999999999 in user's time zone
                    LocalDateTime endOfEndDate = endLocalDate.atTime(LocalTime.MAX);

                    // Convert to UTC
                    Instant startInstantUtc = startOfStartDate.atZone(userZoneId).toInstant();
                    Instant endInstantUtc = endOfEndDate.atZone(userZoneId).toInstant();

                    // Convert to Timestamp
                    Timestamp startTimestamp = Timestamp.from(startInstantUtc);
                    Timestamp endTimestamp = Timestamp.from(endInstantUtc);

                    baseSql.append(" AND created_at BETWEEN :startDate AND :endDate");
                    params.addValue("startDate", startTimestamp);
                    params.addValue("endDate", endTimestamp);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid date format for startDate or endDate. Expected format is yyyy-MM-dd.");
                }
            }

            // Determine the ORDER BY clause based on sortBy parameter
            String orderByClause;
            if ("Newest".equalsIgnoreCase(sortBy)) {
                orderByClause = " ORDER BY created_at DESC";
            } else if ("Oldest".equalsIgnoreCase(sortBy)) {
                orderByClause = " ORDER BY created_at ASC";
            } else if ("Last Updated".equalsIgnoreCase(sortBy)) {
                orderByClause = " ORDER BY updated_at DESC";
            } else {
                // Default to "Newest" if no valid sortBy is provided
                orderByClause = " ORDER BY created_at DESC";
            }

            // Query to count total bookings
            String countSql = "SELECT COUNT(*)" + baseSql.toString();
            int totalItems = namedJdbcTemplate.queryForObject(countSql, params, Integer.class);

            // Construct SQL query for retrieving paginated bookings
            String dataSql = "SELECT *" + baseSql.toString() + orderByClause + " LIMIT :limit OFFSET :offset";
            params.addValue("limit", pageSize);
            params.addValue("offset", offset);

            // Retrieve paginated bookings based on the constructed SQL query
            List<Booked> bookedList = namedJdbcTemplate.query(dataSql, params, new BeanPropertyRowMapper<>(Booked.class));

            // Create Page instance
            Page<Booked> bookedPage = new PageImpl<>(bookedList, pageable, totalItems);

            // Always return the Page object, even if it's empty
            return ResponseEntity.ok(bookedPage);

        } catch (IllegalArgumentException e) {
            // Return 400 Bad Request for invalid input
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            // Return 500 Internal Server Error for other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    public ResponseEntity<Page<Booked>> SearchAllBooked(
         String workspaceId, Pageable pageable, String Search) {
    try {
        int page = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int offset = page * pageSize;

        // Initialize base SQL and parameters
        StringBuilder baseSql = new StringBuilder();
        MapSqlParameterSource params = new MapSqlParameterSource();

        //Search companyId form workspace Id
        String companySql = "SELECT company_id FROM workspace WHERE id = :workspaceId";
        NamedParameterJdbcTemplate JdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        Integer retrievedCompanyId = JdbcTemplate.queryForObject(companySql,
                new MapSqlParameterSource("workspaceId", workspaceId), Integer.class);

        if (retrievedCompanyId == null) {
            throw new IllegalArgumentException("No company found for the given workspaceId.");
        }

        // Apply the company filter
        baseSql.append(" FROM booked WHERE company_id = :companyId AND deleted = 0");
        params.addValue("companyId", retrievedCompanyId);

        if(Search!=null && !Search.trim().isEmpty()){
            try {
                Long searchId=Long.parseLong(Search);
                baseSql.append(" AND CAST(id as char) LIKE :searchId ");
                params.addValue("searchId", Search+ "%");
            } catch (Exception e) {
                baseSql.append(" AND email LIKE :searchEmail ");
                params.addValue("searchEmail", "%"+ Search + "%");
            }
        }

        // Query to count total bookings
        String countSql = "SELECT COUNT(*)" + baseSql.toString();
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        int totalItems = namedJdbcTemplate.queryForObject(countSql, params, Integer.class);

        // Construct SQL query for retrieving paginated bookings
        String dataSql = "SELECT *" + baseSql.toString() +  " LIMIT :limit OFFSET :offset";
        params.addValue("limit", pageSize);
        params.addValue("offset", offset);

        // Retrieve paginated bookings based on the constructed SQL query
        List<Booked> bookedList = namedJdbcTemplate.query(dataSql, params, new BeanPropertyRowMapper<>(Booked.class));

        // Create Page instance
        Page<Booked> bookedPage = new PageImpl<>(bookedList, pageable, totalItems);

        if (bookedList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            return ResponseEntity.ok(bookedPage);
        }
    } catch (IllegalArgumentException e) {
        // Return 400 Bad Request for invalid input
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    } catch (Exception e) {
        e.printStackTrace();
        // Return 500 Internal Server Error for other exceptions
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
    
    public ResponseEntity<List<Booked>> searchBookedRecords(String searchTerm, int companyId, String workspace) {
        try {
            String sql = "SELECT i.id, i.event_type AS event_name, i.workspace AS workspace_id, i.campaign_id AS campaign_id, i.campaign_name AS campaign_name, i.lead_email AS email, i.title, i.email AS second_email, " +
                    "i.website, i.industry, i.lastName AS last_name, i.firstName AS first_name, i.number_of_employees AS business, i.companyName AS name, i.linkedin_url, i.stage_id, i.notes, i.created_at, 0 as booked " + // added i.created_at
                    "FROM interested i " +
                    "JOIN workspace w ON i.workspace = w.id " +
                    "JOIN company c ON w.company_id = c.id " +
                    "WHERE (i.id = ? OR i.lead_email LIKE ? OR i.firstName LIKE ? OR (CASE WHEN ? LIKE '% %' THEN i.lastName LIKE ? ELSE i.lastName LIKE ? END) OR i.notes LIKE ?) AND c.id = ? AND i.workspace = ?";
            String searchTermLike = "%" + searchTerm + "%";
            String secondWordLike = searchTerm.split(" ").length == 2 ? "%" + searchTerm.split(" ")[1] + "%" : searchTermLike;
            List<Booked> bookedList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booked.class), searchTerm, searchTermLike, searchTermLike, searchTerm, secondWordLike, searchTermLike, searchTermLike, companyId, workspace);
            return ResponseEntity.ok(bookedList);
            //return new ApiResponse<>("Booked records retrieved successfully", bookedList, 200);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ResponseEntity<List<Booked>> searchInterestedToMatch(String searchTerm, String workspace) {
        try {
            // First, retrieve the company_id based on the workspace
            String companySql = "SELECT company_id FROM workspace WHERE id = ?";
            Integer companyId = jdbcTemplate.queryForObject(companySql, new Object[]{workspace}, Integer.class);

            if (companyId == null) {
                throw new IllegalArgumentException("No company found for the given workspace.");
            }

            // Prepare the main SQL query
            String sql = "SELECT i.id, i.event_type AS event_name, i.workspace AS workspace_id, i.campaign_id AS campaign_id, " +
                    "i.campaign_name AS campaign_name, i.lead_email AS email, i.title, i.email AS second_email, i.website, " +
                    "i.industry, i.lastName AS last_name, i.firstName AS first_name, i.number_of_employees AS business, " +
                    "i.companyName AS name, i.linkedin_url, i.stage_id, i.notes, i.created_at, 0 as booked " +
                    "FROM interested i " +
                    "JOIN workspace w ON i.workspace = w.id " +
                    "JOIN company c ON w.company_id = c.id " +
                    "WHERE (i.id = ? OR i.lead_email LIKE ? OR i.firstName LIKE ? " +
                    "OR (CASE WHEN ? LIKE '% %' THEN i.lastName LIKE ? ELSE i.lastName LIKE ? END) " +
                    "OR i.notes LIKE ?) AND c.id = ? AND i.workspace = ?";

            // Prepare the parameters
            String searchTermLike = "%" + searchTerm + "%";
            String[] searchTerms = searchTerm.split(" ");
            String secondWordLike = searchTerms.length == 2 ? "%" + searchTerms[1] + "%" : searchTermLike;

            // Now pass all the necessary parameters
            List<Booked> bookedList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booked.class),
                    // Placeholder 1
                    searchTerm,
                    // Placeholder 2
                    searchTermLike,
                    // Placeholder 3
                    searchTermLike,
                    // Placeholder 4
                    searchTerm,
                    // Placeholder 5
                    secondWordLike,
                    // Placeholder 6
                    searchTermLike,
                    // Placeholder 7 (missing parameter added)
                    searchTermLike,
                    // Placeholder 8
                    companyId,
                    // Placeholder 9
                    workspace
            );

            return ResponseEntity.ok(bookedList);

        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    public ResponseEntity<Booked> updateBookedAndInterested(int interestedId, int bookedId) {
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
                    return ResponseEntity.ok(updatedBooked);
                    //return new ApiResponse<>("Booked and interested records updated successfully", updatedBooked, 200);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    //return new ApiResponse<>("Failed to retrieve updated booked record", null, 500);
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No records updated", null, 404);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    public ResponseEntity<Interested> getInterestedByBookedId(int bookedId) {
        try {
            String sql = "SELECT id FROM interested WHERE id = (SELECT interested_id FROM booked WHERE id = ?)";
            Integer interestedId = jdbcTemplate.queryForObject(sql, Integer.class, bookedId);

            if (interestedId != null) {
                String interestedSql = "SELECT * FROM interested WHERE id = ?";
                Interested interested = jdbcTemplate.queryForObject(interestedSql, new BeanPropertyRowMapper<>(Interested.class), interestedId);
                return ResponseEntity.ok(interested);
                //return new ApiResponse<>("Interested record retrieved successfully", interested, 200);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No interested record found for the given booked id", null, 404);
            }
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
           // return new ApiResponse<>("No interested record found for the given booked id", null, 404);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    public ResponseEntity<Void> resetInterestedAndBooked(int interestedId) {
        try {
            // Reset booked = 0 in the interested table
            String updateInterestedSql = "UPDATE interested SET booked = 0 WHERE id = ?";
            int updatedInterestedRows = jdbcTemplate.update(updateInterestedSql, interestedId);

            // Reset interested_id to null in the booked table
            String updateBookedSql = "UPDATE booked SET interested_id = NULL, workspace_id = NULL WHERE interested_id = ?";
            int updatedBookedRows = jdbcTemplate.update(updateBookedSql, interestedId);

            if (updatedInterestedRows > 0 || updatedBookedRows > 0) {
                return ResponseEntity.ok().build();
                //return new ApiResponse<>("Interested and booked records reset successfully", null, 200);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
               // return new ApiResponse<>("No records updated", null, 404);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    public ResponseEntity<PaginatedResponse<List<Booked>>> findByCompanyIdAndWorkspaceId(int companyId, String workspaceId, String searchParam, int page, int pageSize) {
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No bookings found for the given company ID and workspace ID", null, 404);
            } else {
                return ResponseEntity.ok(paginatedResponse);
                //return new ApiResponse<>("Bookings retrieved successfully", paginatedResponse, 200);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Error retrieving bookings by company ID and workspace ID", null, 500);
        }
    }
    public ResponseEntity<Booked> createManual(Booked booked) {
        try {
            // Parse the meeting date
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);
            Date parsedDate = dateFormat.parse(booked.getMeeting_date());
            Timestamp meetingTimestamp = new Timestamp(parsedDate.getTime());

            // Fetch the company name based on company_id
            String fetchCompanyNameSql = "SELECT name FROM company WHERE id = ?";
            String companyName = jdbcTemplate.queryForObject(fetchCompanyNameSql, String.class, booked.getCompany_id());

            if (companyName == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("Company not found", null, 404);
            }

            // Construct the event name
            String eventName = companyName + " - Call";

            String check = "SELECT id FROM interested WHERE lead_email = ? AND (workspace = ? OR workspace IS NULL) AND booked = 0";
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
            String insertBookingSql = "INSERT INTO booked (email, name, company_id, workspace_id, event_name, referral, meeting_date, interested_id, publicist, business) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int insertedRows = jdbcTemplate.update(insertBookingSql,
                    booked.getEmail(),
                    booked.getName(),
                    booked.getCompany_id(),
                    booked.getWorkspace_id(),
                    eventName,  // Use the constructed event name
                    booked.getReferral(),
                    meetingTimestamp,
                    interestedId,
                    booked.getPublicist(),
                    booked.getBusiness()
            );

            if (insertedRows > 0) {
                String getBookedIdQuery = "SELECT LAST_INSERT_ID()";
                int bookedId = jdbcTemplate.queryForObject(getBookedIdQuery, Integer.class);
                String fetchNewBookedQuery = "select * from booked where id=?";
                Booked newInterested = jdbcTemplate.queryForObject(fetchNewBookedQuery, new BeanPropertyRowMapper<>(Booked.class), bookedId);

                return ResponseEntity.ok(newInterested);
                //return new ApiResponse<>(updateHappen ? "Booking created and matched successfully" : "Booking created successfully", null, 200);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("Failed to create booking", null, 400);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<Void> deleteBooked(int bookedId) {
        String updateQuery = "UPDATE booked SET deleted = true WHERE id = ?";

        try {
            int rowsAffected = jdbcTemplate.update(updateQuery, bookedId);
            if (rowsAffected > 0) {
                return ResponseEntity.ok().build();
                //return new ApiResponse<>("Booked item marked as deleted successfully", null, 200);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                //return new ApiResponse<>("No booked item found with the given ID", null, 404);
            }
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace(); // You may want to log the exception instead
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            //return new ApiResponse<>("Failed to mark booked item as deleted: " + e.getMessage(), null, 500);
        }
    }

    public ResponseEntity<Booked> updateBooked(Booked booked) {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);
            Date parsedDate = dateFormat.parse(booked.getMeeting_date());
            Timestamp meetingTimestamp = new Timestamp(parsedDate.getTime());

            String sql = "update booked set email=?, meeting_date=?, publicist=?, name=?, business=?, website=? where id=?;";
            int affectedRows = jdbcTemplate.update(
                    sql,
                    booked.getEmail(),
                    meetingTimestamp,
                    booked.getPublicist(),
                    booked.getName(),
                    booked.getBusiness(),
                    booked.getWebsite(),
                    booked.getId()
            );

            if (affectedRows > 0) {
                String fetchNewBookedQuery = "select * from booked where id=?";
                Booked updatedBook = jdbcTemplate.queryForObject(fetchNewBookedQuery, new BeanPropertyRowMapper<>(Booked.class), booked.getId());
                return ResponseEntity.ok(updatedBook);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}