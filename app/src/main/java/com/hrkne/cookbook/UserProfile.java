package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {

    private TextView user_profile_name,user_profile_username,user_profile_country,user_profile_status,user_profile_dish,user_profile_cuisine;
    private CircleImageView user_profile_image;
    private Button sendReqButton, declineReqButton;
    private DatabaseReference friendReqRef,userRef, friendsRef, messageRef;
    private FirebaseAuth mAuth;
    private String sendUserID,receiverUserID, currentState, saveCurrentDate;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        receiverUserID = getIntent().getExtras().get("visit_uid").toString();
        sendUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendReqRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        messageRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        mToolbar = (Toolbar) findViewById(R.id.user_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user_profile_name = (TextView) findViewById(R.id.user_profile_name);
        user_profile_username = (TextView) findViewById(R.id.user_profile_username);
        user_profile_country = (TextView) findViewById(R.id.user_profile_country);
        user_profile_status = (TextView) findViewById(R.id.user_profile_status);
        user_profile_dish = (TextView) findViewById(R.id.user_profile_dish);
        user_profile_cuisine = (TextView) findViewById(R.id.user_profile_cuisine);
        user_profile_image = (CircleImageView) findViewById(R.id.user_profile_pic);

        sendReqButton = (Button) findViewById(R.id.user_profile_send_friend_req);
        declineReqButton = (Button) findViewById(R.id.user_profile_decline_friend_req);

        currentState = "Not_Friends";

        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
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

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile_placeholder).into(user_profile_image);
                    user_profile_username.setText("@" + myUsername);
                    user_profile_name.setText(myFullname);
                    user_profile_country.setText("Country: " + myCountry);
                    user_profile_status.setText(myStatus);
                    user_profile_dish.setText("Favorite Dish: " + myFavDish);
                    user_profile_cuisine.setText("Favorite Cuisine: " + myFavCuisine);

                    MaintainButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        declineReqButton.setVisibility(View.INVISIBLE);
        declineReqButton.setEnabled(false);

        if(sendUserID.equals(receiverUserID)){
            sendReqButton.setVisibility(View.INVISIBLE);
            declineReqButton.setVisibility(View.INVISIBLE);
        }else{
            sendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendReqButton.setEnabled(false);
                    if(currentState.equals("Not_Friends")){
                        SendFriendRequest();
                    }
                    if(currentState.equals("Request_Sent")){
                        CancelFriendRequest();
                    }
                    if(currentState.equals("Request_Received")){
                        AcceptFriendRequest();
                    }
                    if(currentState.equals("Friends")){
                        UnfriendUser();
                    }
                }
            });
        }
    }

    private void UnfriendUser() {
        friendsRef.child(sendUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(sendUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendReqButton.setEnabled(true);
                                                currentState = "Not_Friends";
                                                sendReqButton.setText("Send Friend Request");
                                                messageRef.child(sendUserID).child(receiverUserID).removeValue();
                                                messageRef.child(receiverUserID).child(sendUserID).removeValue();
                                                declineReqButton.setVisibility(View.INVISIBLE);
                                                declineReqButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar getDateInstance = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(getDateInstance.getTime());

        friendsRef.child(sendUserID).child(receiverUserID).child("Date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiverUserID).child(sendUserID).child("Date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                // Only called when a friend request is accepted, different from
                                                // CancelFriendRequest()
                                                DeleteFriendRequest();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void DeleteFriendRequest() {
        friendReqRef.child(sendUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendReqRef.child(receiverUserID).child(sendUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendReqButton.setEnabled(true);
                                                currentState = "Friends";
                                                sendReqButton.setText("Unfriend");

                                                declineReqButton.setVisibility(View.INVISIBLE);
                                                declineReqButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        friendReqRef.child(sendUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendReqRef.child(receiverUserID).child(sendUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendReqButton.setEnabled(true);
                                                currentState = "Not_Friends";
                                                sendReqButton.setText("Send Friend Request");

                                                declineReqButton.setVisibility(View.INVISIBLE);
                                                declineReqButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintainButtons() {
        friendReqRef.child(sendUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserID)){
                    String req_type = snapshot.child(receiverUserID).child("RequestType").getValue().toString();
                    if(req_type.equals("Sent")){
                        currentState = "Request_Sent";
                        sendReqButton.setText("Cancel Friend Request");
                        declineReqButton.setVisibility(View.INVISIBLE);
                        declineReqButton.setEnabled(false);
                    }else if(req_type.equals("Received")){
                        currentState = "Request_Received";
                        sendReqButton.setText("Accept Friend Request");
                        declineReqButton.setVisibility(View.VISIBLE);
                        declineReqButton.setEnabled(true);
                        declineReqButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                }else{
                    friendsRef.child(sendUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserID)) {
                                currentState = "Friends";
                                sendReqButton.setText("Unfriend");
                                declineReqButton.setVisibility(View.INVISIBLE);
                                declineReqButton.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendFriendRequest() {
        friendReqRef.child(sendUserID).child(receiverUserID).child("RequestType").setValue("Sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendReqRef.child(receiverUserID).child(sendUserID).child("RequestType").setValue("Received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendReqButton.setEnabled(true);
                                                currentState = "Request_Sent";
                                                sendReqButton.setText("Cancel Friend Request");

                                                declineReqButton.setVisibility(View.INVISIBLE);
                                                declineReqButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendUserToMain(){
        Intent mainIntent = new Intent(UserProfile.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}