package com.example.poecollectionapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.poecollectionapp.databinding.ActivityAddItemBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class AddItemActivity extends AppCompatActivity {

    //view binding
    private ActivityAddItemBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressdialog;

    //array list to hold categories
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private Uri imageUri = null;

   //Tag for debugging
    private static final String TAG = "ADD_IMAGE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadItemCategories();

        //set up progress dialog
        progressdialog = new ProgressDialog(this);
        progressdialog.setTitle("Please wait");
        progressdialog.setCanceledOnTouchOutside(false);

        //handle backBtn click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle addImageBtn click, upload image
        binding.addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageAttachMenu();
            }
        });

        //hancle click, choose category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        //handle submitBtn click, add item
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data
                validateData();
            }
        });
    }

    private String title = "", description = "";
    private void validateData(){
        Log.d(TAG, "validateData: validating data...");

        //get data
        title = binding.itemNameEt.getText().toString().trim();
        description = binding.itemDescriptionEt.getText().toString().trim();

        //validate data
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Please enter item title...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Please enter item description...", Toast.LENGTH_SHORT).show();
            }
        else if(TextUtils.isEmpty(sCategoryTitle)) {
            Toast.makeText(this, "Please select item category...", Toast.LENGTH_SHORT).show();
        }
        else if(imageUri==null) {
            Toast.makeText(this, "Please upload image...", Toast.LENGTH_SHORT).show();
        }
        else{
            uploadToStorage();
        }

    }

    private void uploadToStorage(){
        //upload to firebase storage
        Log.d(TAG, "uploadToStorage: Uploading to storage...");

        //show progress
        progressdialog.setMessage("Uploading item...");
        progressdialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();

        //item path in firebase
        String filePathAndName = "Items/" + timestamp;

        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Item uploaded to storage...!");
                        Log.d(TAG, "onSuccess: Getting item url");

                        //get item url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String uploadedItemUrl = ""+uriTask.getResult();

                        //upload to firebase db
                        uploadedItemInfo(uploadedItemUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //upload failed
                        progressdialog.dismiss();
                        Log.d(TAG, "onFailure: Item upload failed due to "+e.getMessage());
                        Toast.makeText(AddItemActivity.this, "Item upload failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadedItemInfo(String uploadedItemUrl, long timestamp){
        //upload item info to firebase db
        Log.d(TAG, "uploadedItemInfo: Uploading item info into database");

        progressdialog.setMessage("Uploading item info...");

        String uid = firebaseAuth.getUid();

        //setup data to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+sCategoryId);
        hashMap.put("url", ""+uploadedItemUrl);
        hashMap.put("timestamp", timestamp);

        //db reference
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Items");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressdialog.dismiss();
                        Log.d(TAG, "onSuccess: Successfully added to database...");
                        Toast.makeText(AddItemActivity.this, "Successfully added to database", Toast.LENGTH_SHORT).show();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressdialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to database due to "+e.getMessage());
                        Toast.makeText(AddItemActivity.this, "Failed to upload to database due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadItemCategories(){
        Log.d(TAG, "loadItemCategories: Loading item categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //db ref to load categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear(); //clear before adding data
                categoryIdArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){

                    //get category id and title
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("categoryName").getValue();

                    //add to respective arraylist
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //selected category id and title
    private String sCategoryId, sCategoryTitle;
    private void categoryPickDialog(){
        //get categories
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        //get string array of categories
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for(int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item click, get clicked item dialog
                        sCategoryTitle = categoryTitleArrayList.get(which);
                        sCategoryId = categoryIdArrayList.get(which);
                        //set to category textview
                        binding.categoryTv.setText(sCategoryTitle);

                        Log.d(TAG, "onClick: Selected Category: "+sCategoryId+" "+sCategoryTitle);
                    }
                })
                .show();
    }

    private void showImageAttachMenu(){
        //init setup popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.addImageBtn);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Gallery");

        popupMenu.show();

        //handle menu items click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //get id of clicked item
                int which = item.getItemId();
                if(which==0){
                    //camera clicked
                    pickImageCamera();
                }
                else if(which==1){
                    //gallery click
                    pickImageGallery();
                }

                return false;
            }
        });
    }

    private void pickImageCamera(){
        //intent to pick image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pick"); //iimage title
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery(){
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of camera intent
                    //get uri of image
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Picked From Camera "+imageUri);
                        Intent data = result.getData();
                    }
                    else{
                        Toast.makeText(AddItemActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of gallery intent
                    //get uri of image
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: "+imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked From Gallery "+imageUri);
                    }
                    else{
                        Toast.makeText(AddItemActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

}