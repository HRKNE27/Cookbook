package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private TextView profile_name,profile_username,profile_country,profile_status,profile_dish,profile_cuisine;
    private CircleImageView profile_image;
    private DatabaseReference userRef, friendsRef, postRef;
    private FirebaseAuth mAuth;
    private String currentUID;
    private Toolbar mToolbar;

    private Button myPosts,myFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get references from Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        // Set toolbar for profile page
        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the views for all elements on the profile page
        profile_name = (TextView) findViewById(R.id.my_profile_name);
        profile_username = (TextView) findViewById(R.id.my_profile_username);
        profile_country = (TextView) findViewById(R.id.my_profile_country);
        profile_status = (TextView) findViewById(R.id.my_profile_status);
        profile_dish = (TextView) findViewById(R.id.my_profile_dish);
        profile_cuisine = (TextView) findViewById(R.id.my_profile_cuisine);
        profile_image = (CircleImageView) findViewById(R.id.my_profile_pic);
        myFriends = (Button) findViewById(R.id.my_profile_friends);
        myPosts = (Button) findViewById(R.id.my_profile_posts);

        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyRecipes();
            }
        });

        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToFriends();
            }
        });

        friendsRef.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int num_friends = (int) snapshot.getChildrenCount();
                    if(num_friends == 1){
                        myFriends.setText(Integer.toString(num_friends) + " Friend");
                    }else {
                        myFriends.setText(Integer.toString(num_friends) + " Friends");
                    }
                }else{
                    myFriends.setText("0 Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postRef.orderByChild("UID").startAt(currentUID).endAt(currentUID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            int num_posts = (int) snapshot.getChildrenCount();
                            if(num_posts == 1){
                                myPosts.setText(Integer.toString(num_posts) + " Post");
                            }else{
                                myPosts.setText(Integer.toString(num_posts) + " Posts");
                            }
                        }else{
                            myPosts.setText("0 Posts");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        userRef.addValueEventListener(new ValueEventListener() {
            // Gets the information from firebase database and sets the text/profile picture for profile page
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String myProfileImage = snapshot.child("ProfileImage").getValue().toString();
                    String myFullname = snapshot.child("FullName").getValue().toString();
                    String myCountry = snapshot.child("Country").getValue().toString();
                    String myStatus = snapshot.child("Status").getValue().toString();
                    String myUsername = snapshot.child("Username").getValue().toString();
                    String myFavDish = snapshot.child("favDish").getValue().toString();
                    String myFavCuisine = snapshot.child("favCuisine").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile_placeholder).into(profile_image);
                    profile_username.setText("@" + myUsername);
                    profile_name.setText(myFullname);
                    profile_country.setText("Country: " + myCountry);
                    profile_status.setText(myStatus);
                    profile_dish.setText("Favorite Dish: " + myFavDish);
                    profile_cuisine.setText("Favorite Cuisine: " + myFavCuisine);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendUserToFriends() {
        Intent loginIntent = new Intent(Profile.this,Friends.class);
        startActivity(loginIntent);
    }

    private void SendUserToMyRecipes() {
        Intent loginIntent = new Intent(Profile.this,MyRecipes.class);
        startActivity(loginIntent);
    }

    // Helper function to send back to main page
    private void SendUserToMain(){
        Intent mainIntent = new Intent(Profile.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}