package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity {
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildNavDrawerAndToolbar();

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = rootRef.child("users/" + currentUser.getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.uid = dataSnapshot.getKey();
                Log.d("appdebug", "User query success");

                if (u.group != null && !u.group.isEmpty()) {
                    DatabaseReference spendingRef = rootRef.child("spending/" + u.group);
                    spendingRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Spending s = dataSnapshot.getValue(Spending.class);
                            s.token = dataSnapshot.getKey();
                            Log.d("appdebug", "Spending query success");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
