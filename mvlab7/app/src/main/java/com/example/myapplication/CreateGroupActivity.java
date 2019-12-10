package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CreateGroupActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    DatabaseReference mRootReference= FirebaseDatabase.getInstance().getReference();
    DatabaseReference groupsRef =  mRootReference;
    List<Group> groupsList = new ArrayList<Group>();
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

        groupsRef.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d("appdebug", "onChildAdded: start");
                //Log.d("appdebug", "onChildAdded: numChildren " + dataSnapshot.getChildrenCount());
                //Log.d("appdebug", "onChildAdded: key " + dataSnapshot.getKey());

                groupsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    Group g = ds.getValue(Group.class);
                    g.token = child;
                    groupsList.add(g);

                    Log.d("appdebug", "onChildAdded: " + child + " " + ds.getValue());
                }

                updateUI();
                //Log.d("appdebug", "onChildAdded: end");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void updateUI()
    {
        Log.d("appdebug", "updateUI: ");
        groupName.setText(getGroupName(groupsList));
        token.setText(getGroupToken(groupsList));

        Log.d("appdebug", "group token: " + getGroupToken(groupsList));
        Log.d("appdebug", "group  name: " + getGroupName(groupsList));
        Log.d("appdebug", "group admin: " + getGroupAdmin(groupsList));
    }

    public void onClickCreateGroup(View view) {
        createGroup(groupsList, groupName.getText().toString());
    }

    public void createGroup(List<Group> groups, String name)
    {
        String currentGroupName = getGroupName(groups);
        if (!currentGroupName.equals("")) {
            // We are currently in a group, so can't create another
            Toast.makeText(getApplicationContext(), "Already in a group. Leave group before creating a new group.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Group g = new Group();
        Category c = new Category();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser.getUid();
        String currentName = currentUser.getDisplayName();

        // Group info
        g.name = name;
        g.adminUid = currentUid;

        // Member info
        g.members = new HashMap<String, String>();
        g.members.put(currentUid, "enabled");

        // Category info
        g.categories = new HashMap<String, Category>();
        g.categories.put(currentUid, new Category(currentUid, currentName, -1));

        // Create child reference; i.e. group node
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupsRef =  mRootReference.child("groups");
        Random rand = new Random();
        g.token = Integer.toString(rand.nextInt(1000000));
        groupsRef.child(g.token).setValue(g);
    }

    String getGroupToken(List<Group> groups)
    {
        // Only admin can see token; verbally conveyed to users
        for (Group g : groups) {
            if ( g.adminUid.equals(mAuth.getCurrentUser().getUid()) ) {
                return g.token;
            }
        }

        return "";
    }

    String getGroupName(List<Group> groups)
        {
        // If admin of a group, return that
        for (Group g : groups) {
            if ( g.adminUid.equals(mAuth.getCurrentUser().getUid()) ) {
                return g.name;
            }
        }

        // Not an admin? see if member of a group
        for (Group g : groups) {
            for (Map.Entry me : g.members.entrySet()) {
                if (me.getKey().equals(mAuth.getCurrentUser().getUid())) {
                    return g.name;
                }
            }
        }

        // Not an admin or member, so not part of a group
        return "";
    }

    String getGroupAdmin(List<Group> groups)
    {
        String myGroupName = getGroupName(groups);

        for (Group g : groups) {
            if ( g.name.equals(myGroupName) ) {
                return g.adminUid;
            }
        }

        return "";
    }

    Map<String, String> getMembers(List<Group> groups)
    {
        String myGroupName = getGroupName(groups);

        for (Group g : groups) {
            if ( g.name.equals(myGroupName) ) {
                return g.members;
            }
        }

        return new HashMap<String, String>();
    }

    public Map<String, Category> getCategories(List<Group> groups)
    {
        String myGroupName = getGroupName(groups);

        for (Group g : groups) {
            if ( g.name.equals(myGroupName) ) {
                return g.categories;
            }
        }

        return new HashMap<String, Category>();
    }

}
