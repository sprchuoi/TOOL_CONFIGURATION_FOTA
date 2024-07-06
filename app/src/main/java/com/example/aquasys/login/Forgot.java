package com.example.aquasys.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.aquasys.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

public class Forgot extends AppCompatActivity {
    private Button  btn_reset_pass;
    private FloatingActionButton btn_back_to_login;
    private EditText edt_email_forgot_page;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ProgressBar forgot_pass_progressbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        // mapping for UI
        cast_UI();
        btn_reset_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResetPasswordEmail();
            }
        });
        // back to login page btn
        btn_back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Back_To_Login();
            }
        });
    }
    // init mapping for UI
    private void cast_UI(){
        btn_back_to_login = findViewById(R.id.btn_back_to_login);
        btn_reset_pass = findViewById(R.id.btn_reset_pass);
        edt_email_forgot_page = findViewById(R.id.edt_email_forgot_page);
        forgot_pass_progressbar = findViewById(R.id.forgot_pass_progressbar);
    }
    // init firebase user
    private void sendResetPasswordEmail() {
        mAuth = FirebaseAuth.getInstance();
        String email = edt_email_forgot_page.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show();
        } else {
            forgot_pass_progressbar.setVisibility(View.VISIBLE);

            // Check if the email exists in Firebase Authentication
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.isSuccessful()) {
                                SignInMethodQueryResult result = task.getResult();
                                if (result != null && result.getSignInMethods() != null ) {
                                    // Email exists, send a password reset email
                                    mAuth.sendPasswordResetEmail(email)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    forgot_pass_progressbar.setVisibility(View.GONE);
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Forgot.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(Forgot.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    // Email does not exist in Firebase Authentication
                                    forgot_pass_progressbar.setVisibility(View.GONE);
                                    Toast.makeText(Forgot.this, "Email does not exist", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Handle the error here, if any
                                forgot_pass_progressbar.setVisibility(View.GONE);
                                Toast.makeText(Forgot.this, "An error occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void Back_To_Login(){
        Intent intent = new Intent(getApplicationContext() , Login.class);
        startActivity(intent);
        finish();
    }
}