package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SavedRecipes extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView recipeList;
    private FirebaseAuth mAuth;
    private DatabaseReference postRef, usersRef, likesRef;
    private String currentUID;
    Boolean likeCheck = true;
    int countLikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_recipes);

        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();

        mToolbar = (Toolbar) findViewById(R.id.saved_recipes_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Saved Recipes");

        recipeList = (RecyclerView) findViewById(R.id.saved_recipes_layout);
        recipeList.setHasFixedSize(true);
        recipeList.setLayoutManager(new LinearLayoutManager(this));
        DisplaySavedPosts();
    }

    private void DisplaySavedPosts() {
        Query myPostQuery = postRef.orderByChild(currentUID).equalTo(true);
        FirebaseRecyclerOptions<Recipes> options = new FirebaseRecyclerOptions.Builder<Recipes>()
                .setQuery(myPostQuery,Recipes.class)
                .build();

        FirebaseRecyclerAdapter<Recipes,RecipesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Recipes, RecipesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecipesViewHolder holder, int position, @NonNull Recipes model) {
                final String postKey = getRef(position).getKey();
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
                holder.setTitle(model.getTitle());
                holder.setDescription(model.getDescription());
                holder.setFullname(model.getFullname());
                holder.setProfileImg(model.getProfileimage(),postKey);
                holder.setImage(model.getRecipeImage());
                holder.setLikeButtonStatus(postKey);

                // When the user clicks on the post, opens detailed recipe with steps and ingredients
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Sends intent with the reference of the post
                        // ClickPost.intent will use reference to get the details of recipe and display it
                        Intent clickPostIntent = new Intent(SavedRecipes.this,ClickPost.class);
                        clickPostIntent.putExtra("PostKey",postKey);
                        startActivity(clickPostIntent);
                    }
                });

                DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference().child("Posts");

                // Set the number of reviews
                reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int reviewCount = (int) snapshot.child(postKey).child("Reviews").getChildrenCount();
                        holder.reviewCount.setText(Integer.toString(reviewCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // Sends user to the review section of the recipe
                holder.reviewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent reviewIntent = new Intent(SavedRecipes.this,Reviews.class);
                        reviewIntent.putExtra("ReviewKey",postKey);
                        startActivity(reviewIntent);
                    }
                });

                holder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If likeChecker == true, then it means that the user hasn't liked the post yet
                        likeCheck = true;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(likeCheck.equals(true)){
                                    if(snapshot.child(postKey).hasChild(currentUID)){
                                        // This is saying that if the post is already liked, we will remove the like from database
                                        likesRef.child(postKey).child(currentUID).removeValue();
                                        postRef.child(postKey).child(currentUID).removeValue();
                                        likeCheck = false;
                                    }else{
                                        // If the post hasn't been liked yet, then if user likes it, will update to firebase
                                        likesRef.child(postKey).child(currentUID).setValue(true);
                                        postRef.child(postKey).child(currentUID).setValue(true);
                                        likeCheck = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public RecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout,parent,false);
                SavedRecipes.RecipesViewHolder viewHolder = new SavedRecipes.RecipesViewHolder(view);
                return viewHolder;
            }
        };
        firebaseRecyclerAdapter.startListening();
        recipeList.setAdapter(firebaseRecyclerAdapter);
    }

    public class RecipesViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView reviewCount, likeCount;
        ImageView reviewButton, likeButton;
        public RecipesViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            reviewCount = (TextView) itemView.findViewById(R.id.post_review_count);
            likeCount = (TextView) itemView.findViewById(R.id.post_like_count);
            reviewButton = (ImageView) itemView.findViewById(R.id.post_comment);
            likeButton = (ImageView) itemView.findViewById(R.id.post_like);
        }
        public void setDate(String date){
            TextView recipe_date = (TextView) mView.findViewById(R.id.post_date);
            recipe_date.setText(date);
        }
        public void setTime(String time){
            TextView recipe_time = (TextView) mView.findViewById(R.id.post_time);
            recipe_time.setText(time);
        }
        public void setDescription(String description){
            TextView recipe_descr = (TextView) mView.findViewById(R.id.post_description);
            recipe_descr.setText(description);
        }
        public void setTitle(String title){
            TextView recipe_title = (TextView) mView.findViewById(R.id.post_title);
            recipe_title.setText(title);
        }
        public void setFullname(String fullname){
            TextView recipe_date = (TextView) mView.findViewById(R.id.post_user_name);
            recipe_date.setText(fullname);
        }
        public void setImage(String img){
            ImageView recipe_image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(img).placeholder(R.drawable.image_placeholder).into(recipe_image);
        }
        public void setProfileImg(String img,String postKey){
            CircleImageView profile_img = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(img).placeholder(R.drawable.profile_placeholder).into(profile_img);
            profile_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference clickRef = FirebaseDatabase.getInstance().getReference().child("Posts");
                    clickRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String visit_user_id = snapshot.child(postKey).child("UID").getValue().toString();
                                Intent profileIntent = new Intent(SavedRecipes.this,UserProfile.class);
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
        }

        public void setLikeButtonStatus(final String PostKeyTemp){
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(PostKeyTemp).hasChild(currentUID)){
                        // If the recipe is liked, then the heart will be filled and likes count updates
                        countLikes = (int) snapshot.child(PostKeyTemp).getChildrenCount();
                        likeButton.setImageResource(R.drawable.favorite_clicked);
                        likeCount.setText(Integer.toString(countLikes));
                    }else{
                        // If the recipe is liked, then the heart will be removed and likes count updates
                        countLikes = (int) snapshot.child(PostKeyTemp).getChildrenCount();
                        likeButton.setImageResource(R.drawable.favorite);
                        likeCount.setText(Integer.toString(countLikes));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}