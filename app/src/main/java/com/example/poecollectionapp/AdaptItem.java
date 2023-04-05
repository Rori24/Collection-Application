package com.example.poecollectionapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.poecollectionapp.databinding.RowItemBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdaptItem extends RecyclerView.Adapter<AdaptItem.HolderItem> implements Filterable {
    //context
    private Context context;

    //array list
    public ArrayList<ModelItem> itemArrayList, filterList;

    private FilterItem filter;

    //view binding
    private RowItemBinding binding;

    private static final String TAG = "ITEM_ADAPTER_TAG";

    //constructor
    public AdaptItem(Context context, ArrayList<ModelItem> itemArrayList) {
        this.context = context;
        this.itemArrayList = itemArrayList;
        this.filterList = itemArrayList;
    }

    @NonNull
    @Override
    public HolderItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind layout
        binding = RowItemBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderItem(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderItem holder, int position) {
        /*get and set data clicks*/

        //get data
        ModelItem model = itemArrayList.get(position);
        String title = model.getTitle();
        String description = model.getDescription();
        long timestamp = model.getTimestamp();

        // convert timestamp
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);
        Glide.with(context).load(itemArrayList.get(position).getUrl()).into(holder.imageIv);

        //functions
        loadCategory(model, holder);
        loadSize(model, holder);


        //handle deleteBtn, delete category
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //begin delete
                                Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show();
                                deleteItem(model, holder);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

    }

    private void deleteItem(ModelItem model, HolderItem holder){
        //get category id
        String id = model.getId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Items");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //deleted successfully
                        Toast.makeText(context, "Item deleted...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to delete
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCategory(ModelItem model, HolderItem holder){
        //get category using categoryId
        String categoryId = model.getCategoryId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("categoryName").getValue();

                        //set to text
                        holder.categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadSize(ModelItem model, HolderItem holder){
        //using url get image and metadata
        String itemUrl = model.getUrl();

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(itemUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        //get size in bytes
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+model.getTitle() +" "+ bytes);

                        //convert bytes to KB, MB
                        double kb = bytes/1024;
                        double mb = kb/1024;

                        if(mb >= 1){
                            holder.sizeTv.setText(String.format("%.2f",mb)+" MB");
                        }
                        else if(kb >= 1){
                            holder.sizeTv.setText(String.format("%.2f",kb)+" KB");
                        }
                        else{
                            holder.sizeTv.setText(String.format("%.2f",bytes)+" bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed getting metadata
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return itemArrayList.size(); //return number of records|list size
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterItem(filterList, this);
        }
        return filter;
    }

    /*view holder class for row_item.xml*/
    class HolderItem extends RecyclerView.ViewHolder{

        //UI views
        ImageView imageIv;
        ImageButton deleteBtn;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;

        public HolderItem(@NonNull View itemView) {
            super(itemView);

            //init ui views
            imageIv = binding.imageIv;
            deleteBtn = binding.deleteBtn;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
        }
    }
}
