package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class JoinGroupActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    HashMap<String, Group> groupsList = new HashMap<String, Group>();
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
                    g.token = child;
                    groupsList.put(child, g);

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
                    g.token = child;
                    groupsList.put(child, g);

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
                    g.token = child;
                    groupsList.put(child, g);

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

    public void addMemberToGroup(HashMap<String, Group> groups, final String token)
    {
        for (Map.Entry g : groups.entrySet()) {
            if ( g.getKey().equals(token) ) {

                for (Map.Entry m : ((Group)g.getValue()).members.entrySet()) {
                    if (m.getKey().equals(mAuth.getCurrentUser().getUid())) {
                        // User already added
                        Toast.makeText(getApplicationContext(), "Already part of this group.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Add user
                final Member m = new Member();
                m.uid = mAuth.getCurrentUser().getUid();
                DatabaseReference userRef = mRootReference.child("users").child(m.uid);
                final Group group = ((Group)g.getValue());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User u = dataSnapshot.getValue(User.class);
                        u.uid = dataSnapshot.getKey();
                        m.displayName = u.displayName;
                        group.members.put(m.uid, m);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

        // Add groups node
        groupsRef.setValue(groups).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    DatabaseReference uidRef = mRootReference.child("users").child(mAuth.getCurrentUser().getUid());
                    uidRef.child("group").setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(JoinGroupActivity.this, "Join Group successful",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(JoinGroupActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(JoinGroupActivity.this, "Unable to save user to database",
                                        Toast.LENGTH_SHORT).show();
                                if (task.getException() != null) {
                                    Toast.makeText(JoinGroupActivity.this, task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(JoinGroupActivity.this, "Unable to save group to database",
                            Toast.LENGTH_SHORT).show();
                    if (task.getException() != null) {
                        Toast.makeText(JoinGroupActivity.this, task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
