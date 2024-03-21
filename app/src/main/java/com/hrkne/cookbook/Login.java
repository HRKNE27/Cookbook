package com.hrkne.cookbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private Button loginButton;
    private EditText userEmail;
    private EditText userPassword;
    private TextView needNewAccount,forgotPassword;
    private FirebaseAuth mAuth;
    private ProgressBar loadingBar;
    private Boolean emailChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Getting elements on the screen
        loginButton = (Button) findViewById(R.id.login_account);
        userEmail = (EditText) findViewById(R.id.login_email);
        userPassword = (EditText) findViewById(R.id.login_password);
        needNewAccount = (TextView) findViewById(R.id.create_account_link);
        forgotPassword = (TextView) findViewById(R.id.forgot_password_link);


        // Sends user to register with a new account if they don't have one
        needNewAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SendUserToRegister();
            }
        });

        // Send user to reset their password if they forgot it
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToForgotPassword();
            }
        });

        // When user clicks login button, checks to see if the email and password exists and login if true
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
    }



    // If there is user signed in, immediately send user to the homepage rather than login page every
    // time the app is opened
    @Override
    protected void onStart() {
        super.onStart();
         FirebaseUser currentUser = mAuth.getCurrentUser();
         if(currentUser != null){
             SendUserToMain();
         }
    }

    // Function checks Firebase to confirm that there is an account that matches the email/password
    // given, if so, it will sign into the app.
    private void AllowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            // If user didn't enter email, prompts them to enter email
            Toast.makeText(this,"Please enter your email",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            // If user didn't enter password, prompts them to enter password
            Toast.makeText(this,"Please enter your password",Toast.LENGTH_SHORT).show();
        }else{
            // This adds the loading circle to the middle of screen while logging in
            // Also prevents user from tapping the screen while signing in
            RelativeLayout layout = findViewById(R.id.main_login_layout);
            loadingBar = new ProgressBar(Login.this,null,android.R.attr.progressBarStyleLarge);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.addView(loadingBar,params);
            loadingBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // If a email and password is given, this will try to sign in the account if email and
            // password matches, or it will return an error saying that there isn't a match
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                // Means that email/password matched, so it will send to homepage, signed in
                                // SendUserToMain();
                                // Toast.makeText(Login.this,"Logging In",Toast.LENGTH_SHORT).show();
                                // Removes the loading bar and allows user to interact with screen again
                                VerifyEmail();

                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                loadingBar.setVisibility(View.GONE);
                            }else{
                                // Means that email/password didn't match and tells user that
                                String error_message = task.getException().toString();
                                Toast.makeText(Login.this,"Error Occurred: " + error_message,Toast.LENGTH_SHORT).show();
                                // Removes the loading bar and allows user to interact with screen again
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                loadingBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private void VerifyEmail(){
        FirebaseUser user = mAuth.getCurrentUser();
        emailChecker = user.isEmailVerified();
        if(emailChecker){
            SendUserToMain();
        }else{
            Toast.makeText(Login.this,"Please verify your email first",Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }
    }

    // Helper function to send to Home page from Login page
    private void SendUserToMain() {
        Intent mainIntent = new Intent(Login.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    // Helper function to send to Register page from Login page
    private void SendUserToRegister() {
        Intent registerIntent = new Intent(Login.this,Register.class);
        registerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(registerIntent);
        finish();
    }

    private void SendUserToForgotPassword(){
        Intent registerIntent = new Intent(Login.this,ResetPassword.class);
        registerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(registerIntent);
        finish();
    }
}