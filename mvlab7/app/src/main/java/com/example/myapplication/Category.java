package com.example.myapplication;

import com.google.firebase.database.Exclude;

public class Category {
    @Exclude
    public String name;    // can be a name like "food", "entertainment" or a UID

    public String displayName;

    public Integer limit;  // -1 is unlimited

    public Category() {}

    public Category(String name, String displayName, Integer limit) {
        this.name = name;
        this.displayName = displayName;
        this.limit = limit;
    }
}
