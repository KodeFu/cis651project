package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class CustomList extends ArrayAdapter<String> {
    private final Context context;
    private final String[] names;
    private final String[] somethingElses;
    public CustomList(@NonNull Context context, String[] names, String[] somethingElses) {
        super(context, R.layout.dashboard_list, names); // why names?
        this.context = context;
        this.names = names;
        this.somethingElses = somethingElses;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View linear_layout = inflater.inflate(R.layout.dashboard_list, null, true);
        TextView name = linear_layout.findViewById(R.id.name);
        TextView somethingElse = linear_layout.findViewById(R.id.somethingElse);
        name.setText(names[position]);
        somethingElse.setText(somethingElses[position]);
        return  linear_layout;
    }
}
