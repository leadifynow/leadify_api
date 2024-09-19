package com.api.leadify.entity;
import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Setter
@Getter
public class DashboardResponse {
public List<clientsResp> clients;
 public List<workspaceResp> worksapces;
 public List<userResp> userWorkspaces;

 @Setter
 @Getter
 public static class clientsResp{
     private Integer id;
     private String clients;
     private boolean favorite;
     private long workspaces;
     private long users;
 };

 @Setter
 @Getter
 public static class workspaceResp{
     private String workspace_id;
     private String client;
     private String name;
     private String description;
     private boolean favorite;
 };

 @Setter
 @Getter
 public static class userResp{
     private Integer id;
     private String name;
 };
}
