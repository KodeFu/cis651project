package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.et_username);
        password = (EditText) findViewById(R.id.et_password);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void onClickTakePhoto(View view) {
        Toast.makeText(getApplicationContext(), "onClickTakePhoto",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickSaveProfile(View view) {
        if (email.getText().toString().matches("") || password.getText().toString().matches(""))
        {
            Toast.makeText(getApplicationContext(), "Enter both email and password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            createAccount(email.getText().toString(), password.getText().toString());
        }
        Toast.makeText(getApplicationContext(), "onClickSaveProfile",
                Toast.LENGTH_SHORT).show();
    }

    private void createAccount(String email, String password) {
        // https://firebase.google.com/docs/auth/android/manage-users
        Log.d("appdebug", "createAccount:" + email);
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d("appdebug", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(RegisterActivity.this, "Authentication succeeded.",
                                Toast.LENGTH_SHORT).show();
                        SendVerification();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("appdebug", "Failure:"+e.getMessage());
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(RegisterActivity.this, "Failure:"+e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void SendVerification(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Email Sent",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
