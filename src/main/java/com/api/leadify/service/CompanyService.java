package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.CompanyDao;
import com.api.leadify.entity.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
    private final CompanyDao companyDao;

    @Autowired
    public CompanyService(CompanyDao companyDao) {
        this.companyDao = companyDao;
    }
    public List<Company> getCompanies() {
        return companyDao.getCompanies();
    }
    public ApiResponse<Company> createCompany(Company company) {
        return companyDao.createCompany(company);
    }
    public ApiResponse<Company> updateCompany(Company company) {
        return companyDao.updateCompany(company);
    }
    public ApiResponse<String> deleteCompany(int companyId) {
        return companyDao.deleteCompany(companyId);
    }
}
