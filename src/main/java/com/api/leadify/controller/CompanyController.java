package com.api.leadify.controller;

import com.api.leadify.dao.CompanyDao;
import com.api.leadify.entity.Company;
import com.api.leadify.entity.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/company")
public class CompanyController {
   private final CompanyDao companyDao;

    @Autowired
    public CompanyController(CompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    @GetMapping("/getCompanies")
    @ResponseBody
    public ResponseEntity<List<Company>> getCompanies() {
        List<Company> companies=companyDao.getCompanies();
        return ResponseEntity.ok(companies);
    }
    @PostMapping("/createCompany")
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        return companyDao.createCompany(company);
    }

    @PutMapping("/updateCompany")
    public ResponseEntity<Company> updateCompany(@RequestBody Company company) {
        return companyDao.updateCompany(company);
    }
    @DeleteMapping("/deleteCompany/{companyId}")
    public ResponseEntity<String> deleteCompany(@PathVariable int companyId) {
        return companyDao.deleteCompany(companyId);
    }
    
    @PutMapping("/favoriteCompany/{companyId}/{status}")
    public ResponseEntity<String> updateFavCompany(@PathVariable Integer companyId,@PathVariable boolean status) {
        return companyDao.updateFavCompany(companyId,status);
    }
}
