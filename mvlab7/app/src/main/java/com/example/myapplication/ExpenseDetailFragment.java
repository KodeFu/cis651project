package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class ExpenseDetailFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.expense_detail_fragment, container, false);
        TextView date = rootView.findViewById(R.id.expense_date);
        TextView name = rootView.findViewById(R.id.name);
        TextView category = rootView.findViewById(R.id.category);
        TextView amount = rootView.findViewById(R.id.amount);
        TextView description = rootView.findViewById(R.id.description);
        Bundle args = getArguments();
        date.setText(args.getString("date"));
        name.setText(args.getString("name"));
        category.setText(args.getString("category"));
        amount.setText(args.getString("amount"));
        description.setText(args.getString("description"));
        return rootView;
    }
}
