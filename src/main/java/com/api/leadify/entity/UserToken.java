package com.api.leadify.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class UserToken extends User {
    private String token;
    private List<Paths> pathsList;
}
