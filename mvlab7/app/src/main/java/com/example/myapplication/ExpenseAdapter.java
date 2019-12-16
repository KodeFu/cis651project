package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends ArrayAdapter<ExpenseAdapterItem> implements Filterable {
    private Context context;
    private List<ExpenseAdapterItem> expenseList = new ArrayList<ExpenseAdapterItem>();
    private List<ExpenseAdapterItem> expenseListFiltered = new ArrayList<ExpenseAdapterItem>();

    public ExpenseAdapter(@NonNull Context context, @NonNull List<ExpenseAdapterItem> objects) {
        super(context, 0, objects);
        this.context = context;
        this.expenseList = objects;
        this.expenseListFiltered = objects;
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.expense_list_item,parent,false);

        ExpenseAdapterItem currentExpense = expenseListFiltered.get(position);

        ImageView image = (ImageView)listItem.findViewById(R.id.iv_profile_photo);
        Glide.with(context).load(currentExpense.getProfilePhotoUri()).into(image);

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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    expenseListFiltered = expenseList;
                } else {
                    List<ExpenseAdapterItem> filteredList = new ArrayList<>();
                    for (ExpenseAdapterItem  expenseAdapterItem : expenseList) {
                        if (expenseAdapterItem.getDescription().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(expenseAdapterItem);
                        }
                    }
                    expenseListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = expenseListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                expenseListFiltered = (List<ExpenseAdapterItem>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getCount() {
        return expenseListFiltered.size();
    }
}
