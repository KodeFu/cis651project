package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class JoinGroupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        buildNavDrawerAndToolbar();
    }

    public void onClickJoinGroup(View view) {
        Toast.makeText(getApplicationContext(), "onClickJoinGroup",
                Toast.LENGTH_SHORT).show();
    }
}
