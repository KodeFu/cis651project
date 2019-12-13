package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    DatabaseReference mRootReference= FirebaseDatabase.getInstance().getReference();
    DatabaseReference groupsRef =  mRootReference;
    HashMap<String, Group> groupsList = new HashMap<String, Group>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("appdebug", "onCancelled");
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.profile_action:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            case R.id.signout_action:
                mAuth.signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    protected void buildNavDrawerAndToolbar() {
        Toolbar toolbar = findViewById(R.id.common_toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.common_navigation_view);
        drawerLayout = findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.nd_open,
                R.string.nd_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //Toast.makeText(BaseActivity.this, "NavigationDrawer Closed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //Toast.makeText(BaseActivity.this, "NavigationDrawer Opened", Toast.LENGTH_SHORT).show();
            }
        };
        //drawerLayout.setDrawerListener(actionBarDrawerToggle);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.create_group:
                if (!GroupsHelper.isAdmin(groupsList)) {
                    Toast.makeText(getApplicationContext(), "Must be Administrator of the Group", Toast.LENGTH_SHORT).show();
                    break;
                }
                startActivity(new Intent(this, CreateGroupActivity.class));
                break;
            case R.id.administer_group:
                if (!GroupsHelper.isAdmin(groupsList)) {
                    Toast.makeText(getApplicationContext(), "Must be Administrator of the Group", Toast.LENGTH_SHORT).show();
                    break;
                }
                startActivity(new Intent(this, AdministerGroupActivity.class));
                break;
            case R.id.administer_category:
                if (!GroupsHelper.isAdmin(groupsList)) {
                    Toast.makeText(getApplicationContext(), "Must be Administrator of the Group", Toast.LENGTH_SHORT).show();
                    break;
                }
                startActivity(new Intent(this, AdministerCategoryActivity.class));
                break;
            case R.id.join_group:
                if (GroupsHelper.getGroupName(groupsList).equals(""))
                {
                    startActivity(new Intent(this, JoinGroupActivity.class));
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Already In A Group. Leave Group First.",
                        Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.leave_group:
                if (GroupsHelper.removeMember(groupsList, mAuth.getCurrentUser().getUid()) )
                {
                    Toast.makeText(getApplicationContext(), "Remove Successful",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Can Not Remove Member",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.submit_receipt:
                startActivity(new Intent(this, SubmitReceiptActivity.class));
                break;
            case R.id.view_spending:
                startActivity(new Intent(this, ViewSpendingActivity.class));
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
