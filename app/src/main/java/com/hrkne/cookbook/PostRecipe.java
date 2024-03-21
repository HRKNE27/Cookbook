package com.hrkne.cookbook;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class PostRecipe extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView select_post_image;
    private Button post_recipe_button;
    private EditText post_recipe_title;
    private EditText post_recipe_desc;
    private Uri selectedImageUri;
    private String recipe_title;
    private String recipe_description;
    private String saveCurrentDate;
    private String saveCurrentTime;
    private String postRandomName;
    private String downloadURL;
    private FirebaseAuth mAuth;
    private StorageReference PostImageRef;
    private DatabaseReference UserRef;
    private DatabaseReference PostRef;
    private String currentUID;

    //For adding steps and ingredients
    private Button add_steps_button;
    private Button add_ingredients_button;
    private LinearLayout add_steps_layout;
    private LinearLayout add_ingredients_layout;

    private ArrayList<String> stepsList;
    private ArrayList<String> ingredientsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_recipe);

        // Get the reference to post image, user, and post
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        PostImageRef = FirebaseStorage.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        // Sets the toolbar for the post recipe page
        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post Recipe");

        // Gets the view for each element on the page
        select_post_image = (ImageView) findViewById(R.id.post_recipe_image);
        post_recipe_title = (EditText) findViewById(R.id.post_recipe_title);
        post_recipe_desc = (EditText) findViewById(R.id.post_recipe_description);
        post_recipe_button = (Button) findViewById(R.id.post_recipe_button);
        add_steps_button = (Button) findViewById(R.id.post_add_step_button);
        add_ingredients_button = (Button) findViewById(R.id.post_add_ingredient_button);
        add_steps_layout = findViewById(R.id.steps_container);
        add_ingredients_layout = findViewById(R.id.ingredients_container);

        // Sets the array list that stores each step and ingredients which will be converted to string later
        stepsList = new ArrayList<String>();
        ingredientsList = new ArrayList<String>();

        // Opens the gallery and lets user pick the image from their gallery to use for their recipe
        select_post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        // Saves the recipe information and sends it the firebase database
        post_recipe_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

        // Lets the user add a step to the recipe
        add_steps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opens alert dialog and asks user to input a step
                AlertDialog.Builder builder = new AlertDialog.Builder(PostRecipe.this);
                builder.setTitle("Add steps in order");
                final EditText input = new EditText(PostRecipe.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // When user adds a step, adds a card that displays step and allows user to edit
                        addStepCard(input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        // Lets user add an ingredient to the recipe
        add_ingredients_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Opens an AlertDialog and lets user input ingredient
                AlertDialog.Builder builder = new AlertDialog.Builder(PostRecipe.this);
                builder.setTitle("Add ingredients");
                final EditText input = new EditText(PostRecipe.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    // Adds a ingredient card that displays ingredient and allows user to edit it
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addIngredientsCard(input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    // This function populates the scrollview for the ingredients by adding a card
    private void addIngredientsCard(String toString) {
        View view = getLayoutInflater().inflate(R.layout.step_ingredient_card,null);
        TextView ingredient_name = view.findViewById(R.id.step_card_name);
        ImageView delete = view.findViewById(R.id.step_card_delete);
        ImageView edit = view.findViewById(R.id.step_card_edit);
        ingredient_name.setText(toString);
        ingredientsList.add(toString);
        // Sets the delete button
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Deletes the card and removes it from arraylist
                add_ingredients_layout.removeView(view);
                ingredientsList.remove(toString);
            }
        });
        // Sets the edit button
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opens AlertDialog and lets user edit the ingredient
                AlertDialog.Builder builder = new AlertDialog.Builder(PostRecipe.this);
                builder.setTitle("Edit this ingredient");
                final EditText input = new EditText(PostRecipe.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(ingredient_name.getText().toString());
                builder.setView(input);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ingredient_name.setText(input.getText().toString());
                        ingredientsList.set(ingredientsList.indexOf(toString),input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        add_ingredients_layout.addView(view);
    }

    // This function populates the scrollview for the steps by adding a card
    private void addStepCard(String toString) {
        View view = getLayoutInflater().inflate(R.layout.step_ingredient_card,null);
        TextView step_name = view.findViewById(R.id.step_card_name);
        ImageView delete = view.findViewById(R.id.step_card_delete);
        ImageView edit = view.findViewById(R.id.step_card_edit);
        step_name.setText(toString);
        stepsList.add(toString);
        // Sets the delete button
        delete.setOnClickListener(new View.OnClickListener() {
            //  Removes the card and deletes it from the arraylist
            @Override
            public void onClick(View v) {
                add_steps_layout.removeView(view);
                stepsList.remove(toString);
            }
        });
        // Sets the edit button
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Opens the AlertDialog and lets user edit the step
                AlertDialog.Builder builder = new AlertDialog.Builder(PostRecipe.this);
                builder.setTitle("Edit this step");
                final EditText input = new EditText(PostRecipe.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(step_name.getText().toString());
                builder.setView(input);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        step_name.setText(input.getText().toString());
                        stepsList.set(stepsList.indexOf(toString),input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        add_steps_layout.addView(view);
    }

    // Function checks to see that everything is filled before posting it the the firebase
    private void ValidatePostInfo() {
        recipe_title = post_recipe_title.getText().toString();
        recipe_description = post_recipe_desc.getText().toString();
        if(selectedImageUri == null){
            Toast.makeText(PostRecipe.this,"Please select an image to use for the recipe",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(recipe_title)){
            Toast.makeText(PostRecipe.this,"Please enter a title for the recipe",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(recipe_description)){
            Toast.makeText(PostRecipe.this,"Please enter a description for the recipe",Toast.LENGTH_SHORT).show();
        }else if(stepsList.isEmpty()){
            Toast.makeText(PostRecipe.this,"Please enter steps for the recipe",Toast.LENGTH_SHORT).show();
        }else if(ingredientsList.isEmpty()){
            Toast.makeText(PostRecipe.this,"Please enter ingredients for the recipe",Toast.LENGTH_SHORT).show();
        } else{
            StoreImageToFirebaseStorage();
        }
    }

    // Function to store the image to firebase
    private void StoreImageToFirebaseStorage() {
        // We want to create a unique name for the image so we will use date and time
        Calendar getDateInstance = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(getDateInstance.getTime());
        Calendar getTimeInstance = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(getTimeInstance.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        // Sends the image to firebase storage and stored with unique name
        StorageReference filePath = PostImageRef.child("Recipe Images").child(selectedImageUri.getLastPathSegment() + postRandomName + ".jpg");
        filePath.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener(){
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        // If the image is stored correctly, then we can store the rest of the unformation
                        @Override
                        public void onSuccess(Uri uri) {
                            // Need to call SavePostInformation() here otherwise, downloadURL becomes null again
                            // and doesn't store the image path
                            downloadURL = uri.toString();
                            SavePostInformation();
                        }
                    });
                    Toast.makeText(PostRecipe.this,"Image uploaded successfully",Toast.LENGTH_SHORT).show();
                }else{
                    String error_message = task.getException().getMessage();
                    Toast.makeText(PostRecipe.this, "Error: " + error_message,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Function to store the information of the post, called only if the image is stored successfully
    private void SavePostInformation() {
        UserRef.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String fullName = snapshot.child("FullName").getValue().toString();
                    String userProfileImage = snapshot.child("ProfileImage").getValue().toString();

                    // Stores each element in string into one single string that is separated by "####"
                    StringBuilder stepsStr = new StringBuilder();
                    for(String i:stepsList){
                        stepsStr.append(i);
                        stepsStr.append("####");
                    }
                    StringBuilder ingredientsStr = new StringBuilder();
                    for(String j:ingredientsList){
                        ingredientsStr.append(j);
                        ingredientsStr.append("####");
                    }

                    // Store information into hashmap before putting into firebase
                    HashMap postMap = new HashMap();
                    postMap.put("UID",currentUID);
                    postMap.put("Date",saveCurrentDate);
                    postMap.put("Time",saveCurrentTime);
                    postMap.put("Title",recipe_title);
                    postMap.put("Description",recipe_description);
                    postMap.put("RecipeImage",downloadURL);
                    postMap.put("ProfileImage",userProfileImage);
                    postMap.put("Fullname",fullName);
                    postMap.put("Steps",stepsStr.toString());
                    postMap.put("ServerTime", -1*new Date().getTime());
                    postMap.put("Ingredients",ingredientsStr.toString());

                    // Each recipe is stored with the currentUID and unique postRandomName which is the time/date posted
                    PostRef.child(currentUID + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                stepsList.clear();
                                ingredientsList.clear();
                                SendUserToMainActivity();
                                Toast.makeText(PostRecipe.this,"New recipe added successfully",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(PostRecipe.this,"Error occurred while creating new recipe",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Opens the user's gallery
    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        launchGalleryActivity.launch(galleryIntent);
    }

    // Main function that allows the user to open up their gallery to choose a recipe image
    ActivityResultLauncher<Intent> launchGalleryActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        // Gets the url of the image from data
                        selectedImageUri = data.getData();
                        select_post_image.setImageURI(selectedImageUri);
                    }else{
                        Toast.makeText(PostRecipe.this,"Error: Image can't be picked",Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // Puts a back button on the toolbar that allows user to go back to home
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper function to send user back to the home page
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostRecipe.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}