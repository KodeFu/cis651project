package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdministerCategoryActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    DatabaseReference mRootReference= FirebaseDatabase.getInstance().getReference();
    DatabaseReference groupsRef =  mRootReference;
    HashMap<String, Group> groupsList = new HashMap<String, Group>();
    String[] categories = { "Clothing", "Groceries", "Dining", "Ride Share", "Entertainment", "Gifts", "Fuel / Gas", "Automobile", "Home Improvement", "Credit Cards"};
    ArrayAdapter adapterMembersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer_category);
        buildNavDrawerAndToolbar();

        mAuth = FirebaseAuth.getInstance();

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categories);
        Spinner category = (Spinner) findViewById(R.id.category);
        category.setAdapter(adapter);

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
/*
        EditText groupName = findViewById(R.id.group_name);
        groupName.setText(GroupsHelper.getGroupName(groupsList));

        Map<String, Member> groupMemberList = GroupsHelper.getMembers(groupsList);
        membersList.clear();

        for (Map.Entry m : groupMemberList.entrySet()) {
            membersList.add(((Member)m.getValue()).displayName);
        }
        Spinner m = findViewById(R.id.members);
        adapterMembersList.notifyDataSetChanged();
 */
    }

    public void onClickCategoryDelete(View view) {
        Toast.makeText(getApplicationContext(), "onClickCategoryDelete",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickCategoryUpdate(View view) {
        Toast.makeText(getApplicationContext(), "onClickCategoryUpdate",
                Toast.LENGTH_SHORT).show();
    }


    public void onClickCategoryAdd(View view) {
        Toast.makeText(getApplicationContext(), "onClickCategoryAdd",
                Toast.LENGTH_SHORT).show();
    }
}
