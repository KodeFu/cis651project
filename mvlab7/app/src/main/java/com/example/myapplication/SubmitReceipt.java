package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SubmitReceipt extends AppCompatActivity {
    String[] categories = { "Clothing", "Groceries", "Dining", "Ride Share", "Entertainment", "Gifts", "Fuel / Gas", "Automobile", "Home Improvement", "Credit Cards"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_receipt);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categories);
        Spinner category = (Spinner) findViewById(R.id.category);
        category.setAdapter(adapter);
    }
}
