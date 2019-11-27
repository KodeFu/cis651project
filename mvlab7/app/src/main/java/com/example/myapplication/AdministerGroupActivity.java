package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class AdministerGroupActivity extends BaseActivity {
    String[] categories = { "Clothing", "Groceries", "Dining", "Ride Share", "Entertainment", "Gifts", "Fuel / Gas", "Automobile", "Home Improvement", "Credit Cards"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administer_group);
        buildNavDrawerAndToolbar();

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categories);

        Spinner category = (Spinner) findViewById(R.id.category);
        category.setAdapter(adapter);

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
}
