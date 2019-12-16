package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView tvRegister = findViewById(R.id.register);
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(myIntent);
            }
        });

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                successfulLogin();
            } else {
                Toast.makeText(this, "Email is not verified",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClickLogin(View view) {
        EditText email = findViewById(R.id.et_username);
        EditText password = findViewById(R.id.et_password);
        if (email.getText().toString().matches("") || password.getText().toString().matches(""))
        {
            Toast.makeText(this, "Enter both email and password",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                if (currentUser != null) {
                                    if (currentUser.isEmailVerified()) {
                                        successfulLogin();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Email is not verified",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                if (task.getException() != null) {
                                    Toast.makeText(LoginActivity.this, "Sign in failure: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Sign in failure",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    public void successfulLogin()
    {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult().getToken();
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            DatabaseReference databaseReference =
                                    FirebaseDatabase.getInstance().getReference().child("userTokens").child(currentUser.getUid());
                            databaseReference.setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        if (task.getException() != null) {
                                            Toast.makeText(LoginActivity.this, "Save instance token failure: " + task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Save instance token failure",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        } else {
                            if (task.getException() != null) {
                                Toast.makeText(LoginActivity.this, "Get instance token failure: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Get instance token failure",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
