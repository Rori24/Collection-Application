package com.example.poecollectionapp;

public class CategoryItemsData {

    String categories;
    int items;

    public CategoryItemsData(String categories, int items) {
        this.categories = categories;
        this.items = items;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public int getItems() {
        return items;
    }

    public void setItems(int items) {
        this.items = items;
    }
}
