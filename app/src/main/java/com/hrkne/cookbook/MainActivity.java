package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference postRef;
    private DatabaseReference likesRef;
    private CircleImageView navProfileImage;
    private ImageButton addRecipeButton;
    private TextView navProfileUserName;
    String currentUID;
    Boolean likeChecker;
    private recipeAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Getting the current user signed in and their UID
        // Needs to check if the currentUser is null, because when deleting the app and redownloading
        // it, the user and UID will be null causing the app to crash
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            currentUID = mAuth.getCurrentUser().getUid();
        }
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        // Creates the toolbar on top of homepage and named "Cookbook"
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Cookbook");

        // Getting elements on the screen
        addRecipeButton = (ImageButton) findViewById(R.id.add_recipe_button);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);

        // Creates the toggle that pulls up the sidebar with different options
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Sets the user's full name and profile image for the sidebar
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileUserName = (TextView) navView.findViewById(R.id.nav_username);

        // RecyclerView to load all the posts
        postList = findViewById(R.id.all_users_post_list);
        // Allows user to press back button from any page to go back to main page
        postList.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));

        // Checks again to make sure the current user exists
        if(mAuth.getCurrentUser() != null) {

            // addValueEventListener is used here to receive events about data change at a location
            // Takes the current user and their UID, and changes the profile image and name to the
            // one associated with the account
            userRef.child(currentUID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Checks to confirm that the user has a full name
                        if(snapshot.hasChild("FullName")){
                            String full_name = snapshot.child("FullName").getValue().toString();
                            navProfileUserName.setText(full_name);
                        }
                        // Checks to confirm that the user has a profile image
                        if(snapshot.hasChild("ProfileImage")){
                            String image = snapshot.child("ProfileImage").getValue().toString();
                            Picasso.get().load(image).placeholder(R.drawable.profile_placeholder).into(navProfileImage);
                        }else{
                            // If none of these exist, then the profile doesn't exist (shouldn't be happening)
                            Toast.makeText(MainActivity.this,"Profile Does Not Exist",Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            // If there is no current user and UID, then this will send user back to Login page immediately
            SendUserToLogin();
        }

        // When a navigation bar item is selected from the toggle, it will perform a certain action
        // using the UserMenuSelector(item) function
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

        // Button to send user to Post Recipe page
        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostRecipe();
            }
        });
        Query sortPostDescending = postRef.orderByChild("ServerTime");

        // Firebase Recycler view for displaying all the posts on the main page
        FirebaseRecyclerOptions<Recipes> options = new FirebaseRecyclerOptions.Builder<Recipes>()
                .setQuery(sortPostDescending,Recipes.class)
                .build();
        adapter = new recipeAdapter(options);
        postList.setAdapter(adapter);
    }

    // Firebase recycler adapter that sets the values for each post card
    public class recipeAdapter extends FirebaseRecyclerAdapter<Recipes,recipeAdapter.recipeViewHolder>{

        public recipeAdapter(@NonNull FirebaseRecyclerOptions<Recipes> options){
            super(options);
        }

        // This function sets the value for the card by getting the models information
        @Override
        protected void onBindViewHolder(@NonNull recipeViewHolder holder, int position, @NonNull Recipes model) {
            final String postKey = getRef(position).getKey();
            holder.date.setText(model.getDate());
            holder.time.setText(model.getTime());
            holder.fullname.setText(model.getFullname());
            holder.description.setText(model.getDescription());
            holder.title.setText(model.getTitle());
            Picasso.get().load(model.getRecipeImage()).placeholder(R.drawable.image_placeholder).into(holder.recipeimage);
            Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile_placeholder).into(holder.profileimage);

            // Sets the like button
            holder.setLikeButtonStatus(postKey);
            DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference().child("Posts");

            // Set the number of reviews
            reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int reviewCount = (int) snapshot.child(postKey).child("Reviews").getChildrenCount();
                    holder.PostReviewCount.setText(Integer.toString(reviewCount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            holder.profileimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference clickRef = FirebaseDatabase.getInstance().getReference().child("Posts");
                    clickRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String visit_user_id = snapshot.child(postKey).child("UID").getValue().toString();
                                Intent profileIntent = new Intent(MainActivity.this,UserProfile.class);
                                profileIntent.putExtra("visit_uid",visit_user_id);
                                startActivity(profileIntent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            // When the user clicks on the post, opens detailed recipe with steps and ingredients
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Sends intent with the reference of the post
                    // ClickPost.intent will use reference to get the details of recipe and display it
                    Intent clickPostIntent = new Intent(MainActivity.this,ClickPost.class);
                    clickPostIntent.putExtra("PostKey",postKey);
                    startActivity(clickPostIntent);
                }
            });

            // Each node in "Like" node of firebase tracks which user likes which post and stores as true
            holder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If likeChecker == true, then it means that the user hasn't liked the post yet
                    likeChecker = true;
                    likesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(likeChecker.equals(true)){
                                if(snapshot.child(postKey).hasChild(currentUID)){
                                    // This is saying that if the post is already liked, we will remove the like from database
                                    likesRef.child(postKey).child(currentUID).removeValue();
                                    likeChecker = false;
                                    postRef.child(postKey).child(currentUID).removeValue();
                                }else{
                                    // If the post hasn't been liked yet, then if user likes it, will update to firebase
                                    likesRef.child(postKey).child(currentUID).setValue(true);
                                    postRef.child(postKey).child(currentUID).setValue(true);
                                    likeChecker = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            // Sends user to the review section of the recipe
            holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent reviewIntent = new Intent(MainActivity.this,Reviews.class);
                    reviewIntent.putExtra("ReviewKey",postKey);
                    startActivity(reviewIntent);
                }
            });

        }

        // Populates the recycler view with the recipes
        @NonNull
        @Override
        public recipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout,parent,false);
            return new recipeAdapter.recipeViewHolder(view);
        }

        // Blueprint of the recipe that is used to fill the values of the recipe for each post
        class recipeViewHolder extends RecyclerView.ViewHolder{
            TextView date,time,fullname,title,description;
            ImageView recipeimage;
            CircleImageView profileimage;

            ImageView LikePostButton,CommentPostButton;
            TextView PostLikeCount,PostReviewCount;
            int countLikes;
            String currentUserID;
            DatabaseReference userLikesRef;
            public recipeViewHolder(@NonNull View itemView) {
                super(itemView);

                // Gets the views for each element in the recipe card, used later to set the values
                date = itemView.findViewById(R.id.post_date);
                time = itemView.findViewById(R.id.post_time);
                fullname = itemView.findViewById(R.id.post_user_name);
                title = itemView.findViewById(R.id.post_title);
                description = itemView.findViewById(R.id.post_description);
                recipeimage = itemView.findViewById(R.id.post_image);
                profileimage = itemView.findViewById(R.id.post_profile_image);

                LikePostButton = (ImageView) itemView.findViewById(R.id.post_like);
                CommentPostButton = (ImageView) itemView.findViewById(R.id.post_comment);
                PostLikeCount = (TextView) itemView.findViewById(R.id.post_like_count);
                PostReviewCount =(TextView) itemView.findViewById(R.id.post_review_count);

                userLikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
                currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            // Function that changes the like button if it clicked to indicate that it is liked
            public void setLikeButtonStatus(final String PostKeyTemp){
                userLikesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(PostKeyTemp).hasChild(currentUserID)){
                            // If the recipe is liked, then the heart will be filled and likes count updates
                            countLikes = (int) snapshot.child(PostKeyTemp).getChildrenCount();
                            LikePostButton.setImageResource(R.drawable.favorite_clicked);
                            PostLikeCount.setText(Integer.toString(countLikes));
                        }else{
                            // If the recipe is liked, then the heart will be removed and likes count updates
                            countLikes = (int) snapshot.child(PostKeyTemp).getChildrenCount();
                            LikePostButton.setImageResource(R.drawable.favorite);
                            PostLikeCount.setText(Integer.toString(countLikes));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
    }


    // Helper function to send to Post Recipe page from Home page
    private void SendUserToPostRecipe() {
        Intent postIntent = new Intent(MainActivity.this,PostRecipe.class);
        postIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(postIntent);
        finish();
    }

    // More validations to ensure user exists with email,password,username,full name, profile image
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            // If user doesn't have email or password, immediately send to login page
            SendUserToLogin();
        }else{
            // If user has email/password, but no account information, sends to SetUp page
            CheckUserExistence();
        }
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    // If user has email/password, but no account information, sends to SetUp page
    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(current_user_id)){
                    SendUserToSetUp();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Helper function to send to SetUp page from Home page
    private void SendUserToSetUp() {
        Intent setupIntent = new Intent(MainActivity.this,Setup.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    // Helper function to send to Login page from Home page
    private void SendUserToLogin() {
        Intent loginIntent = new Intent(MainActivity.this,Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Sends user to different pages when item in navigation bar is selected
    private void UserMenuSelector(MenuItem item){
        if(item.getItemId() == R.id.nav_home){
            SendUserToMyRecipes();
        }else if(item.getItemId() == R.id.nav_post_recipe){
            SendUserToPostRecipe();
        }else if(item.getItemId() == R.id.nav_profile){
            SendUserToProfile();
        }else if(item.getItemId() == R.id.nav_friends){
            SendUserToFriends();
        }else if(item.getItemId() == R.id.nav_find_user){
            SendUserToSearchUser();
        }else if(item.getItemId() == R.id.nav_settings){
            SendUserToSettings();
        }else if(item.getItemId() == R.id.nav_sign_out){
            mAuth.signOut();
            SendUserToLogin();
        }else if(item.getItemId() == R.id.nav_find_recipes){
            SendUserToSearchRecipes();
        }else if(item.getItemId() == R.id.nav_saved_recipes){
            SendUsersToSavedRecipes();
        }
    }

    private void SendUsersToSavedRecipes() {
        Intent loginIntent = new Intent(MainActivity.this,SavedRecipes.class);
        startActivity(loginIntent);
    }

    private void SendUserToSearchRecipes() {
        Intent loginIntent = new Intent(MainActivity.this,FindRecipes.class);
        startActivity(loginIntent);
    }

    private void SendUserToMyRecipes() {
        Intent loginIntent = new Intent(MainActivity.this,MyRecipes.class);
        startActivity(loginIntent);
    }

    private void SendUserToFriends() {
        Intent loginIntent = new Intent(MainActivity.this,Friends.class);
        startActivity(loginIntent);
    }

    // Helper function to send user to search section
    private void SendUserToSearchUser() {
        Intent loginIntent = new Intent(MainActivity.this,FindUsers.class);
        startActivity(loginIntent);
    }

    // Helper function to send user to profile section
    private void SendUserToProfile() {
        Intent loginIntent = new Intent(MainActivity.this,Profile.class);
        startActivity(loginIntent);
    }

    //Helper function to send user to setting section
    private void SendUserToSettings() {
        Intent loginIntent = new Intent(MainActivity.this,Settings.class);
        startActivity(loginIntent);
    }

    // Linear layout manager class that allows user to press back button from any page back to home page
    public class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }
}