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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindUsers extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView search_button;
    private EditText search_input;
    private RecyclerView search_results;
    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        // Editing the toolbar
        mToolbar = (Toolbar) findViewById(R.id.find_user_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search Users");

        // Reference to the users node in firebase database, contains all users
        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Recycler view that will contain all search results
        search_results = (RecyclerView) findViewById(R.id.find_user_search_result);
        search_results.setHasFixedSize(true);
        search_results.setLayoutManager(new LinearLayoutManager(this));

        search_button = (ImageView) findViewById(R.id.find_user_search_button);
        search_input = (EditText) findViewById(R.id.find_user_searchbar);

        // When the button is clicked search users based on input
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searched_user = search_input.getText().toString();
                SearchUsers(searched_user);
            }
        });
    }

    // Function to search all users in the "Users" node in Firebase Database
    private void SearchUsers(String searchedUser) {

        // Query search looks for all users based on what was inputted by user
        Toast.makeText(FindUsers.this,"Searching for User",Toast.LENGTH_SHORT).show();
        Query searchAllUsers = allUsersDatabaseRef.orderByChild("FullName").startAt(searchedUser).endAt(searchedUser + "\uf8ff");

        // Creates a firebase recycler view that uses the Users class (Profile pic, Status, and Name)
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(searchAllUsers,Users.class)
                .build();

        // This is the adapter that sets the values for the firebase recycler
        FirebaseRecyclerAdapter<Users,FindUsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, FindUsersViewHolder>(options) {

            // Holder is the blueprint that we fill out and model is the Users class where we get the info from (I think?)
            @Override
            protected void onBindViewHolder(@NonNull FindUsersViewHolder holder, int position, @NonNull Users model) {
                holder.setFullName(model.getFullName());
                holder.setProfileImage(model.getProfileImage());
                holder.setStatus(model.getStatus());
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(holder.getBindingAdapterPosition()).getKey();
                        Intent profileIntent = new Intent(FindUsers.this,UserProfile.class);
                        profileIntent.putExtra("visit_uid",visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            // Populates the recycler view with the "search_users_display_layout" layout file with the filled out values
            @NonNull
            @Override
            public FindUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_users_display_layout,parent,false);
                FindUsersViewHolder viewHolder = new FindUsersViewHolder(view);
                return viewHolder;
            }
        };
        // Sets the adapter to the recycler view and recycler view starts to listen to any changes
        firebaseRecyclerAdapter.startListening();
        search_results.setAdapter(firebaseRecyclerAdapter);
    }

    // Allows above function to set the values
    public static class FindUsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FindUsersViewHolder(View itemView){
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

        public void setProfileImage(String ProfileImage){
            CircleImageView myProfileImage = (CircleImageView) mView.findViewById(R.id.search_users_profile_image);
            Picasso.get().load(ProfileImage).placeholder(R.drawable.profile_placeholder).into(myProfileImage);
        }
    }
}