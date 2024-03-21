package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class Friends extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView friendsList;
    private DatabaseReference friendsRef,usersRef;
    private FirebaseAuth mAuth;
    private String current_UID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mToolbar = findViewById(R.id.friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Friends");

        friendsList = (RecyclerView) findViewById(R.id.friends_list);
        friendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friendsList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        current_UID = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_UID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        DisplayFriends();
    }

    private void DisplayFriends() {
        Query friendsQuery = friendsRef.orderByChild("Date");

        FirebaseRecyclerOptions<FriendsClass> options = new FirebaseRecyclerOptions.Builder<FriendsClass>()
                .setQuery(friendsQuery,FriendsClass.class)
                .build();

        FirebaseRecyclerAdapter<FriendsClass,FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendsClass, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull FriendsClass model) {
                final String userID = getRef(position).getKey();
                usersRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final String username = snapshot.child("FullName").getValue().toString();
                        final String profile_pic = snapshot.child("ProfileImage").getValue().toString();
                        final String status = snapshot.child("Status").getValue().toString();

                        holder.setFullName(username);
                        holder.setStatus(status);
                        holder.setProfileImage(profile_pic,userID);
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(Friends.this,Chat.class);
                                chatIntent.putExtra("visit_uid",userID);
                                chatIntent.putExtra("user_name",username);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_users_display_layout,parent,false);
                Friends.FriendsViewHolder viewHolder = new Friends.FriendsViewHolder(view);
                return viewHolder;
            }
        };

        firebaseRecyclerAdapter.startListening();
        friendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullName(String FullName){
            TextView myFullName = (TextView) mView.findViewById(R.id.search_users_fullname);
            myFullName.setText(FullName);
        }

        public void setStatus(String Status){
            TextView myStatus = (TextView) mView.findViewById(R.id.search_users_status);
            myStatus.setText(Status);
        }

        public void setProfileImage(String ProfileImage, String user_ID){
            CircleImageView myProfileImage = (CircleImageView) mView.findViewById(R.id.search_users_profile_image);
            Picasso.get().load(ProfileImage).placeholder(R.drawable.profile_placeholder).into(myProfileImage);
            myProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(Friends.this,UserProfile.class);
                    profileIntent.putExtra("visit_uid",user_ID);
                    startActivity(profileIntent);
                }
            });
        }
    }
}