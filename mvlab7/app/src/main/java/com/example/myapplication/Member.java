package com.example.myapplication;

import com.google.firebase.database.Exclude;

public class Member {
    @Exclude
    public String uid;

    public String displayName;

    public Member() {}

    public Member(String uid, String displayName) {
        this.uid = uid;
        this.displayName = displayName;
    }
}
