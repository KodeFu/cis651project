package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    public void onClickLogin(View view) {
        EditText email = findViewById(R.id.et_username);
        EditText password = findViewById(R.id.et_password);
        if (email.getText().toString().matches("") || password.getText().toString().matches(""))
        {
            Toast.makeText(getApplicationContext(), "Enter both email and password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(this,
                    new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Log.d("debug", "onClickSignIn:success");
                            Toast.makeText(getApplicationContext(), "Sign in succeeded.", Toast.LENGTH_SHORT).show();
                            successfulLogin();
                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("debug", "onClickSignIn Failure:" + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Sign in failure" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void successfulLogin()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
