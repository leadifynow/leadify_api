package com.api.leadify.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserToken extends User {
    private String token;

}
