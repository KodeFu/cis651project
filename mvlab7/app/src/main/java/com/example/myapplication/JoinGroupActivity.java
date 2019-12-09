package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class JoinGroupActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    DatabaseReference mRootReference= FirebaseDatabase.getInstance().getReference();
    DatabaseReference groupsRef =  mRootReference;
    List<Group> groupsList = new ArrayList<Group>();
    EditText token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        buildNavDrawerAndToolbar();

        token = findViewById(R.id.token);
        mAuth = FirebaseAuth.getInstance();
        Log.d("appdebug", "JoinGroupActivity onCreate: onCreate called...");


        groupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (!dataSnapshot.getKey().equals("groups")) {
                    // Ignore nodes other than groups
                    return;
                }

                groupsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    Group g = ds.getValue(Group.class);
                    groupsList.add(g);

                    Log.d("appdebug", "onChildAdded: " + child + " " + ds.getValue());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("appdebug", "onChildChanged");

                if (!dataSnapshot.getKey().equals("groups")) {
                    // Ignore nodes other than groups
                    return;
                }

                groupsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    Group g = ds.getValue(Group.class);
                    groupsList.add(g);

                    Log.d("appdebug", "onChildChanged: " + child + " " + ds.getValue());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d("appdebug", "onChildRemoved");

                if (!dataSnapshot.getKey().equals("groups")) {
                    // Ignore nodes other than groups
                    return;
                }

                groupsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    Group g = ds.getValue(Group.class);
                    groupsList.add(g);

                    Log.d("appdebug", "onChildRemoved: " + child + " " + ds.getValue());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("appdebug", "onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("appdebug", "onCancelled");
            }
        });
    }

    public void onClickJoinGroup(View view) {
        addMemberToGroup(groupsList, token.getText().toString());
    }

    public void addMemberToGroup(List<Group> groups, String token)
    {
        for (Group g : groups) {
            if ( g.token.equals(token) ) {

                for (Member m : g.members) {
                    if (m.uid.equals(mAuth.getCurrentUser().getUid())) {
                        // User already added
                        Toast.makeText(getApplicationContext(), "Already part of this group.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Add user
                Member m = new Member();
                m.uid = mAuth.getCurrentUser().getUid();
                m.name = mAuth.getCurrentUser().getDisplayName();
                g.members.add(m);
            }
        }

        // Add groups node
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupsRef =  mRootReference.child("groups");
        groupsRef.setValue(groups);
    }
}
