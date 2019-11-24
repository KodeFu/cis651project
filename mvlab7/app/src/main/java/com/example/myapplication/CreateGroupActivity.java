package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Toast;

public class CreateGroupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        buildNavDrawerAndToolbar();
    }

    public void onClickCreateGroup(View view) {
        Toast.makeText(getApplicationContext(), "onClickCreateGroup",
                Toast.LENGTH_SHORT).show();
    }
}
