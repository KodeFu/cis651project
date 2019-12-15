package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    DatabaseReference rootRef;
    HashMap<String, Group> groupsList = new HashMap<String, Group>();
    HashMap<String, Double> monthlySummaryList = new HashMap<String, Double>();

    ListView dashboardList;
    CustomList adapterCategoriesList;
    ArrayList<String> categoryList = new ArrayList<String>();
    ArrayList<String> limitList = new ArrayList<String>();
    ArrayList<String> summaryList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildNavDrawerAndToolbar();

        // Dashboard code
        adapterCategoriesList = new CustomList(this, categoryList, limitList, summaryList);
        dashboardList = findViewById(R.id.dashboard_listview);
        dashboardList.setAdapter(adapterCategoriesList);


        mAuth = FirebaseAuth.getInstance();

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference groupsRef =  rootRef;
        DatabaseReference userRef = rootRef.child("users/" + currentUser.getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.uid = dataSnapshot.getKey();
                Log.d("appdebug", "User query success");

                /*if (u.group != null && !u.group.isEmpty()) {
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
                            Log.d("appdebug", "onCancelled");
                        }
                    });
                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("appdebug", "onCancelled");
            }
        });

        groupsRef.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    Group g = ds.getValue(Group.class);
                    g.token = child;
                    groupsList.put(child, g);

                    Log.d("appdebug", "main addchild: " + child + " " + ds.getValue());
                }


                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH) + 1; // Add one here since calender is zero based

                String myGroup = GroupsHelper.getGroupUserToken(groupsList);
                String path = "/spending/" + myGroup + "/receipts/" + yy +"/" + mm + "/summary/";
                Log.d("appdebug", "db path is " + path);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference dbRef = database.getReference(path);

                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("appdebug", "onDataChange: spending EGADS MAN! Got something!");

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String key = ds.getKey();
                            Number value = (Number) ds.getValue();
                            Double doubleValue;
                            if (value instanceof Long) {
                                doubleValue = ((Long)value).doubleValue();
                            } else {
                                doubleValue = (Double)value;
                            }
                            Log.d("appdebug", "onDataChange: spending key:" + key + " value: " + value);

                            monthlySummaryList.put(key, doubleValue);
                        }

                        updateUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("appdebug", "spending onCancelled");
                    }
                });

                //updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("appdebug", "onCancelled");
            }

        });
    }

    void updateUI()
    {
        Log.d("appdebug", "updateUI");
        Map<String, Category> groupCategoryList = GroupsHelper.getCategories(groupsList);
        categoryList.clear();
        limitList.clear();
        summaryList.clear();

        for (Map.Entry m : groupCategoryList.entrySet())
        {
            String name = ((Category)m.getValue()).displayName;
            String limit = "Unlimited";
            Double limitValue =  Double.valueOf( ((Category)m.getValue()).limit );

            if (limitValue != -1 ) {
                limit = String.format("%.2f", limitValue);
            }


            if (monthlySummaryList.containsKey(name)) {
                Double summaryValue = (Double) monthlySummaryList.get(name);
                String summary = String.format("%.2f", summaryValue);

                categoryList.add(name);
                limitList.add(limit);
                summaryList.add(summary);

                Log.d("appdebug", "updateUI: " + name + " " + summary);
            }
            else
            {
                Log.d("appdebug", "updateUI: KEY NOT CONTAINED" + name);
            }
        }

        adapterCategoriesList.notifyDataSetChanged();
    }

}
