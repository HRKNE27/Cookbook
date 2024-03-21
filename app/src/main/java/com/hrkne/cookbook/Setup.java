package com.hrkne.cookbook;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Setup extends AppCompatActivity {
    private EditText userName;
    private EditText userFullName;
    private EditText userCountry;
    private Button saveInfoButton;
    private CircleImageView profileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference userProfileImageRef;
    private ProgressBar loadingBar;
    String currentUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        // Gets the reference to the user information given UID from Firebase Database
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        // Gets the profile image associated with the UID from Firebase Storage
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");

        // Gets the elements on the screen
        userName = (EditText) findViewById(R.id.setup_username);
        userFullName = (EditText) findViewById(R.id.setup_fullname);
        userCountry = (EditText) findViewById(R.id.setup_country);
        saveInfoButton = (Button) findViewById(R.id.save_user_information);
        profileImage = (CircleImageView) findViewById(R.id.setup_icon);

        // Button saves the user information that was given in the text fields
        saveInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInfo();
            }
        });

        // When user clicks on the profile image to sort it, opens up phone's gallery and lets user pick
        // Want to add way to crop image, but this is for later
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                launchGalleryActivity.launch(galleryIntent);
            }
        });

        // When a profile image is selected will display on the SetUp page
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("ProfileImage")){
                        // Sets profile image on SetUp page using the Picasso class that was imported
                        String image = snapshot.child("ProfileImage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile_placeholder).into(profileImage);
                    }else{
                        Toast.makeText(Setup.this,"Please select a profile image",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Main function that allows the user to open up their gallery to choose a profile picture
    ActivityResultLauncher<Intent> launchGalleryActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        // Gets the url of the image from data
                        Uri selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap = null;
                        try {
                            // Sets the Bitmap of the image chosen
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(), selectedImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        profileImage.setImageBitmap(selectedImageBitmap);
                        // This variable is just a string that is the current UID which is used to associate
                        // user to their given profile picture
                        StorageReference filePath = userProfileImageRef.child(currentUID+".jpg");
                        // Adds the image to the Firebase storage with UID as a name
                        filePath.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(Setup.this,"Profile Image Stored",Toast.LENGTH_SHORT).show();
                                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            // This gives Firebase database a reference to the image address in the
                                            // Firebase storage so that it can be easily accessed later
                                            final String downloadUrl = uri.toString();
                                            userRef.child("ProfileImage").setValue(downloadUrl)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Intent selfIntent = new Intent(Setup.this,Setup.class);
                                                                startActivity(selfIntent);
                                                                Toast.makeText(Setup.this,"Everything complete",Toast.LENGTH_SHORT).show();
                                                            }else{
                                                                String error_message = task.getException().toString();
                                                                Toast.makeText(Setup.this,"Error: "+error_message,Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                                }
                            }
                        });
                    }else{
                        Toast.makeText(Setup.this,"Error: Image can't be picked",Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // Function stores all the user information into the Firebase Database
    private void SaveAccountSetupInfo() {
        String user_name = userName.getText().toString();
        String full_name = userFullName.getText().toString();
        String country = userCountry.getText().toString();

        // Checks to make sure that everything is filled out
        if(TextUtils.isEmpty(user_name)){
            Toast.makeText(this,"Please enter your user name",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(full_name)){
            Toast.makeText(this,"Please enter your full name",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(country)){
            Toast.makeText(this,"Please enter your country",Toast.LENGTH_SHORT).show();
        }else{
            // Create a HashMap to more easily give information the Firebase Database
            HashMap userMap = new HashMap();
            userMap.put("Username",user_name);
            userMap.put("FullName",full_name);
            userMap.put("Country",country);
            userMap.put("Status","Hello, I am using Cookbook to share my recipes to the world");
            userMap.put("favDish","n/a");
            userMap.put("favCuisine","n/a");

            // This adds the loading circle to the middle of screen while logging in
            // Also prevents user from tapping the screen while signing in
            RelativeLayout layout = findViewById(R.id.main_setup_layout);
            loadingBar = new ProgressBar(Setup.this,null,android.R.attr.progressBarStyleLarge);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.addView(loadingBar,params);
            loadingBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // Updates the children of the UID in FirebaseDatabase
            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(Setup.this,"Your account was created successfully",Toast.LENGTH_SHORT).show();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        loadingBar.setVisibility(View.GONE);
                    }else{
                        String error_message = task.getException().toString();
                        Toast.makeText(Setup.this,"Error Occurred: " + error_message,Toast.LENGTH_SHORT).show();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        loadingBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    // Helper function to send user to Home page from SetUp page
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(Setup.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}