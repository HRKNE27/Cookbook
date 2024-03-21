package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {
    private EditText userEmail;
    private EditText userPassword;
    private EditText userConfirmPassword;
    private Button createAccountButton;
    private ProgressBar loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        // Gets elements on the screen
        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword = (EditText) findViewById(R.id.register_password);
        userConfirmPassword = (EditText) findViewById(R.id.confirm_register_password);
        createAccountButton = (Button) findViewById(R.id.register_create_account);

        // Button to create account when all text fields are filled out
        createAccountButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    // If the user is already signed in, then this will send them directly to Home page
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            SendUserToMain();
        }
    }

    // Helper function to send user to Home page from the Register page
    private void SendUserToMain() {
        Intent setupIntent = new Intent(Register.this,MainActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    // Function takes in all text fields and creates an account and pushes it to Firebase
    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirm_password = userConfirmPassword.getText().toString();

        // Checks to make sure there is email, password, and passwords match
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter your email",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your password",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(confirm_password)){
            Toast.makeText(this,"Please confirm your password",Toast.LENGTH_SHORT).show();
        }else if(!password.equals(confirm_password)){
            Toast.makeText(this,"Your passwords do not match",Toast.LENGTH_SHORT).show();
        }else{
            // This adds the loading circle to the middle of screen while logging in
            // Also prevents user from tapping the screen while signing in
            RelativeLayout layout = findViewById(R.id.main_register_layout);
            loadingBar = new ProgressBar(Register.this,null,android.R.attr.progressBarStyleLarge);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.addView(loadingBar,params);
            loadingBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // Creates a Firebase authentication account, so user can sign in with email and password
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Register.this,"Your account is created",Toast.LENGTH_SHORT).show();
                                // Removes the loading bar and allows user to interact with screen again
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                loadingBar.setVisibility(View.GONE);
                                // sendUserToSetUpActivity();
                                SendEmailVerification();
                            }else{
                                // Error usually means that account already exists or password is too short
                                String error_message = task.getException().getMessage();
                                Toast.makeText(Register.this,"Error occurred: " + error_message,Toast.LENGTH_SHORT).show();
                                // Removes the loading bar and allows user to interact with screen again
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                loadingBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private void SendEmailVerification(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Register.this,"Check your email for a verification email",Toast.LENGTH_SHORT).show();
                        sendUserToLoginActivity();
                        mAuth.signOut();
                    }else{
                        Toast.makeText(Register.this,"Error: " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }

    // Helper function to send user to SetUp page from the Register page
    private void sendUserToLoginActivity() {
        Intent setupIntent = new Intent(Register.this,Login.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}