package com.example.aquasys.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aquasys.MainActivity;
import com.example.aquasys.R;
import com.example.aquasys.object.sensor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private TextInputEditText edt_usr_login, edt_psw_login;
    private Button btn_login;
    private ProgressBar login_progressbar;
    private Intent intent;
    private TextView tv_forgot_pass;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // authentication
        mAuth = FirebaseAuth.getInstance();



        //mapping
        edt_usr_login = findViewById(R.id.edt_usr_login);
        edt_psw_login = findViewById(R.id.edt_psw_login);
        btn_login = findViewById(R.id.btn_login);
        login_progressbar = findViewById(R.id.login_progressbar);
        tv_forgot_pass = findViewById(R.id.tv_forgot_pass);
        login_progressbar.setVisibility(View.GONE);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = edt_usr_login.getText().toString();
                password = edt_psw_login.getText().toString();
                // check the Email or Password is empty
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    // set the progressbar visible when the task login is running
                    login_progressbar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // set the progressbar gone when the task login is completing

                                    if (task.isSuccessful()) {
                                        // Toast popup login success and go to main page
                                        Toast.makeText(Login.this, " Login success", Toast.LENGTH_SHORT).show();
                                        intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        login_progressbar.setVisibility(View.GONE);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Login.this, "Wrong password or username",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        // OnClick open forgot page activity
        tv_forgot_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(getApplicationContext(), Forgot.class);
                startActivity(intent);
                finish();
            }
        });
    }
}