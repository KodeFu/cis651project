package com.example.myapplication;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Spending {
    @Exclude
    public String token;

    public Map<String, Map<String, MonthlyReceipts> > receipts;

    public Spending() {}
}
