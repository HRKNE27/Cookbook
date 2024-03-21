package com.hrkne.cookbook;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText status,username,fullname,fav_dish,fav_cuisine,country;
    private Button update_settings_button;
    private CircleImageView profile_image;
    private Uri selectedImageUri;
    private DatabaseReference settingsRef;
    private FirebaseAuth mAuth;
    private String currentUID;
    private StorageReference userProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Gets the references from firebase
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        settingsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        // Sets the toolbar for settings page
        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Gets the views from all elements on the page
        status = (EditText) findViewById(R.id.settings_status);
        username = (EditText) findViewById(R.id.settings_username);
        fullname = (EditText) findViewById(R.id.settings_fullname);
        fav_dish = (EditText) findViewById(R.id.settings_dish);
        fav_cuisine = (EditText) findViewById(R.id.settings_cuisine);
        country = (EditText) findViewById(R.id.settings_country);
        update_settings_button = (Button) findViewById(R.id.update_account_settings);
        profile_image = (CircleImageView) findViewById(R.id.settings_profile_image);

        settingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Presets the current user's current values, and allows them to change it if they need to
                if(snapshot.exists()){
                    String myProfileImage = snapshot.child("ProfileImage").getValue().toString();
                    String myFullname = snapshot.child("FullName").getValue().toString();
                    String myCountry = snapshot.child("Country").getValue().toString();
                    String myStatus = snapshot.child("Status").getValue().toString();
                    String myUsername = snapshot.child("Username").getValue().toString();
                    String myFavDish = snapshot.child("favDish").getValue().toString();
                    String myFavCuisine = snapshot.child("favCuisine").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile_placeholder).into(profile_image);
                    status.setText(myStatus);
                    username.setText(myUsername);
                    fullname.setText(myFullname);
                    country.setText(myCountry);
                    fav_dish.setText(myFavDish);
                    fav_cuisine.setText(myFavCuisine);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Lets user update their settings
        update_settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });

        // Lets user open their gallery and change their profile picture
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
    }

    // Status = Bio
    // Checks to see that each entry is filled with something before letting the user update their bio
    private void ValidateAccountInfo() {
        String new_status = status.getText().toString();
        String new_username = username.getText().toString();
        String new_fullname = fullname.getText().toString();
        String new_country = country.getText().toString();
        String new_fav_dish = fav_dish.getText().toString();
        String new_fav_cuisine = fav_cuisine.getText().toString();

        if(TextUtils.isEmpty(new_status)){
            Toast.makeText(Settings.this,"Please enter your bio",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(new_username)){
            Toast.makeText(Settings.this,"Please enter your username",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(new_fullname)){
            Toast.makeText(Settings.this,"Please enter your full name",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(new_country)){
            Toast.makeText(Settings.this,"Please enter your country",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(new_fav_dish)){
            Toast.makeText(Settings.this,"Please enter your favorite dish",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(new_fav_cuisine)){
            Toast.makeText(Settings.this,"Please enter your cuisine",Toast.LENGTH_SHORT).show();
        } else{
            UpdateAccountInfo(new_status,new_username,new_fullname,new_country,new_fav_dish,new_fav_cuisine);
        }
    }

    // This function updates the newly inputed info into firebase
    private void UpdateAccountInfo(String newStatus, String newUsername, String newFullname, String newCountry, String newFavDish, String newFavCuisine) {
        HashMap userMap = new HashMap();
        userMap.put("Status",newStatus);
        userMap.put("Username",newUsername);
        userMap.put("FullName",newFullname);
        userMap.put("Country",newCountry);
        userMap.put("favDish",newFavDish);
        userMap.put("favCuisine",newFavCuisine);

        settingsRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                // When the information is updated correctly, also store the new profile image to firebase and send to main
                if(task.isSuccessful()){
                    StoreProfileImage();
                    SendUserToMain();
                    Toast.makeText(Settings.this,"Account information updated",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(Settings.this,"Error: " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Helper function to send user to main page
    private void SendUserToMain() {
        Intent mainIntent = new Intent(Settings.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    // Lets user open gallery and choose a picture
    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        launchGalleryActivity.launch(galleryIntent);
    }

    // Gets the uri of the image selected and sets the new profile picture
    ActivityResultLauncher<Intent> launchGalleryActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        // Gets the url of the image from data
                        selectedImageUri = data.getData();
                        profile_image.setImageURI(selectedImageUri);
                    }else{
                        Toast.makeText(Settings.this,"Error: Image can't be picked",Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // This function will store the image to firebase
    private void StoreProfileImage() {
        // Gets the reference to storage in firebase that belongs to user
        StorageReference filePath = userProfileImageRef.child(currentUID+".jpg");
        // Updates the storage with the new image
        filePath.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // This gives Firebase database a reference to the image address in the
                            // Firebase storage so that it can be easily accessed later
                            final String downloadUrl = uri.toString();
                            settingsRef.child("ProfileImage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                // This is used to update the profile picture
                                                Intent selfIntent = new Intent(Settings.this,Settings.class);
                                                startActivity(selfIntent);
                                            }else{
                                                String error_message = task.getException().toString();
                                                Toast.makeText(Settings.this,"Error: "+error_message,Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    });
                }else{
                    String error_message = task.getException().getMessage();
                    Toast.makeText(Settings.this, "Error: " + error_message,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}