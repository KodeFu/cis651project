package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdministerGroupActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    DatabaseReference mRootReference= FirebaseDatabase.getInstance().getReference();
    DatabaseReference groupsRef =  mRootReference;
    HashMap<String, Group> groupsList = new HashMap<String, Group>();
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

        groupsRef.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    Group g = ds.getValue(Group.class);
                    g.token = child;
                    groupsList.put(child, g);

                    Log.d("appdebug", "onChildAdded: " + child + " " + ds.getValue());
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

        EditText groupName = findViewById(R.id.group_name);
        groupName.setText(GroupsHelper.getGroupName(groupsList));

        Map<String, Member> groupMemberList = GroupsHelper.getMembers(groupsList);
        membersList.clear();

        for (Map.Entry m : groupMemberList.entrySet()) {
            membersList.add(((Member)m.getValue()).displayName);
        }
        Spinner m = findViewById(R.id.members);
        adapterMembersList.notifyDataSetChanged();

        TextView token = findViewById(R.id.token);
        token.setText(GroupsHelper.getGroupToken(groupsList));

    }

    public void onClickGroupUpdate(View view) {
        EditText groupName = findViewById(R.id.group_name);
        String s = groupName.getText().toString();
        if ( (!s.equals("")) && GroupsHelper.updateGroupName(groupsList, s))
        {
            Toast.makeText(getApplicationContext(), "Update Successful",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Could Not Update Group",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickMemberRemove(View view)
    {
        String uid = "";
        Spinner memberSpinner = findViewById(R.id.members);
        String selectedItem = memberSpinner.getSelectedItem().toString();

        Log.d("appdebug", "onClickMemberRemove: " + selectedItem);

        Map<String, Member> groupMemberList = GroupsHelper.getMembers(groupsList);

        for (Map.Entry m : groupMemberList.entrySet()) {
            if (((Member)m.getValue()).displayName.equals(selectedItem)) {
                uid = m.getKey().toString();
            }
        }

        if (!uid.equals("")) {
            Log.d("appdebug", "onClickMemberRemove: uid of member " + uid);
            if (GroupsHelper.removeMember(groupsList, uid))
            {
                Toast.makeText(getApplicationContext(), "Remove Successful",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Can Not Remove Member",
                        Toast.LENGTH_SHORT).show();
            }
        }

        adapterMembersList.notifyDataSetChanged();
    }

}
