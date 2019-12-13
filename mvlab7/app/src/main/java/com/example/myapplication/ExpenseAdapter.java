package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends ArrayAdapter<ExpenseAdapterItem> {
    private Context context;
    private List<ExpenseAdapterItem> expenseList = new ArrayList<ExpenseAdapterItem>();

    public ExpenseAdapter(@NonNull Context context, @NonNull List<ExpenseAdapterItem> objects) {
        super(context, 0, objects);
        this.context = context;
        this.expenseList = objects;
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.expense_list_item,parent,false);

        ExpenseAdapterItem currentExpense = expenseList.get(position);

        ImageView image = (ImageView)listItem.findViewById(R.id.iv_profile_photo);
        image.setImageResource(currentExpense.getProfilePhoto());

        TextView date = (TextView) listItem.findViewById(R.id.tv_date);
        date.setText(currentExpense.getDate());

        TextView name = (TextView) listItem.findViewById(R.id.tv_name);
        name.setText(currentExpense.getName());

        TextView category = (TextView) listItem.findViewById(R.id.tv_category);
        category.setText(currentExpense.getCategory());

        TextView amount = (TextView) listItem.findViewById(R.id.tv_amount);
        amount.setText(currentExpense.getAmount());

        return listItem;
    }
}
