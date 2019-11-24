package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void onClickUploadPhoto(View view) {
        Toast.makeText(getApplicationContext(), "onClickUploadPhoto",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickSaveProfile(View view) {
        Toast.makeText(getApplicationContext(), "onClickSaveProfile",
                Toast.LENGTH_SHORT).show();
    }
}
