package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class SubmitReceiptActivity extends BaseActivity
        implements DatePickerDialog.OnDateSetListener {
    String[] categories = { "Clothing", "Groceries", "Dining", "Ride Share", "Entertainment", "Gifts", "Fuel / Gas", "Automobile", "Home Improvement", "Credit Cards"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_receipt);
        buildNavDrawerAndToolbar();

        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);

        TextView expenseDateTextView = findViewById(R.id.expense_date);
        expenseDateTextView.setText(mm + "/" + dd + "/" + yy);
        expenseDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH);
                int dd = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(SubmitReceiptActivity.this, SubmitReceiptActivity.this, yy, mm, dd);
                datePickerDialog.show();
            }
        });

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categories);
        Spinner category = (Spinner) findViewById(R.id.category);
        category.setAdapter(adapter);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = String.format("%02d", month) + "/" +
                String.format("%02d", day) + "/" +
                String.format("%04d", year);
        final TextView textView = findViewById(R.id.expense_date);
        textView.setText(date);
    }

    public void onClickUploadPhoto(View view) {
        Toast.makeText(getApplicationContext(), "onClickUploadPhoto",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickSubmitReceipt(View view) {
        Toast.makeText(getApplicationContext(), "onClickSubmitReceipt",
                Toast.LENGTH_SHORT).show();
    }
}
