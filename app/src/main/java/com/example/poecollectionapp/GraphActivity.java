package com.example.poecollectionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.poecollectionapp.databinding.ActivityGraphBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity {

    //view binding
    private ActivityGraphBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    //array list to store categories
    private ArrayList<ModelCategory> categoryArrayList;

    //adapter
    private AdaptCategory adaptCategory;

    //arraylist to store items
    private ArrayList<ModelItem> itemArrayList;
    //adapter
    private AdaptItem adaptItem;

    private static final String TAG = "GRAPH_TAG";

    BarChart barChart;
    ArrayList<BarEntry> barEnrtyArrayList;
    ArrayList<String> labelNames;

    ArrayList<CategoryItemsData> categoryItemsDataArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //set up firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        loadItemList();
        loadCategory();

        //create new object
        barEnrtyArrayList = new ArrayList<>();
        labelNames = new ArrayList<>();

        fillCategoryItems();
       for(int i = 0; i < categoryItemsDataArrayList.size(); i++){
           String category = categoryItemsDataArrayList.get(i).getCategories();
           int items = categoryItemsDataArrayList.get(i).getItems();

           barEnrtyArrayList.add(new BarEntry(i,items));
           labelNames.add(category);
       }

        BarDataSet barDataSet = new BarDataSet(barEnrtyArrayList, "Items in Categories");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        Description description = new Description();
        description.setText("Categories");
        binding.barGraphBc.setDescription(description);
        BarData barData = new BarData(barDataSet);
        binding.barGraphBc.setData(barData);

        //Set xAxis
        XAxis xAxis = new XAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelNames));
        //set position
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labelNames.size());
        xAxis.setLabelRotationAngle(270);
        binding.barGraphBc.animateY(2000);
        binding.barGraphBc.invalidate();

        //handle backBtn click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void loadCategory(){
        //init arraylist
        categoryArrayList = new ArrayList<>();
        //get all categories from firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear array list before adding data
                categoryArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelCategory model = ds.getValue(ModelCategory.class);

                    //add to arraylist
                    categoryArrayList.add(model);
                }
                //setup adapter
                adaptCategory = new AdaptCategory(GraphActivity.this, categoryArrayList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadItemList(){
        //init array list
        itemArrayList = new ArrayList<>();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Items");
        ref.addValueEventListener(new ValueEventListener() {
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
                adaptItem = new AdaptItem(GraphActivity.this, itemArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fillCategoryItems(){
        categoryItemsDataArrayList.clear();
        categoryItemsDataArrayList.add(new CategoryItemsData("Books", 5));
        categoryItemsDataArrayList.add(new CategoryItemsData("Games ", 7));
        categoryItemsDataArrayList.add(new CategoryItemsData("Music", 15));
        categoryItemsDataArrayList.add(new CategoryItemsData("Shoes", 9));
        categoryItemsDataArrayList.add(new CategoryItemsData("Balls", 5));
        categoryItemsDataArrayList.add(new CategoryItemsData("Cars", 7));
    }
}