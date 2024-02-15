package com.api.leadify.controller;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.entity.Company;
import com.api.leadify.entity.Workspace;
import com.api.leadify.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/company")
public class CompanyController {
    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) { this.companyService = companyService; }

    @GetMapping("/getCompanies")
    @ResponseBody
    public ApiResponse<List<Company>> getCompanies() {
        List<Company> companies = companyService.getCompanies();
        return new ApiResponse<>(null, companies, 200);
    }

    @PostMapping("/createCompany")
    public ApiResponse<Company> createCompany(@RequestBody Company company) {
        return companyService.createCompany(company);
    }
    @PutMapping("/updateCompany")
    public ApiResponse<Company> updateCompany(@RequestBody Company company) {
        return companyService.updateCompany(company);
    }
    @DeleteMapping("/deleteCompany/{companyId}")
    public ApiResponse<String> deleteCompany(@PathVariable int companyId) {
        return companyService.deleteCompany(companyId);
    }

}
