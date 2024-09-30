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
 private user users;


    @Setter
    @Getter
    public static class resp{
        private String id;
        private String name;
        private String description;
        private String client;
        private Integer client_Id;
        private List<user> users;
        private boolean fav;
    };

    @Setter
    @Getter
    public static class workspace{
        private Integer companyId;
        private String companyName;
        public List<resp> workspaces;
    };

    @Setter
    @Getter
    public static class user{
        private Integer userId;
        private String userName;
        private String idWorkspace;
    };
}
