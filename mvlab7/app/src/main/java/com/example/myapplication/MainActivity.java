package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildNavDrawerAndToolbar();
    }

    public void onClickCreateGroup(View view) {
        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivity(intent);
    }

    public void onClickJoinGroup(View view) {
        Intent intent = new Intent(this, JoinGroupActivity.class);
        startActivity(intent);
    }

    public void onClickLeaveGroup(View view) {
        Toast.makeText(getApplicationContext(), "Leave Group",
                Toast.LENGTH_SHORT).show();  }

    public void onClickAdministerGroup(View view) {
        Intent intent = new Intent(this, AdministerGroupActivity.class);
        startActivity(intent);
    }

    public void onClickViewSpending(View view) {
        Intent intent = new Intent(this, ViewSpendingActivity.class);
        startActivity(intent);
    }

    public void onClickSubmitReceipt(View view) {
        Intent intent = new Intent(this, SubmitReceiptActivity.class);
        startActivity(intent);
    }
}
