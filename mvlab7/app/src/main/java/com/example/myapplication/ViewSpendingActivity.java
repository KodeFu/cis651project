package com.example.myapplication;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

public class ViewSpendingActivity extends BaseActivity
        implements ExpenseListFragment.OnItemSelectedListener {

    ExpenseListFragment expenseListFragment;
    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_spending);
        buildNavDrawerAndToolbar();

        if (savedInstanceState == null) {
            expenseListFragment = new ExpenseListFragment(this);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_container, expenseListFragment).commit();
        }

        twoPane = false;
        if (findViewById(R.id.detail_container) != null) {
            twoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appbar_menu_with_search, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.search_action);

        SearchView searchView = (SearchView)myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                expenseListFragment.getExpenseRecyclerAdapter().getFilter().filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                expenseListFragment.getExpenseRecyclerAdapter().getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }

    public void onClickGetSpendingReport(View view) {
        expenseListFragment.getExpenseList();
    }

    @Override
    public void OnListItemSelected(View sharedView, ExpenseAdapterItem expenseAdapterItem) {
        Bundle args = new Bundle();
        args.putString("id", expenseAdapterItem.getId());
        args.putString("group", expenseAdapterItem.getGroup());
        args.putString("date", expenseAdapterItem.getDate());
        args.putString("name", expenseAdapterItem.getName());
        args.putString("category", expenseAdapterItem.getCategory());
        args.putString("amount", expenseAdapterItem.getAmount());
        args.putString("description", expenseAdapterItem.getDescription());
        args.putString("receipt", expenseAdapterItem.getReceiptPhotoUri());
        Fragment expenseDetailFragment = new ExpenseDetailFragment(this);
        expenseDetailFragment.setArguments(args);

        if (twoPane) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_container, expenseDetailFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, expenseDetailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
