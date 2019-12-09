package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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

public class AdministerGroupActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    DatabaseReference mRootReference= FirebaseDatabase.getInstance().getReference();
    DatabaseReference groupsRef =  mRootReference;
    List<Group> groupsList = new ArrayList<Group>();
    String[] categories = { "Clothing", "Groceries", "Dining", "Ride Share", "Entertainment", "Gifts", "Fuel / Gas", "Automobile", "Home Improvement", "Credit Cards"};
    ArrayList<String> membersList = new ArrayList<String>();
    ArrayAdapter adapterMembersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer_group);
        buildNavDrawerAndToolbar();

        mAuth = FirebaseAuth.getInstance();

        adapterMembersList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, membersList);
        Spinner members = (Spinner) findViewById(R.id.members);
        members.setAdapter(adapterMembersList);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categories);
        Spinner category = (Spinner) findViewById(R.id.category);
        category.setAdapter(adapter);

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

        EditText groupName = findViewById(R.id.group_name);
        groupName.setText(getGroupName(groupsList));

        List<Member> groupMemberList = getMembers(groupsList);
        membersList.clear();

        for (Member m: groupMemberList) {
            membersList.add(m.name);
        }
        Spinner m = findViewById(R.id.members);
        adapterMembersList.notifyDataSetChanged();

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

    void removeMember(List<Group> groups, String uid)
    {
        // Not an admin? see if member of a group
        for (Group g : groups) {
            for (Member m : g.members) {
                if (m.uid.equals(mAuth.getCurrentUser().getUid())) {
                    g.members.remove(m);
                }
            }
        }

        // Add groups node
        DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupsRef =  mRootReference.child("groups");
        groupsRef.setValue(groups);
    }

    public void onClickGroupDelete(View view) {
        Toast.makeText(getApplicationContext(), "onClickGroupDelete",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickGroupUpdate(View view) {
        Toast.makeText(getApplicationContext(), "onClickGroupUpdate",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickCategoryDelete(View view) {
        Toast.makeText(getApplicationContext(), "onClickCategoryDelete",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickCategoryUpdate(View view) {
        Toast.makeText(getApplicationContext(), "onClickCategoryUpdate",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickMemberRemove(View view)
    {
        String uid = "";
        Spinner memberSpinner = findViewById(R.id.members);
        String selectedItem = memberSpinner.getSelectedItem().toString();

        Log.d("appdebug", "onClickMemberRemove: " + selectedItem);

        List<Member> groupMemberList = getMembers(groupsList);

        for (Member m: groupMemberList) {
            if (m.name.equals(selectedItem)) {
                uid = m.uid;
            }
        }

        if (!uid.equals("")) {
            removeMember(groupsList, uid);

            //membersList.remove()
        }

        adapterMembersList.notifyDataSetChanged();
    }
}
