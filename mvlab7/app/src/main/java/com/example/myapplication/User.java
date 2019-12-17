package com.example.myapplication;

import com.google.firebase.database.Exclude;

public class User {
    @Exclude
    public String uid;

    public String email;

    public String displayName;

    public String group;

    public String profilePhotoUri;

    public User() {}

    public User(String email, String displayName, String group, String profilePhotoUri) {
        this.email = email;
        this.displayName = displayName;
        this.group = group;
        this.profilePhotoUri = profilePhotoUri;
    }
}
