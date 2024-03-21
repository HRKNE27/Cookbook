package com.hrkne.cookbook;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

    public MessageAdapter(List<Messages> userMessageList){
        this.userMessageList = userMessageList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView SenderMessageText, ReceiverMessageText;
        public CircleImageView ReceiverProfileImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            ReceiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            ReceiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_users,parent,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String profile_image = snapshot.child("ProfileImage").getValue().toString();
                    Picasso.get().load(profile_image).placeholder(R.drawable.profile_placeholder).into(holder.ReceiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(fromMessageType.equals("Text")){
            holder.ReceiverMessageText.setVisibility(View.INVISIBLE);
            holder.ReceiverProfileImage.setVisibility(View.INVISIBLE);

            if(fromUserID.equals(messageSenderId)){
                holder.SenderMessageText.setBackgroundResource(R.drawable.message_sender_background);
                holder.SenderMessageText.setTextColor(Color.WHITE);
                holder.SenderMessageText.setGravity(Gravity.LEFT);
                holder.SenderMessageText.setText(messages.getMessage());
            }else{
                holder.SenderMessageText.setVisibility(View.INVISIBLE);
                holder.ReceiverMessageText.setVisibility(View.VISIBLE);
                holder.ReceiverProfileImage.setVisibility(View.VISIBLE);

                holder.ReceiverMessageText.setBackgroundResource(R.drawable.message_receiver_background);
                holder.ReceiverMessageText.setTextColor(Color.WHITE);
                holder.ReceiverMessageText.setGravity(Gravity.LEFT);
                holder.ReceiverMessageText.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }
}
