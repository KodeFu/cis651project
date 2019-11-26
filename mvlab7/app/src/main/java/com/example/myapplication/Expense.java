package com.example.myapplication;

public class Expense {
    private int profilePhoto;
    private String date;
    private String name;
    private String category;
    private String amount;
    private String description;

    public Expense(
            int profilePhoto,
            String date,
            String name,
            String category,
            String amount,
            String description
    ) {
        this.profilePhoto = profilePhoto;
        this.date = date;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    public int getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(int profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
