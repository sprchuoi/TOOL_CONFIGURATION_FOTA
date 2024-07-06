package com.example.aquasys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private TextView editTextCurrentUser;
    private EditText editTextCurrentPassword;
    private EditText editTextNewPassword;
    private Button btnChangePassword;
    private FirebaseAuth firebaseAuth;
    private Toolbar changepass_toolbar;
    private ProgressBar progressBar_Status;
    private View dimOverlay; // Add a reference to the dim overlay view
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        dimOverlay = findViewById(R.id.view); // Initialize dim overlay view
        dimOverlay.setVisibility(View.GONE); // Initially set it invisible

        editTextCurrentUser = findViewById(R.id.TextCurrentUser);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progressBar_Status = findViewById(R.id.progressBar_status);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            String userEmail = user.getEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                editTextCurrentUser.setText(userEmail);
            } else {
                editTextCurrentUser.setText("Error when getting email");
            }
        } else {
            editTextCurrentUser.setText("User is not logged in");
        }

        btnChangePassword.setOnClickListener(view -> {
            String currentPassword = editTextCurrentPassword.getText().toString();
            String newPassword = editTextNewPassword.getText().toString();

            if (user != null && !currentPassword.isEmpty() && !newPassword.isEmpty()) {
                // Thực hiện thay đổi mật khẩu
                String userEmail = user.getEmail();
                if (userEmail != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(userEmail, currentPassword);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(reauthTask -> {
                                if (reauthTask.isSuccessful()) {
                                    // Re-authentication thành công, thực hiện thay đổi mật khẩu
                                    showProgressBarWithOverlay();
                                    user.updatePassword(newPassword)
                                            .addOnCompleteListener(changePasswordTask -> {
                                                hideProgressBarWithOverlay();
                                                if (changePasswordTask.isSuccessful()) {
                                                    Toast.makeText(ChangePassword.this, "Change Password Completed", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(ChangePassword.this, "Change Password Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(ChangePassword.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(ChangePassword.this, "Get email failed", Toast.LENGTH_SHORT).show();
                }
            }
            else Toast.makeText(ChangePassword.this, "Empty field", Toast.LENGTH_SHORT).show();
        });


        changepass_toolbar = findViewById(R.id.change_password_toolbar);
        changepass_toolbar.setTitle("Change Password");
        setBackButtonOnToolbar();
    }
    public void setBackButtonOnToolbar() {
        setSupportActionBar(changepass_toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Change Password");
        }
        changepass_toolbar.setNavigationOnClickListener(v -> finish());
    }
    private void showProgressBarWithOverlay() {
        dimOverlay.setVisibility(View.VISIBLE);
        progressBar_Status.setVisibility(View.VISIBLE);

    }

    private void hideProgressBarWithOverlay() {
        dimOverlay.setVisibility(View.INVISIBLE);
        progressBar_Status.setVisibility(View.INVISIBLE);

    }
}