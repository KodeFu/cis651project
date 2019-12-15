package com.example.myapplication;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdministerCategoryActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    HashMap<String, Group> groupsList = new HashMap<String, Group>();
    ArrayList<String> categoryList = new ArrayList<String>();
    ArrayAdapter adapterCategoriesList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer_category);
        buildNavDrawerAndToolbar();

        mAuth = FirebaseAuth.getInstance();

        adapterCategoriesList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categoryList);
        final Spinner category = (Spinner) findViewById(R.id.category);
        category.setAdapter(adapterCategoriesList);

        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                Log.d("appdebug", "updateUI: onItemSelected position: " + position);

                Category c = GroupsHelper.getCategory(groupsList, categoryList.get(position));
                Log.d("appdebug", "updateUI: onItemSelected " + c.displayName);

                if (c!=null) {
                    EditText limit = findViewById(R.id.limit);

                    if (c.limit == -1 ) {
                        limit.setText("No Limit");
                    } else {
                        limit.setText(c.limit.toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
                Log.d("appdebug", "updateUI: onItemSelected position: " + "nothing changed");
            }

        });

        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("appdebug", "onDataChange: ");
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

        Map<String, Category> groupCategoryList = GroupsHelper.getCategories(groupsList);
        categoryList.clear();

        for (Map.Entry m : groupCategoryList.entrySet())
        {
            categoryList.add(((Category)m.getValue()).displayName);
        }
        Spinner categorySpinner = findViewById(R.id.category);
        adapterCategoriesList.notifyDataSetChanged();

        TextView token = findViewById(R.id.token);
        token.setText(GroupsHelper.getGroupToken(groupsList));
    }

    public void onClickCategoryAdd(View view) {
        EditText categoryName = findViewById(R.id.new_category);
        if (!categoryName.getText().toString().equals("")) {
            GroupsHelper.addCategory(groupsList, categoryName.getText().toString());

            Toast.makeText(getApplicationContext(), "Category Added", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Can Not Add Category", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickCategoryDelete(View view) {
        String categoryName = "";
        Spinner categorySpinner = findViewById(R.id.category);
        String selectedItem = categorySpinner.getSelectedItem().toString();

        Log.d("appdebug", "onClickCategoryDelete: " + selectedItem);

        Map<String, Category> groupCategoryList = GroupsHelper.getCategories(groupsList);

        for (Map.Entry m : groupCategoryList.entrySet()) {
            if (((Category)m.getValue()).displayName.equals(selectedItem)) {
                categoryName = m.getKey().toString();
            }
        }

        if (!categoryName.equals("")) {
            if (GroupsHelper.removeCategory(groupsList, categoryName))
            {
                Toast.makeText(getApplicationContext(), "Remove Successful",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Can Not Remove Category",
                        Toast.LENGTH_SHORT).show();
            }

        }

        /*if (adapterCategoriesList.getCount()>=0) {
            categorySpinner.setSelection(0);
            categorySpinner.set
        }*/

        adapterCategoriesList.notifyDataSetChanged();
    }

    public void onClickCategoryUpdate(View view)
    {
        String categoryName = "";
        Category category = new Category();

        Spinner categorySpinner = findViewById(R.id.category);

        if (categorySpinner.getSelectedItem() == null)
        {
            return;
        }

        String selectedItem = categorySpinner.getSelectedItem().toString();

        EditText limit = findViewById(R.id.limit);
        if (limit.getText().toString().equals("No Limit"))
        {
            category.limit = -1;
        }
        else
        {
            category.limit = Integer.parseInt( limit.getText().toString() );
        }

        Map<String, Category> groupCategoryList = GroupsHelper.getCategories(groupsList);

        for (Map.Entry m : groupCategoryList.entrySet()) {
            if (((Category)m.getValue()).displayName.equals(selectedItem)) {
                categoryName = m.getKey().toString();
            }
        }

        if (GroupsHelper.updateCategory(groupsList, categoryName, category)) {
            Toast.makeText(getApplicationContext(), "Update Successful", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Could Not Update Category", Toast.LENGTH_SHORT).show();
        }

        adapterCategoriesList.notifyDataSetChanged();
    }
}
