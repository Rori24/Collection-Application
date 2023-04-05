package com.example.poecollectionapp;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterItem extends Filter {
    //arraylist we want to search
    ArrayList<ModelItem> filterList;
    //adapterin which filter need to be implemented
    AdaptItem adaptItem;

    //constructor
    public FilterItem(ArrayList<ModelItem> filterList, AdaptItem adaptItem) {
        this.filterList = filterList;
        this.adaptItem = adaptItem;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if(constraint != null && constraint.length() > 0){
            //change to upper case or lower case to avoid case sensitivty
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelItem> filteredModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                //validate
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
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
        adaptItem.itemArrayList = (ArrayList<ModelItem>)results.values;

        //notify changes
        adaptItem.notifyDataSetChanged();
    }
}
