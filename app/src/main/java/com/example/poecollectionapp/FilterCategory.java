package com.example.poecollectionapp;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    //arraylist we want to search
    ArrayList<ModelCategory> filterList;
    //adapterin which filter need to be implemented
    AdaptCategory adaptCategory;

    //constructor
    public FilterCategory(ArrayList<ModelCategory> filterList, AdaptCategory adaptCategory) {
        this.filterList = filterList;
        this.adaptCategory = adaptCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if(constraint != null && constraint.length() > 0){
            //change to upper case or lower case to avoid case sensitivty
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                //validate
                if(filterList.get(i).getCategoryName().toUpperCase().contains(constraint)){
                    //add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else{
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply dilter changes
        adaptCategory.categoryArrayList = (ArrayList<ModelCategory>)results.values;

        //notify changes
        adaptCategory.notifyDataSetChanged();
    }
}
