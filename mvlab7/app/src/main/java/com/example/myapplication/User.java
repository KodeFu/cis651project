package com.example.myapplication;

import com.google.firebase.database.Exclude;

public class User {
    @Exclude
    public String uid;

    public String displayName;

    public String email;

    public String group;

    public User() {}

    public User(String displayName, String email, String group) {
        this.displayName = displayName;
        this.email = email;
        this.group = group;
    }
}
