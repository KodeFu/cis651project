package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseListFragment extends Fragment
        implements DatePickerDialog.OnDateSetListener {

    private boolean initialized = false;

    private FirebaseAuth mAuth;
    DatabaseReference groupsRef;
    DatabaseReference spendingRef;

    HashMap<String, Group> groupsList = new HashMap<String, Group>();
    ArrayList<String> categoryList = new ArrayList<String>();
    ArrayAdapter adapterCategoriesList;
    ArrayList<String> membersList = new ArrayList<String>();
    ArrayAdapter adapterMembersList;

    private Context context;
    private View rootView;
    private List<ExpenseAdapterItem> expenseList;
    private OnItemSelectedListener clickListener;
    private DatePicker startDate, endDate;

    public ExpenseListFragment(Context context) {
        this.context = context;
    }

    private ExpenseAdapter expenseAdapter;

    public ExpenseAdapter getExpenseAdapter() {
        return expenseAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.expense_list_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();

        groupsRef = FirebaseDatabase.getInstance().getReference("/groups");
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String child = ds.getKey();
                    Group g = ds.getValue(Group.class);
                    g.token = child;
                    groupsList.put(child, g);

                    Log.d("appdebug", "onChildAdded: " + child + " " + ds.getValue());
                }

                updateUI();

                if (!initialized) {
                    initialized = true;

                    String groupUserToken = GroupsHelper.getGroupUserToken(groupsList);

                    if (!groupUserToken.isEmpty()) {
                        spendingRef = FirebaseDatabase.getInstance().getReference("/spending/" + groupUserToken);
                        spendingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Spending s = dataSnapshot.getValue(Spending.class);
                                s.token = dataSnapshot.getKey();
                                Log.d("appdebug", "spendingRef.addListenerForSingleValueEvent get Spending success");

                                for (Map.Entry y : s.receipts.entrySet()) {
                                    Map<String, MonthlyReceipts> mapMonthlyReceipts = (Map<String, MonthlyReceipts>)y.getValue();
                                    for (Map.Entry mr : mapMonthlyReceipts.entrySet()) {
                                        MonthlyReceipts monthlyReceipts = (MonthlyReceipts)mr.getValue();
                                        for (Map.Entry c : monthlyReceipts.detail.entrySet()) {
                                            Map<String, Receipt> mapReceipts = (Map<String, Receipt>)c.getValue();
                                            for (Map.Entry r : mapReceipts.entrySet()) {
                                                Receipt receipt = (Receipt)r.getValue();
                                                String dateString = String.valueOf(receipt.date);
                                                NumberFormat nf = NumberFormat.getInstance();
                                                nf.setMaximumFractionDigits(2);
                                                nf.setMinimumFractionDigits(2);
                                                String amountString = nf.format(receipt.amount);
                                                expenseList.add(
                                                        new ExpenseAdapterItem(
                                                                R.drawable.profile_photo,
                                                                dateString.substring(4, 6) + "/" + dateString.substring(6, 8) + "/" + dateString.substring(0, 4),
                                                                receipt.user,
                                                                receipt.category,
                                                                amountString,
                                                                receipt.description
                                                        )
                                                );
                                            }
                                        }
                                    }
                                }
                                Collections.sort(expenseList, new Comparator<ExpenseAdapterItem>() {
                                    @Override
                                    public int compare(ExpenseAdapterItem o1, ExpenseAdapterItem o2) {
                                        Long day1 = Long.parseLong(o1.getDate().substring(3, 5));
                                        Long month1 = Long.parseLong(o1.getDate().substring(0, 2));
                                        Long year1 = Long.parseLong(o1.getDate().substring(6, 10));
                                        Long l1 = (year1 * 10000) + (month1 * 100) + day1;

                                        Long day2 = Long.parseLong(o2.getDate().substring(3, 5));
                                        Long month2 = Long.parseLong(o2.getDate().substring(0, 2));
                                        Long year2 = Long.parseLong(o2.getDate().substring(6, 10));
                                        Long l2 = (year2 * 10000) + (month2 * 100) + day2;

                                        if (l1 < l2) return -1;
                                        if (l1 > l2) return 1;
                                        return 0;
                                    }
                                });
                                expenseAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("appdebug", "onCancelled");
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("appdebug", "onCancelled");
            }
        });

        adapterCategoriesList = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, categoryList);
        Spinner category = rootView.findViewById(R.id.category);
        category.setAdapter(adapterCategoriesList);

        adapterMembersList = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, membersList);
        Spinner members = rootView.findViewById(R.id.members);
        members.setAdapter(adapterMembersList);

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

        expenseList = new ArrayList<ExpenseAdapterItem>();
        expenseAdapter = new ExpenseAdapter(context, expenseList);

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

    public void updateUI()
    {
        Log.d("appdebug", "updateUI: ");

        Map<String, Category> groupCategoryList = GroupsHelper.getCategories(groupsList);
        categoryList.clear();

        categoryList.add("All");
        for (Map.Entry m : groupCategoryList.entrySet())
        {
            categoryList.add(((Category)m.getValue()).displayName);
        }
        Spinner categorySpinner = rootView.findViewById(R.id.category);
        adapterCategoriesList.notifyDataSetChanged();

        Map<String, Member> groupMemberList = GroupsHelper.getMembers(groupsList);
        membersList.clear();

        membersList.add("All");
        for (Map.Entry m : groupMemberList.entrySet()) {
            membersList.add(((Member)m.getValue()).displayName);
        }
        Spinner m = rootView.findViewById(R.id.members);
        adapterMembersList.notifyDataSetChanged();
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
