package com.example.poecollectionapp;

public class ModelCategory {

    String id, categoryName, categoryGoal, uid;
    long timestamp;

    //constructor empty required for firebase
    public ModelCategory() {
    }

    //parametrized constructor
    public ModelCategory(String id, String categoryName, String categoryGoal, String uid, long timestamp) {
        this.id = id;
        this.categoryName = categoryName;
        this.categoryGoal = categoryGoal;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    /*Getter&Setter*/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryGoal() {
        return categoryGoal;
    }

    public void setCategoryGoal(String categoryGoal) {
        this.categoryGoal = categoryGoal;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
