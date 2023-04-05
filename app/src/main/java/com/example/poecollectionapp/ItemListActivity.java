package com.example.poecollectionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.example.poecollectionapp.databinding.ActivityItemListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ItemListActivity extends AppCompatActivity {

    //view binding
    private ActivityItemListBinding binding;

    //arraylist
    private ArrayList<ModelItem> itemArrayList;
    //adapter
    private AdaptItem adaptItem;

    private String categoryId, categoryTitle;

    private ProgressBar progressBar;
    private int currentProgress;

    private static final String TAG = "ITEM_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityItemListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");

        //set category
        binding.subTitleTv.setText(categoryTitle);

        loadItemList();


        //edit text change listern, search
        binding.searchBarEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //called as and when user types
                try{
                    adaptItem.getFilter().filter(s);
                }
                catch(Exception e){
                    Log.d(TAG, "onTextChanged: "+e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //handle backBtn click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadItemList(){
        //init array list
        itemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Items");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        itemArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelItem model = ds.getValue(ModelItem.class);
                            //add to list
                            itemArrayList.add(model);

                            Log.d(TAG, "onDataChange: "+ model.getId() + " " + model.getTitle());
                        }
                        //setup adapter
                        adaptItem = new AdaptItem(ItemListActivity.this, itemArrayList);
                        binding.itemsRv.setAdapter(adaptItem);
                        binding.goalTv.setText("Collected items: "  + ""+itemArrayList.size());

                        currentProgress = itemArrayList.size();
                        binding.progressBar.setProgress(itemArrayList.size());
                        binding.progressBar.setMax(15);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}