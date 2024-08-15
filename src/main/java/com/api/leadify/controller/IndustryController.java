package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.IndustryDao;
import com.api.leadify.entity.Industry;
import com.api.leadify.service.IndustryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/industry")
public class IndustryController {
    private final IndustryDao industryDao;

    @Autowired
    public IndustryController(IndustryDao industryDao) { this.industryDao = industryDao; }


    @PostMapping("/create")
    public ResponseEntity<Industry> createIndustry(@RequestBody Industry industry) {
        return industryDao.createIndustry(industry);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Industry>> getIndustries() {
        List<Industry> industries= industryDao.getIndustries();
        return ResponseEntity.ok(industries);
    }
}
