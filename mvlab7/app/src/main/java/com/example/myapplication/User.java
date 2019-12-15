package com.example.myapplication;

import com.google.firebase.database.Exclude;

public class User {
    @Exclude
    public String uid;

    public String displayName;

    public String email;

    public String group;

    public String profilePhotoUri;

    public User() {}

    public User(String displayName, String email, String group, String profilePhotoUri) {
        this.displayName = displayName;
        this.email = email;
        this.group = group;
        this.profilePhotoUri = profilePhotoUri;
    }
}
