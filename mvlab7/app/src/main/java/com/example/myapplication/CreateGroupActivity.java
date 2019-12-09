package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

        groupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (!dataSnapshot.getKey().equals("groups")) {
                    // Ignore nodes other than groups
                    return;
                }

                //Log.d("appdebug", "onChildAdded: start");
                //Log.d("appdebug", "onChildAdded: numChildren " + dataSnapshot.getChildrenCount());
                //Log.d("appdebug", "onChildAdded: key " + dataSnapshot.getKey());

                groupsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    Group g = ds.getValue(Group.class);
                    groupsList.add(g);

                    Log.d("appdebug", "onChildAdded: " + child + " " + ds.getValue());
                }

                updateUI();
                //Log.d("appdebug", "onChildAdded: end");

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("appdebug","onChildChanged");

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

                updateUI();
                //Log.d("appdebug", "onChildChanged: end");

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d("appdebug","onChildRemoved");

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

                updateUI();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("appdebug","onChildMoved");
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
        Member m = new Member();
        Category c = new Category();

        // Group info
        g.name = name;
        g.adminUID = mAuth.getCurrentUser().getUid();
        Random rand = new Random();
        g.token = Integer.toString(rand.nextInt(10000));

        // Member info
        g.members = new ArrayList<Member>();
        m.uid = mAuth.getCurrentUser().getUid();
        m.name = mAuth.getCurrentUser().getDisplayName();
        g.members.add(m);

        // Category info
        g.categories = new ArrayList<Category>();
        c.name = mAuth.getCurrentUser().getUid();
        c.limit = -1;
        g.categories.add(c);

        groups.add(g);

        // Create child reference; i.e. group node
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupsRef =  mRootReference.child("groups");
        groupsRef.setValue(groups);
    }

    String getGroupToken(List<Group> groups)
    {
        // Only admin can see token; verbally conveyed to users
        for (Group g : groups) {
            if ( g.adminUID.equals(mAuth.getCurrentUser().getUid()) ) {
                return g.token;
            }
        }

        return "";
    }

    String getGroupName(List<Group> groups)
        {
        // If admin of a group, return that
        for (Group g : groups) {
            if ( g.adminUID.equals(mAuth.getCurrentUser().getUid()) ) {
                return g.name;
            }
        }

        // Not an admin? see if member of a group
        for (Group g : groups) {
            for (Member m : g.members) {
                if (m.uid.equals(mAuth.getCurrentUser().getUid())) {
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
                return g.adminUID;
            }
        }

        return "";
    }

    List<Member> getMembers(List<Group> groups)
    {
        String myGroupName = getGroupName(groups);

        for (Group g : groups) {
            if ( g.name.equals(myGroupName) ) {
                return g.members;
            }
        }

        ArrayList<Member> emptyList = new ArrayList<Member>();
        return emptyList;
    }

    List<Category> getCategories(List<Group> groups)
    {
        String myGroupName = getGroupName(groups);

        for (Group g : groups) {
            if ( g.name.equals(myGroupName) ) {
                return g.categories;
            }
        }

        ArrayList<Category> emptyList = new ArrayList<Category>();
        return emptyList;
    }

}
