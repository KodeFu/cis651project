package com.example.myapplication;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Group {
    @Exclude
    public String token;

    public String adminUid;
    public Map<String, Category> categories;
    public String name;
    public Map<String, String> members;

    public Group() {}
}
