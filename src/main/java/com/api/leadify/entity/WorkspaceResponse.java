package com.api.leadify.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class WorkspaceResponse {
 public List<resp> favorites;
 public List<workspace> companies;
 private resp response;


    @Setter
    @Getter
    public static class resp{
        private String id;
        private String name;
        private String description;
        private String client;
        private String users;
        private Integer companyId;
        private boolean fav;
    };

    @Setter
    @Getter
    public static class workspace{
        private Integer id;
        private String name;
        public List<resp> workspaces;
    };
}
