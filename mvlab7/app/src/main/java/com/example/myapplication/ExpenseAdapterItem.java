package com.example.myapplication;

public class ExpenseAdapterItem {
    private String id;
    private String group;
    private String profilePhotoUri;
    private String date;
    private String name;
    private String category;
    private String amount;
    private String description;

    private String receiptPhotoUri;

    public ExpenseAdapterItem(
            String id,
            String group,
            String profilePhotoUri,
            String date,
            String name,
            String category,
            String amount,
            String description,
            String receiptPhotoUri
    ) {
        this.id = id;
        this.group = group;
        this.profilePhotoUri = profilePhotoUri;
        this.date = date;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.receiptPhotoUri = receiptPhotoUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getProfilePhotoUri() {
        return profilePhotoUri;
    }

    public void setProfilePhotoUri(String profilePhotoUri) {
        this.profilePhotoUri = profilePhotoUri;
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

    public String getReceiptPhotoUri() {
        return receiptPhotoUri;
    }

    public void setReceiptPhotoUri(String receiptPhotoUri) {
        this.receiptPhotoUri = receiptPhotoUri;
    }
}
