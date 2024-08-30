package com.api.leadify.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Setter
@Getter
public class CompanyResponse {
 public List<resp> favorites;
 public List<resp> companyList;

    @Setter
    @Getter
    public static class resp{
        private Integer id;
        private String name;
        private String location;
        private String industry;
        private boolean fav;
    };
}
