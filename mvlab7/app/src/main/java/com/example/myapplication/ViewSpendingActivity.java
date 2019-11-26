package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class ViewSpendingActivity extends BaseActivity
        implements ExpenseListFragment.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_spending);
        buildNavDrawerAndToolbar();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container, new ExpenseListFragment(this)).commit();
        }
    }

    public void onClickGetSpendingReport(View view) {
        Toast.makeText(getApplicationContext(), "onClickGetSpendingReport",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnListItemSelected(View sharedView, String date, String name, String category, String amount, String description) {
        Bundle args = new Bundle();
        args.putString("date", date);
        args.putString("name", name);
        args.putString("category", category);
        args.putString("amount", amount);
        args.putString("description", description);
        Fragment expenseDetailFragment = new ExpenseDetailFragment();
        expenseDetailFragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, expenseDetailFragment)
                .addToBackStack(null)
                .commit();
    }
}
