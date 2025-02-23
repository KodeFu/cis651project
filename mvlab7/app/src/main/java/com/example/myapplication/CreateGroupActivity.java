package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class CreateGroupActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    HashMap<String, Group> groupsList = new HashMap<String, Group>();
    EditText groupName;
    TextView token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        buildNavDrawerAndToolbar();

        mAuth = FirebaseAuth.getInstance();
        groupName = findViewById(R.id.group_name);
        token = findViewById(R.id.token);

        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    Group g = ds.getValue(Group.class);
                    g.token = child;
                    groupsList.put(child, g);

                    Log.d("appdebug", "CreateGroupActivity onDataChanged: " + child + " " + ds.getValue());
                }

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("appdebug", "onCancelled");
            }
        });


    }

    public void updateUI()
    {
        Log.d("appdebug", "updateUI: ");
        groupName.setText(GroupsHelper.getGroupName(groupsList));
        token.setText(GroupsHelper.getGroupToken(groupsList));

        Log.d("appdebug", "group token: " + GroupsHelper.getGroupToken(groupsList));
        Log.d("appdebug", "group  name: " + GroupsHelper.getGroupName(groupsList));
        Log.d("appdebug", "group admin: " + GroupsHelper.getGroupAdmin(groupsList));
    }

    public void onClickCreateGroup(View view) {
        String myGroup = GroupsHelper.getGroupName(groupsList);

        if ( !myGroup.equals("") ) {
            Toast.makeText(getApplicationContext(), "Already in a Group",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final String currentUid = mAuth.getCurrentUser().getUid();
        DatabaseReference usersRef = mRootReference.child("users").child(currentUid);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.uid = dataSnapshot.getKey();
                if (GroupsHelper.createGroup(u, groupsList, groupName.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(), "Create Successful",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CreateGroupActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Can Not Create Group",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onClickRemoveGroup(View view) {
        if (!GroupsHelper.isAdmin(groupsList)) {
            Toast.makeText(getApplicationContext(), "Must be Administrator of the Group",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (GroupsHelper.removeGroup(groupsList))
        {
            Toast.makeText(getApplicationContext(), "Remove Successful",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CreateGroupActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Can Not Remove Group",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
