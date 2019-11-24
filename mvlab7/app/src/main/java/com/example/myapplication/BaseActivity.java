package com.example.myapplication;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.profile_action:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    protected void buildNavDrawerAndToolbar() {
        toolbar = findViewById(R.id.common_toolbar);
        setSupportActionBar(toolbar);
        navigationView = findViewById(R.id.common_navigation_view);
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
                startActivity(new Intent(this, CreateGroupActivity.class));
                break;
            case R.id.administer_group:
                startActivity(new Intent(this, AdministerGroupActivity.class));
                break;
            case R.id.join_group:
                startActivity(new Intent(this, JoinGroupActivity.class));
                break;
            case R.id.leave_group:
                Toast.makeText(getApplicationContext(), "Leave Group",
                        Toast.LENGTH_SHORT).show();
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
