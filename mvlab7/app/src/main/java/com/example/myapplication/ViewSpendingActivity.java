package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class ViewSpendingActivity extends BaseActivity
        implements DatePickerDialog.OnDateSetListener {

    String[] categories = { "Clothing", "Groceries", "Dining", "Ride Share", "Entertainment", "Gifts", "Fuel / Gas", "Automobile", "Home Improvement", "Credit Cards"};

    DatePicker startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_spending);
        buildNavDrawerAndToolbar();

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categories);
        Spinner category = (Spinner) findViewById(R.id.category);
        category.setAdapter(adapter);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = String.format("%02d", month) + "/" +
                String.format("%02d", day) + "/" +
                String.format("%04d", year);
        if (view == startDate) {
            final TextView textView = findViewById(R.id.start_date_text_view);
            textView.setText(date);
        }
        if (view == endDate) {
            final TextView textView = findViewById(R.id.end_date_text_view);
            textView.setText(date);
        }
    }

    public void onClickPickStartDate(View view) {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, yy, mm, dd);
        startDate = datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }

    public void onClickPickEndDate(View view) {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, yy, mm, dd);
        endDate = datePickerDialog.getDatePicker();
        datePickerDialog.show();
    }
}
