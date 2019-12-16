package com.example.myapplication;

import com.google.firebase.database.Exclude;

public class Receipt {
    @Exclude
    public String id;
    @Exclude
    public String group;

    public Double amount;
    public String category;
    public Long date;
    public String description;
    public String receipt;
    public String userUid;

    public Receipt() {}
}
