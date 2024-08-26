package com.api.leadify.entity;

import lombok.Data;

@Data
public class Report {
    private double leads;
    private double booked;
    private double uniqueEmails;
    private String name;
    private double meets;
}