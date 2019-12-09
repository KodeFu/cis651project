package com.example.myapplication;

import com.google.firebase.database.Exclude;

public class Member {
    @Exclude
    public String uid;

    @Exclude
    public String name;

    public Boolean enabled;

    public Member() {}

    public Member(String uid, String name, Boolean enabled) {
        this.uid = uid;
        this.name = name;
        this.enabled = enabled;
    }
}
