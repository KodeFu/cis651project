package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class AdministerGroup extends AppCompatActivity {
    String[] categories = { "Clothing", "Groceries", "Dining", "Ride Share", "Entertainment", "Gifts", "Fuel / Gas", "Automobile", "Home Improvement", "Credit Cards"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer_group);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categories);

        Spinner category = (Spinner) findViewById(R.id.category);
        category.setAdapter(adapter);

    }

    public void OnClickGroupDelete(View view) {
        Toast.makeText(getApplicationContext(), "OnClickGroupDelete",
                Toast.LENGTH_SHORT).show();
    }

    public void OnClickGroupUpdate(View view) {
        Toast.makeText(getApplicationContext(), "OnClickGroupUpdate",
                Toast.LENGTH_SHORT).show();
    }

    public void OnClickCategoryDelete(View view) {
        Toast.makeText(getApplicationContext(), "OnClickCategoryDelete",
                Toast.LENGTH_SHORT).show();
    }

    public void OnClickCategoryUpdate(View view) {
        Toast.makeText(getApplicationContext(), "OnClickCategoryUpdate",
                Toast.LENGTH_SHORT).show();
    }
}
