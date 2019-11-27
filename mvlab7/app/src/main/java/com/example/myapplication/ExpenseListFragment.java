package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseListFragment extends Fragment
        implements DatePickerDialog.OnDateSetListener {

    String[] categories = { "All", "Clothing", "Groceries", "Dining", "Ride Share", "Entertainment", "Gifts", "Fuel / Gas", "Automobile", "Home Improvement", "Credit Cards"};

    private Context context;
    private View rootView;
    private List<Expense> expenseList;
    private OnItemSelectedListener clickListener;
    private DatePicker startDate, endDate;

    public ExpenseListFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.expense_list_fragment, container, false);

        ArrayAdapter adapter = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, categories);
        Spinner category = rootView.findViewById(R.id.category);
        category.setAdapter(adapter);

        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);

        TextView startDateTextView = rootView.findViewById(R.id.start_text_view);
        startDateTextView.setText(String.format("%02d", mm) + "/01/" + String.format("%04d", yy));
        startDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH);
                int dd = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), ExpenseListFragment.this, yy, mm, dd);
                startDate = datePickerDialog.getDatePicker();
                datePickerDialog.show();
            }
        });

        TextView endDateTextView = rootView.findViewById(R.id.end_text_view);
        endDateTextView.setText(String.format("%02d", mm) + "/" + String.format("%02d", dd) + "/" + String.format("%04d", yy));
        endDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH);
                int dd = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), ExpenseListFragment.this, yy, mm, dd);
                endDate = datePickerDialog.getDatePicker();
                datePickerDialog.show();
            }
        });

        expenseList = new ArrayList<Expense>();
        expenseList.add(new Expense(R.drawable.profile_photo, "10/01/2019", "Jerry Doe", "Clothing", "$100", "Clothes for school"));
        expenseList.add(new Expense(R.drawable.profile_photo, "10/02/2019", "Mary Doe", "Groceries", "$200", ""));
        expenseList.add(new Expense(R.drawable.profile_photo, "10/03/2019", "Carry Doe", "Dining", "$100", ""));
        expenseList.add(new Expense(R.drawable.profile_photo, "10/04/2019", "Gerry Doe", "Ride Share", "$15", ""));
        expenseList.add(new Expense(R.drawable.profile_photo, "10/05/2019", "Terry Doe", "Entertainment", "$25", ""));
        expenseList.add(new Expense(R.drawable.profile_photo, "10/06/2019", "Larry Doe", "Gifts", "$85", ""));
        expenseList.add(new Expense(R.drawable.profile_photo, "10/07/2019", "Barry Doe", "Fuel / Gas", "$20", ""));
        expenseList.add(new Expense(R.drawable.profile_photo, "10/08/2019", "Harry Doe", "Automobile", "$200", ""));
        expenseList.add(new Expense(R.drawable.profile_photo, "10/09/2019", "Perry Doe", "Home Improvement", "$500", ""));
        ExpenseAdapter expenseAdapter = new ExpenseAdapter(context, expenseList);

        ListView listView  = rootView.findViewById(R.id.list_view);
        listView.setAdapter(expenseAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (clickListener != null) {
                    clickListener.OnListItemSelected(
                            view,
                            expenseList.get(position).getDate(),
                            expenseList.get(position).getName(),
                            expenseList.get(position).getCategory(),
                            expenseList.get(position).getAmount(),
                            expenseList.get(position).getDescription()
                    );
                }
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            clickListener = (OnItemSelectedListener)context;
        } catch (ClassCastException ex) {
            throw new ClassCastException(context.toString() + "must implement OnListItemSelected");
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = String.format("%02d", month) + "/" +
                String.format("%02d", day) + "/" +
                String.format("%04d", year);
        if (view == startDate) {
            final TextView textView = rootView.findViewById(R.id.start_text_view);
            textView.setText(date);
        }
        if (view == endDate) {
            final TextView textView = rootView.findViewById(R.id.end_text_view);
            textView.setText(date);
        }
    }

    public interface  OnItemSelectedListener {
        public void OnListItemSelected(View sharedView, String date, String name, String category, String amount, String description);
    }
}
