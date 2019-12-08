package com.example.myapplication;

import java.util.List;

public class Group {
    public String name;                // name of group
    public String token;               // random group id
    public String adminUID;            // UID of admin
    public List<Member> members;       // UIDs of members
    public List<Category> categories;  // Categories and limits
}
