package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

public class ClickPost extends AppCompatActivity {
    private ImageView postImage;
    private TextView postName,postDescription;
    private ImageView editOptions;
    private DatabaseReference clickPostRef;
    private FirebaseStorage storageReference;
    private StorageReference deleteImageRef;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private String postKey;
    private String currentUID;
    private String db_UID;
    private boolean toggle_steps_ingredients;
    private String db_description,db_name,db_image,db_steps,db_ingredients;
    private LinearLayout steps_layout,ingredients_layout;
    private ArrayList<String> array_ingredients,array_steps;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        // Getting references to nodes in Firebase Database
        // postKey = Getting the unique post id from the database, which is sent when user clicks on a certain post
        postKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();

        // Setting the toolbar for back button and title
        mToolbar = (Toolbar) findViewById(R.id.click_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Getting the view for the multiple items in the layout file for clicked post
        postImage = (ImageView) findViewById(R.id.click_post_image);
        postName = (TextView) findViewById(R.id.click_post_name);
        postDescription = (TextView) findViewById(R.id.click_post_description);
        steps_layout = (LinearLayout) findViewById(R.id.click_post_steps);
        ingredients_layout = (LinearLayout) findViewById(R.id.click_post_ingredients);

        // Getting view for the edit option and is initially set to invisible
        // If the user that clicked on post is the creator, allows them to edit the recipe
        editOptions = (ImageView) findViewById(R.id.click_post_edit_options);
        editOptions.setVisibility(View.INVISIBLE);
        editOptions.setEnabled(false);
        toggle_steps_ingredients = false;

        // When there is a change in the database for this node, this will be addValueEventListener will be called
        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    // Get the various elements from the recipe node, and sets it to these values
                    db_description = snapshot.child("Description").getValue().toString();
                    db_name = snapshot.child("Title").getValue().toString();
                    db_image = snapshot.child("RecipeImage").getValue().toString();
                    db_UID = snapshot.child("UID").getValue().toString();
                    db_steps = snapshot.child("Steps").getValue().toString();
                    db_ingredients = snapshot.child("Ingredients").getValue().toString();

                    // Steps and ingredients in database is stored as string where each element
                    // is separated by "####" so need to convert back to an array for use
                    array_steps = convertStringToArray(db_steps);
                    array_ingredients = convertStringToArray(db_ingredients);

                    // Setting the views in layout folder to their respective elements
                    postName.setText(db_name);
                    postDescription.setText(db_description);
                    Picasso.get().load(db_image).placeholder(R.drawable.recipe).into(postImage);

                    // This function essentially refreshes the cards when anything is updated to stay accurate
                    update_cards();

                    // If the user is the recipe creator, they are allowed to edit reciped
                    if(currentUID.equals(db_UID)){
                        editOptions.setVisibility(View.VISIBLE);
                        editOptions.setEnabled(true);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Clicking on editOptions will open menu with various things to edit with the recipe
        editOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog with multiple options and does different things based on what is clicked
                String[] edit_options = {"Edit Recipe Name","Edit Recipe Description","Toggle ingredients and steps","Add Ingredients","Add Steps","Delete Recipe"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ClickPost.this);
                builder.setTitle("What would you like to do?");
                builder.setItems(edit_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(edit_options[0].equals(edit_options[which])){
                            // Opens AlertDialog and allows user to edit the name of the recipe
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(ClickPost.this);
                            builder1.setTitle("Edit Recipe Name");
                            final EditText input1 = new EditText(ClickPost.this);
                            input1.setInputType(InputType.TYPE_CLASS_TEXT);
                            input1.setText(postName.getText().toString());
                            builder1.setView(input1);

                            // When user clicks Ok, it will update the "Title" field for the post with the new
                            // name that user inputted into the AlertDialog
                            builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clickPostRef.child("Title").setValue(input1.getText().toString());
                                    Toast.makeText(ClickPost.this,"Recipe Name Changed",Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Will cancel current AlertDialog with negative button and nothing happens
                            builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder1.show();
                        }else if(edit_options[1].equals(edit_options[which])){
                            // Opens AlertDialog and allows user to edit the description of the recipe
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(ClickPost.this);
                            builder2.setTitle("Edit Recipe Description");
                            final EditText input2 = new EditText(ClickPost.this);
                            input2.setInputType(InputType.TYPE_CLASS_TEXT);
                            input2.setText(postDescription.getText().toString());
                            builder2.setView(input2);

                            // When user clicks Ok, it will update the "Description" field for the post with the new
                            // name that user inputted into the AlertDialog
                            builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clickPostRef.child("Description").setValue(input2.getText().toString());
                                    Toast.makeText(ClickPost.this,"Recipe Description Changed",Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Will cancel current AlertDialog with negative button and nothing happens
                            builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder2.show();
                        }else if(edit_options[2].equals(edit_options[which])){

                            // toggle_steps_ingredients allows user to toggle editing current steps / ingredients
                            // if its false, then we won't see the edit buttons for each card
                            if(toggle_steps_ingredients == false){
                                toggle_steps_ingredients = true;
                            }else{
                                toggle_steps_ingredients = false;
                            }
                            // Updates the cards again if anything was edited
                            update_cards();
                        }else if(edit_options[3].equals(edit_options[which])){
                            // Opens an AlertDialog and allows user to add an ingredient to current recipe
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(ClickPost.this);
                            builder2.setTitle("Add an ingredient");
                            final EditText input2 = new EditText(ClickPost.this);
                            input2.setInputType(InputType.TYPE_CLASS_TEXT);
                            builder2.setView(input2);

                            // Adds the user input to the array, then convert it back to string and then update
                            // firebase database with new string
                            // Since there is a value change, the addValueEventListener will update the cards
                            builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    array_ingredients.add(input2.getText().toString());
                                    update_ingredients();
                                    Toast.makeText(ClickPost.this,"Ingredient Added",Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Cancels the AlertDialog
                            builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder2.show();
                        }else if(edit_options[4].equals(edit_options[which])){
                            // Opens an AlertDialog and allows user to add a step to current recipe
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(ClickPost.this);
                            builder2.setTitle("Add a step");
                            final EditText input2 = new EditText(ClickPost.this);
                            input2.setInputType(InputType.TYPE_CLASS_TEXT);
                            builder2.setView(input2);

                            // Adds the user input to the array, then convert it back to string and then update
                            // firebase database with new string
                            // Since there is a value change, the addValueEventListener will update the cards
                            builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    array_steps.add(input2.getText().toString());
                                    update_steps();
                                    Toast.makeText(ClickPost.this,"Step Added",Toast.LENGTH_SHORT).show();
                                }
                            });

                            // Cancel the AlertDialog
                            builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder2.show();
                        }else if(edit_options[5].equals(edit_options[which])){
                            // Deletes the recipe by removing the node from "Posts" in firebase database
                            deleteCurrentPost();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    // This helper function updates the recycler view if a step/ingredient added, deleted, or edited
    private void update_cards() {
        ingredients_layout.removeAllViews();
        for(String ingredients:array_ingredients){
            addIngredientsCard(ingredients);
        }
        steps_layout.removeAllViews();
        for(String steps:array_steps){
            addStepsCard(steps);
        }
    }

    // This function is called for each element in the steps arraylist. For each element, creates a
    // card for the recycler view and populates it
    private void addStepsCard(String steps) {
        // Essentially get the step_ingredient_card file for a blueprint and fills it with the specific
        // recipe steps and ingredients
        View view = getLayoutInflater().inflate(R.layout.step_ingredient_card,null);
        TextView step_name = view.findViewById(R.id.step_card_name);
        ImageView delete = view.findViewById(R.id.step_card_delete);
        ImageView edit = view.findViewById(R.id.step_card_edit);
        step_name.setText(steps);

        // Deletes the pressed step from the array and removes view, also updates to the firebase
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                steps_layout.removeView(view);
                array_steps.remove(steps);
                update_steps();
            }
        });

        // Allow user to change the pressed step and updates the step to firebase, updates the view
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClickPost.this);
                builder.setTitle("Edit this step");
                final EditText input = new EditText(ClickPost.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(step_name.getText().toString());
                builder.setView(input);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        step_name.setText(input.getText().toString());
                        array_steps.set(array_steps.indexOf(steps),input.getText().toString());
                        update_steps();
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

        // Toggles the edit and delete buttons, based on if the user clicked the option on ALertDialog
        if(toggle_steps_ingredients == false){
            edit.setVisibility(View.INVISIBLE);
            delete.setVisibility(View.INVISIBLE);
        }else{
            edit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }
        steps_layout.addView(view);
    }

    // This function is called for each element in the ingreident arraylist. For each element, creates a
    // card for the recycler view and populates it
    private void addIngredientsCard(String ingredients) {
        // Essentially get the step_ingredient_card file for a blueprint and fills it with the specific
        // recipe steps and ingredients
        View view = getLayoutInflater().inflate(R.layout.step_ingredient_card,null);
        TextView ingredient_name = view.findViewById(R.id.step_card_name);
        ImageView delete = view.findViewById(R.id.step_card_delete);
        ImageView edit = view.findViewById(R.id.step_card_edit);
        ingredient_name.setText(ingredients);

        // Deletes the pressed step from the array and removes view, also updates to the firebase
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingredients_layout.removeView(view);
                array_ingredients.remove(ingredients);
                update_ingredients();
            }
        });

        // Allow user to change the pressed step and updates the step to firebase, updates the view
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClickPost.this);
                builder.setTitle("Edit this ingredient");
                final EditText input = new EditText(ClickPost.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(ingredient_name.getText().toString());
                builder.setView(input);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ingredient_name.setText(input.getText().toString());
                        array_ingredients.set(array_ingredients.indexOf(ingredients),input.getText().toString());
                        update_ingredients();
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
        // Toggles the edit and delete buttons, based on if the user clicked the option on ALertDialog
        if(toggle_steps_ingredients == false){
            edit.setVisibility(View.INVISIBLE);
            delete.setVisibility(View.INVISIBLE);
        }else{
            edit.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }
        ingredients_layout.addView(view);
    }

    // This function gets called everytime an ingredient gets edited, deleted, or updated
    private void update_ingredients() {
        // Creates the string separated by "####"
        StringBuilder ingredientsStr = new StringBuilder();
        for(String i:array_ingredients){
            ingredientsStr.append(i);
            ingredientsStr.append("####");
        }
        // Pushes the new string to firebase
        clickPostRef.child("Ingredients").setValue(ingredientsStr.toString());
    }

    // This function gets called everytime a step gets edited, deleted, or updated
    private void update_steps() {
        // Creates the string separated by "####"
        StringBuilder stepsArr = new StringBuilder();
        for(String i:array_steps){
            stepsArr.append(i);
            stepsArr.append("####");
        }
        // Pushes the new string to firebase
        clickPostRef.child("Steps").setValue(stepsArr.toString());
    }

    // Helper function to convert string back to array
    private ArrayList<String> convertStringToArray(String str) {
        String[] tokens = str.split("####");
        ArrayList<String> new_array = new ArrayList<>(Arrays.asList(tokens));
        return new_array;
    }

    // Deletes the clicked post and associated image from the firebase
    private void deleteCurrentPost() {
        // Get the reference to the image from the firebase storage
        storageReference = FirebaseStorage.getInstance();
        deleteImageRef = storageReference.getReferenceFromUrl(db_image);

        // If the image is deleted successfully from storage, then also delete the post and send back to main page
        deleteImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                clickPostRef.removeValue();
                SendUserToMainActivity();
                Toast.makeText(ClickPost.this,"Post deleted successfully",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper function to send back to main activity
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPost.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}