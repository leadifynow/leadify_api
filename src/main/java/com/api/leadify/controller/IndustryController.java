package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.Industry;
import com.api.leadify.service.IndustryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/industry")
public class IndustryController {
    private final IndustryService industryService;

    @Autowired
    public IndustryController(IndustryService industryService) { this.industryService = industryService; }

    @PostMapping("/create")
    public ApiResponse<Industry> createIndustry(@RequestBody Industry industry) {
        return industryService.createIndustry(industry);
    }

    @GetMapping("/getAll")
    public ApiResponse<List<Industry>> getIndustries() {
        List<Industry> industries = industryService.getIndustries();
        return new ApiResponse<>(null, industries, 200);
    }
}
