package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class CustomList extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> names;
    private final ArrayList<String> limits;
    private final ArrayList<String> summaries;
    public CustomList(@NonNull Context context, ArrayList<String> names, ArrayList<String> limits, ArrayList<String> summaries) {
        super(context, R.layout.dashboard_list, names); // why names?
        this.context = context;
        this.names = names;
        this.limits = limits;
        this.summaries = summaries;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View linear_layout = inflater.inflate(R.layout.dashboard_list, null, true);
        TextView name = linear_layout.findViewById(R.id.name);
        TextView limit = linear_layout.findViewById(R.id.limit);
        TextView summary = linear_layout.findViewById(R.id.summary);
        name.setText(names.get(position));

        if (limits.get(position).equals("Unlimited")) {
            limit.setText(limits.get(position));
        }
        else {
            limit.setText("$ " + limits.get(position));
        }


        summary.setText("$ " + summaries.get(position));

        Double limitDouble = 0.0;
        Double progressDouble = 0.0;
        if (!limits.get(position).equals("Unlimited")) {
            limitDouble = Double.valueOf(limits.get(position));
        }

        if (limitDouble > 0.0) {
            Double summaryDouble = Double.valueOf(summaries.get(position));
            progressDouble = (summaryDouble / limitDouble) * 100.0;
            //Log.d("appdebug", "values " + names.get(position) + " " + summaryDouble + " " + limitDouble + " " + progressDouble);
        }

        int progressInt = progressDouble.intValue();

        ProgressBar progressBar = linear_layout.findViewById(R.id.spending_progress);
        progressBar.setProgress(progressInt);

        return  linear_layout;
    }
}
