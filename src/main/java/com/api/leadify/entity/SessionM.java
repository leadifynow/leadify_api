package com.api.leadify.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SessionM {
    public int idUsuario;
    public String email;
    public String token;
    public int status;
    public Date expiration;
}