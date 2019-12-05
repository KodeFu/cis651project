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

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null) {
            if(currentUser.isEmailVerified()) {
                successfulLogin();
            }
            else
            {
                Toast.makeText(this, "Email is not Verified",
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
                    .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if(currentUser.isEmailVerified()) {
                                Log.d("debug", "onClickSignIn:success");
                                Toast.makeText(LoginActivity.this, "Sign in succeeded.",
                                        Toast.LENGTH_SHORT).show();
                                successfulLogin();
                            } else {
                                Toast.makeText(LoginActivity.this, "Email is not Verified",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("debug", "onClickSignIn Failure:" + e.getMessage());
                            Toast.makeText(LoginActivity.this, "Sign in failure" + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void successfulLogin()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
