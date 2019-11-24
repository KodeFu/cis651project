package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class JoinGroup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
    }

    public void OnClickJoinGroup(View view) {
        Toast.makeText(getApplicationContext(), "OnClickJoinGroup",
                Toast.LENGTH_SHORT).show();
    }
}
