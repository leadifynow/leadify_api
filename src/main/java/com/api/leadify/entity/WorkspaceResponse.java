package com.api.leadify.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class WorkspaceResponse {
 public List<resp> favorites;
 public workspace companies;
 

    @Setter
    @Getter
    public static class resp{
        private String id;
        private String name;
        private String description;
        private String client;
        private String users;
        private boolean favorite;
    };

    @Setter
    @Getter
    public static class workspace{
        public List<resp> mediablitz;
        public List<resp> mindful_Agency;
        public List<resp> leadify_Now;
        public List<resp> vincent_Koza;
        private boolean favorite;
    };
}
