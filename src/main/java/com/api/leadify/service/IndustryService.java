package com.api.leadify.service;

import com.api.leadify.dao.ApiResponse;
import com.api.leadify.dao.IndustryDao;
import com.api.leadify.entity.Industry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndustryService {
    /*
     * private final IndustryDao industryDao;

    @Autowired
    public IndustryService(IndustryDao industryDao) { this.industryDao = industryDao; }

    public ApiResponse<Industry> createIndustry(Industry industry) {
        return industryDao.createIndustry(industry);
    }
    public List<Industry> getIndustries() {
        return industryDao.getIndustries();
    }
     */
}
