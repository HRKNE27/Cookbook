package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView sendMessageButton;
    private RecyclerView userChatList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private EditText userChatInput;
    private String messageReceiverID, messageReceiverName, messageSenderID, saveCurrentTime, saveCurrentDate;
    private TextView receiverName;
    private CircleImageView receiverProfilePic;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_toolbar, null);
        actionBar.setCustomView(action_bar_view);

        receiverName = (TextView) findViewById(R.id.chat_custom_profile_name);
        receiverProfilePic = (CircleImageView) findViewById(R.id.chat_custom_profile_pic);
        sendMessageButton = (ImageView) findViewById(R.id.chat_send);
        userChatInput = (EditText) findViewById(R.id.chat_input);

        messageReceiverID = getIntent().getExtras().get("visit_uid").toString();
        messageReceiverName = getIntent().getExtras().get("user_name").toString();
        messageSenderID = mAuth.getCurrentUser().getUid();

        messageAdapter = new MessageAdapter(messagesList);
        userChatList = (RecyclerView) findViewById(R.id.chat_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userChatList.setHasFixedSize(true);
        userChatList.setLayoutManager(linearLayoutManager);
        userChatList.setAdapter(messageAdapter);

        DisplayReceiverInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        GetMessages();
    }

    private void GetMessages() {
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if(snapshot.exists()){
                            Messages new_message = snapshot.getValue(Messages.class);
                            messagesList.add(new_message);
                            messageAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendMessage() {
        String messageText = userChatInput.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(Chat.this, "Please enter a message",Toast.LENGTH_SHORT).show();
        }else{
            String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;

            // .push() creates a unique key whenever its used
            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();
            String message_push_id = user_message_key.getKey();

            Calendar getDateInstance = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
            saveCurrentDate = currentDate.format(getDateInstance.getTime());
            Calendar getTimeInstance = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(getTimeInstance.getTime());

            HashMap messageInfo = new HashMap();
            messageInfo.put("Message", messageText);
            messageInfo.put("Time", saveCurrentTime);
            messageInfo.put("Date",saveCurrentDate);
            messageInfo.put("Type","Text");
            messageInfo.put("From",messageSenderID);

            HashMap messageDetails = new HashMap();
            messageDetails.put(message_sender_ref + "/" + message_push_id,messageInfo);
            messageDetails.put(message_receiver_ref + "/" + message_push_id,messageInfo);

            rootRef.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Chat.this,"Message Sent",Toast.LENGTH_SHORT).show();
                        userChatInput.setText("");
                    }else{
                        Toast.makeText(Chat.this,"Error: " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        userChatInput.setText("");
                    }
                }
            });
        }
    }

    private void DisplayReceiverInfo() {
        receiverName.setText(messageReceiverName);
        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    final String profileImage = snapshot.child("ProfileImage").getValue().toString();
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile_placeholder).into(receiverProfilePic);
                    receiverProfilePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent profileIntent = new Intent(Chat.this,UserProfile.class);
                            profileIntent.putExtra("visit_uid",messageReceiverID);
                            startActivity(profileIntent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}