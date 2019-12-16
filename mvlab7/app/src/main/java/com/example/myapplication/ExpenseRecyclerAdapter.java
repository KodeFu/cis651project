package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ExpenseRecyclerAdapter extends RecyclerView.Adapter<ExpenseRecyclerAdapter.ViewHolder>
        implements Filterable {

    private Context context;
    private List<ExpenseAdapterItem> expenseList = new ArrayList<ExpenseAdapterItem>();
    private List<ExpenseAdapterItem> expenseListFiltered = new ArrayList<ExpenseAdapterItem>();
    private ExpenseListFragment.OnItemSelectedListener onListItemClickListener = null;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView card_view;
        public ImageView iv_profile_photo;
        public TextView tv_date;
        public TextView tv_name;
        public TextView tv_category;
        public TextView tv_amount;
        public ViewHolder(View view) {
            super(view);
            card_view = view.findViewById(R.id.card_view);
            iv_profile_photo = view.findViewById(R.id.iv_profile_photo);
            tv_date = view.findViewById(R.id.tv_date);
            tv_name = view.findViewById(R.id.tv_name);
            tv_category = view.findViewById(R.id.tv_category);
            tv_amount = view.findViewById(R.id.tv_amount);
        }
    }

    public ExpenseRecyclerAdapter(Context context, List<ExpenseAdapterItem> objects) {
        this.context = context;
        expenseList = expenseListFiltered = objects;
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

    public ExpenseAdapterItem getItem(int i) {
        return expenseListFiltered.get(i);
    }

    public void setOnListItemClickListener(ExpenseListFragment.OnItemSelectedListener listener) {
        onListItemClickListener = listener;
    }

    @Override
    public ExpenseRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_card_view, parent, false);
        final ViewHolder view_holder = new ViewHolder(v);
        return view_holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Glide.with(context).load(expenseListFiltered.get(position).getProfilePhotoUri()).into(holder.iv_profile_photo);
        holder.tv_date.setText(expenseListFiltered.get(position).getDate());
        holder.tv_name.setText(expenseListFiltered.get(position).getName());
        holder.tv_category.setText(expenseListFiltered.get(position).getCategory());
        holder.tv_amount.setText(expenseListFiltered.get(position).getAmount());

        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onListItemClickListener != null) {
                    onListItemClickListener.OnListItemSelected(
                            view,
                            expenseListFiltered.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseListFiltered.size();
    }
}
