package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void OnClickCreateGroup(View view) {
        Intent intent = new Intent(MainActivity.this, CreateGroup.class);
        startActivity(intent);
    }

    public void OnClickAdministerGroup(View view) {
        Intent intent = new Intent(MainActivity.this, AdministerGroup.class);
        startActivity(intent);
    }

    public void OnClickEditGroupCategories(View view) {
        Intent intent = new Intent(MainActivity.this, EditGroupCategories.class);
        startActivity(intent);
    }

    public void OnClickJoinGroup(View view) {
        Intent intent = new Intent(MainActivity.this, JoinGroup.class);
        startActivity(intent);
    }

    public void OnClickSubmitReceipt(View view) {
        Intent intent = new Intent(MainActivity.this, SubmitReceipt.class);
        startActivity(intent);
    }
}
