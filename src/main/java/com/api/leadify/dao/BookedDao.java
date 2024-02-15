package com.api.leadify.dao;

import com.api.leadify.entity.Booked;
import com.api.leadify.entity.Interested;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Objects;

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

            // Check if the email exists in the interested table
            String interestedIdQuery = "SELECT id FROM interested WHERE lead_email = ?";
            Integer interestedId;
            try {
                interestedId = jdbcTemplate.queryForObject(interestedIdQuery, Integer.class, email);
                String updateInterestedSql = "UPDATE interested SET booked = 1 WHERE id = ?";
                jdbcTemplate.update(updateInterestedSql, interestedId);
            } catch (EmptyResultDataAccessException e) {
                interestedId = null;
            }

            // Inserting basic booking information into the database
            String sql = "INSERT INTO booked (email, first_name, last_name, name, text_reminder_number, timezone, interested_id, company_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            Integer finalInterestedId = interestedId;
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
                }
            }

            return new ApiResponse<>("Booked created successfully", null, 201);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>("Error creating booked", null, 500);
        }
    }
    public ApiResponse<List<Booked>> getAllBookedByCompanyId(int companyId) {
        try {
            String sql = "SELECT * FROM booked WHERE company_id = ?";
            List<Booked> bookedList = jdbcTemplate.query(sql, new Object[]{companyId}, new BeanPropertyRowMapper<>(Booked.class));
            if (bookedList.isEmpty()) {
                return new ApiResponse<>("No bookings found for the given company ID", null, 404);
            } else {
                return new ApiResponse<>("Bookings retrieved successfully", bookedList, 200);
            }
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving bookings by company ID. Details: " + e.getLocalizedMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<List<Booked>> searchBookedRecords(String searchTerm, int companyId) {
        try {
            String sql = "SELECT i.id, i.event_type, i.workspace, i.campaign_id, i.campaign_name, i.lead_email as email, i.title, i.email, " +
                    "i.website, i.industry, i.lastName, i.firstName, i.number_of_employees, i.companyName, i.linkedin_url, i.stage_id, i.notes, 0 as booked " +
                    "FROM interested i " +
                    "JOIN workspace w ON i.workspace = w.id " +
                    "JOIN company c ON w.company_id = c.id " +
                    "WHERE (i.id = ? OR i.lead_email LIKE ? OR i.firstName LIKE ? OR i.lastName LIKE ? OR i.notes LIKE ?) AND c.id = ? AND i.booked = 0";
            String searchTermLike = "%" + searchTerm + "%";
            List<Booked> bookedList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Booked.class), searchTerm, searchTermLike, searchTermLike, searchTermLike, searchTermLike, companyId);
            return new ApiResponse<>("Booked records retrieved successfully", bookedList, 200);
        } catch (DataAccessException e) {
            String errorMessage = "Error retrieving booked records: " + e.getLocalizedMessage();
            e.printStackTrace();
            return new ApiResponse<>(errorMessage, null, 500);
        }
    }
    public ApiResponse<Void> updateBookedAndInterested(int interestedId, int bookedId) {
        try {
            // Update interested_id in the booked table
            String updateBookedSql = "UPDATE booked SET interested_id = ? WHERE id = ?";
            int updatedBookedRows = jdbcTemplate.update(updateBookedSql, interestedId, bookedId);

            // Update booked column to 1 in the interested table
            String updateInterestedSql = "UPDATE interested SET booked = 1 WHERE id = ?";
            int updatedInterestedRows = jdbcTemplate.update(updateInterestedSql, interestedId);

            if (updatedBookedRows > 0 && updatedInterestedRows > 0) {
                return new ApiResponse<>("Booked and interested records updated successfully", null, 200);
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

}