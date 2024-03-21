package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.validation.Validator;

import de.hdodenhof.circleimageview.CircleImageView;

public class Reviews extends AppCompatActivity {

    private ImageView reviewButton;
    private EditText reviewInput;
    private RecyclerView reviewList;
    private String PostKey;
    private String currentUID;
    private Toolbar mToolbar;
    private DatabaseReference userRef;
    private DatabaseReference postRef;
    private FirebaseAuth mAuth;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        mToolbar = findViewById(R.id.reviews_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reviews");

        PostKey = getIntent().getExtras().get("ReviewKey").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("Reviews");

        reviewList = (RecyclerView) findViewById(R.id.reviews_list);
        reviewList.setHasFixedSize(true);
        /*LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);*/
        //reviewList.setLayoutManager(linearLayoutManager);
        reviewList.setLayoutManager(new LinearLayoutManager(this));

        reviewButton = (ImageView) findViewById(R.id.reviews_button);
        reviewInput = (EditText) findViewById(R.id.reviews_input);

        score = 0;
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child(currentUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String userName = snapshot.child("Username").getValue().toString();
                            String profilePic = snapshot.child("ProfileImage").getValue().toString();
                            ValidateReview(userName,profilePic);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    protected void onStart(){
        super.onStart();

        FirebaseRecyclerOptions<Reviewers> option = new FirebaseRecyclerOptions.Builder<Reviewers>()
                .setQuery(postRef,Reviewers.class)
                .build();

        FirebaseRecyclerAdapter<Reviewers,ReviewersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Reviewers, ReviewersViewHolder>(option) {
            @Override
            protected void onBindViewHolder(@NonNull ReviewersViewHolder holder, int position, @NonNull Reviewers model) {
                holder.setReviewPic(model.getReviewPic());
                holder.setDate(model.getDate());
                holder.setUsername(model.getUsername());
                holder.setScore(model.getScore());
                holder.setTime(model.getTime());
                holder.setReview(model.getReview());
            }

            @NonNull
            @Override
            public ReviewersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_reviews_layout,parent,false);
                ReviewersViewHolder viewHolder = new ReviewersViewHolder(view);
                return viewHolder;
            }
        };
        firebaseRecyclerAdapter.startListening();
        reviewList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ReviewersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ReviewersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date){
            TextView myDate = (TextView) mView.findViewById(R.id.reviewer_date);
            myDate.setText(date);
        }

        public void setTime(String time){
            TextView myTime = (TextView) mView.findViewById(R.id.reviewer_time);
            myTime.setText(time);
        }
        public void setReview(String review){
            TextView myReview = (TextView) mView.findViewById(R.id.reviewer_text);
            myReview.setText(review);
        }
        public void setScore(String score){
            TextView myScore = (TextView) mView.findViewById(R.id.reviewer_score);
            myScore.setText(score);
        }
        public void setReviewPic(String reviewPic){
            CircleImageView myProfileImage = (CircleImageView) mView.findViewById(R.id.reviewer_profile_pic);
            Picasso.get().load(reviewPic).placeholder(R.drawable.profile_placeholder).into(myProfileImage);
        }

        public void setUsername(String username){
            TextView myUsername = (TextView) mView.findViewById(R.id.reviewer_username);
            myUsername.setText("@" + username + " ");
        }
    }


    private void ValidateReview(String userName,String profilePic) {
        String reviewText = reviewInput.getText().toString();
        if(TextUtils.isEmpty(reviewText)){
            Toast.makeText(Reviews.this, "Please write a review", Toast.LENGTH_SHORT).show();
        }else{
            String[] score_options = {"1","2","3","4","5"};
            AlertDialog.Builder builder = new AlertDialog.Builder(Reviews.this);
            builder.setTitle("What would you like to do?");
            builder.setItems(score_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(score_options[0].equals(score_options[which])){
                        score = 1;
                        SaveReview(userName,profilePic);
                    }else if(score_options[1].equals(score_options[which])){
                        score = 2;
                        SaveReview(userName,profilePic);
                    }else if(score_options[2].equals(score_options[which])){
                        score = 3;
                        SaveReview(userName,profilePic);
                    }else if(score_options[3].equals(score_options[which])){
                        score = 4;
                        SaveReview(userName,profilePic);
                    }else if(score_options[4].equals(score_options[which])){
                        score = 5;
                        SaveReview(userName,profilePic);
                    }
                }
            });
            builder.show();
        }
    }

    private void SaveReview(String userName,String profilePic) {
        String reviewText = reviewInput.getText().toString();
        // We want to create a unique name for the image so we will use date and time
        Calendar getDateInstance = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        final String saveCurrentDate = currentDate.format(getDateInstance.getTime());

        Calendar getTimeInstance = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        final String saveCurrentTime = currentTime.format(getTimeInstance.getTime());

        final String randomKey = currentUID + saveCurrentDate + saveCurrentTime;
        HashMap reviewMap = new HashMap();
        reviewMap.put("UID",currentUID);
        reviewMap.put("Review",reviewText);
        reviewMap.put("Date",saveCurrentDate);
        reviewMap.put("Time",saveCurrentTime);
        reviewMap.put("Username",userName);
        reviewMap.put("ReviewPic",profilePic);
        reviewMap.put("Score",String.valueOf(score));

        postRef.child(randomKey).updateChildren(reviewMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    reviewInput.setText("");
                    Toast.makeText(Reviews.this,"Thank you for the review",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(Reviews.this,"Error occurred, please try again",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}