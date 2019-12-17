package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExpenseListFragment extends Fragment
        implements DatePickerDialog.OnDateSetListener {

    private ViewGroup container;

    DatabaseReference rootRef;
    DatabaseReference groupsRef;
    DatabaseReference usersRef;
    DatabaseReference spendingRef;

    HashMap<String, Group> groupsList = new HashMap<String, Group>();
    ArrayList<String> categoryList = new ArrayList<String>();
    ArrayAdapter adapterCategoriesList;
    ArrayList<String> membersList = new ArrayList<String>();
    ArrayAdapter adapterMembersList;
    HashMap<String, User> usersMap = new HashMap<String, User>();

    private Context context;
    private View rootView;
    private Spinner category;
    private Spinner members;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private RecyclerView recyclerView;
    private List<ExpenseAdapterItem> expenseList;
    private OnItemSelectedListener clickListener;
    private DatePicker startDate, endDate;

    public ExpenseListFragment(Context context) {
        this.context = context;
    }

    private ExpenseRecyclerAdapter expenseRecyclerAdapter;

    public ExpenseRecyclerAdapter getExpenseRecyclerAdapter() {
        return expenseRecyclerAdapter;
    }

    ReceiptDeleteCallback receiptDeleteCallback = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        this.container = container;

        rootView = inflater.inflate(R.layout.expense_list_fragment, container, false);

        rootRef = FirebaseDatabase.getInstance().getReference();

        adapterCategoriesList = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, categoryList);
        category = rootView.findViewById(R.id.category);
        category.setAdapter(adapterCategoriesList);

        adapterMembersList = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, membersList);
        members = rootView.findViewById(R.id.members);
        members.setAdapter(adapterMembersList);

        startDateEditText = rootView.findViewById(R.id.start_edit_text);
        endDateEditText = rootView.findViewById(R.id.end_edit_text);

        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH) + 1;
        int dd = calendar.get(Calendar.DAY_OF_MONTH);

        startDateEditText.setText(String.format("%02d", mm) + "/01/" + String.format("%04d", yy));
        endDateEditText.setText(String.format("%02d", mm) + "/" + String.format("%02d", dd) + "/" + String.format("%04d", yy));
        startDateEditText.setFocusable(false);
        endDateEditText.setFocusable(false);

        startDateEditText.setOnClickListener(new View.OnClickListener() {
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

        endDateEditText.setOnClickListener(new View.OnClickListener() {
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
        expenseRecyclerAdapter = new ExpenseRecyclerAdapter(context, expenseList);

        recyclerView  = rootView.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(expenseRecyclerAdapter);

        try {
            OnItemSelectedListener parent = (OnItemSelectedListener)getActivity();
            expenseRecyclerAdapter.setOnListItemClickListener(parent);
        } catch (ClassCastException ex) {
            throw new ClassCastException(container.toString() + "must implement OnListItemSelected");
        }

        // https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28
        // https://github.com/FanFataL/swipe-controller-demo
        receiptDeleteCallback = new ReceiptDeleteCallback(new ReceiptDeleteCallbackActions() {
            @Override
            public void onRightClicked(int position) {
                ExpenseAdapterItem expenseAdapterItem = expenseRecyclerAdapter.getItem(position);
                String month = expenseAdapterItem.getDate().substring(0, 2);
                String year = expenseAdapterItem.getDate().substring(6, 10);

                DatabaseReference receiptRef =
                        rootRef
                        .child("spending")
                        .child(expenseAdapterItem.getGroup())
                        .child("receipts")
                        .child(year)
                        .child(month)
                        .child("detail")
                        .child(expenseAdapterItem.getCategory())
                        .child(expenseAdapterItem.getId());
                receiptRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Delete successful",
                                    Toast.LENGTH_SHORT).show();
                            getExpenseList();
                        } else {
                            Toast.makeText(context, "Unable to delete receipt",
                                    Toast.LENGTH_SHORT).show();
                            if (task.getException() != null) {
                                Toast.makeText(context, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(receiptDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                receiptDeleteCallback.onDraw(c);
            }
        });

        getExpenseList();

        return rootView;
    }

    public void getExpenseList() {
        expenseList.clear();
        
        groupsRef = FirebaseDatabase.getInstance().getReference("/groups");
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

                usersRef = FirebaseDatabase.getInstance().getReference("/users");
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            User u = child.getValue(User.class);
                            u.uid = child.getKey();
                            usersMap.put(u.uid, u);
                        }

                        final String groupUserToken = GroupsHelper.getGroupUserToken(groupsList);

                        if (!groupUserToken.isEmpty()) {
                            spendingRef = FirebaseDatabase.getInstance().getReference("/spending/" + groupUserToken);
                            spendingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        final Spending s = dataSnapshot.getValue(Spending.class);
                                        s.token = dataSnapshot.getKey();
                                        Log.d("appdebug", "spendingRef.addListenerForSingleValueEvent get Spending success");

                                        if (s.receipts != null) {
                                            for (Map.Entry y : s.receipts.entrySet()) {
                                                Map<String, MonthlyReceipts> mapMonthlyReceipts = (Map<String, MonthlyReceipts>)y.getValue();
                                                if (mapMonthlyReceipts != null) {
                                                    for (Map.Entry mr : mapMonthlyReceipts.entrySet()) {
                                                        MonthlyReceipts monthlyReceipts = (MonthlyReceipts)mr.getValue();
                                                        if (monthlyReceipts.detail != null) {
                                                            for (Map.Entry c : monthlyReceipts.detail.entrySet()) {
                                                                Map<String, Receipt> mapReceipts = (Map<String, Receipt>)c.getValue();
                                                                if (mapReceipts != null) {
                                                                    for (Map.Entry r : mapReceipts.entrySet()) {
                                                                        Receipt receipt = (Receipt)r.getValue();
                                                                        receipt.id = (String)r.getKey();
                                                                        receipt.group = (String)groupUserToken;
                                                                        String dateString = String.valueOf(receipt.date);
                                                                        NumberFormat nf = NumberFormat.getInstance();
                                                                        nf.setMaximumFractionDigits(2);
                                                                        nf.setMinimumFractionDigits(2);
                                                                        String amountString = nf.format(receipt.amount);
                                                                        expenseList.add(
                                                                                new ExpenseAdapterItem(
                                                                                        receipt.id,
                                                                                        receipt.group,
                                                                                        usersMap.get(receipt.userUid).profilePhotoUri,
                                                                                        dateString.substring(4, 6) + "/" + dateString.substring(6, 8) + "/" + dateString.substring(0, 4),
                                                                                        usersMap.get(receipt.userUid).displayName,
                                                                                        receipt.category,
                                                                                        amountString,
                                                                                        receipt.description,
                                                                                        receipt.receipt
                                                                                )
                                                                        );
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Iterator<ExpenseAdapterItem> i = expenseList.iterator();
                                        while (i.hasNext()) {
                                            ExpenseAdapterItem expenseAdapterItem = i.next();
                                            String categorySelected = category.getSelectedItem().toString();
                                            String memberSelected = members.getSelectedItem().toString();

                                            Long startDay = Long.parseLong(startDateEditText.getText().toString().substring(3, 5));
                                            Long startMonth = Long.parseLong(startDateEditText.getText().toString().substring(0, 2));
                                            Long startYear = Long.parseLong(startDateEditText.getText().toString().substring(6, 10));
                                            Long startDate = (startYear * 10000) + (startMonth * 100) + startDay;

                                            Long endDay = Long.parseLong(endDateEditText.getText().toString().substring(3, 5));
                                            Long endMonth = Long.parseLong(endDateEditText.getText().toString().substring(0, 2));
                                            Long endYear = Long.parseLong(endDateEditText.getText().toString().substring(6, 10));
                                            Long endDate = (endYear * 10000) + (endMonth * 100) + endDay;

                                            Long receiptDay = Long.parseLong(expenseAdapterItem.getDate().substring(3, 5));
                                            Long receiptMonth = Long.parseLong(expenseAdapterItem.getDate().substring(0, 2));
                                            Long receiptYear = Long.parseLong(expenseAdapterItem.getDate().substring(6, 10));
                                            Long receiptDate = (receiptYear * 10000) + (receiptMonth * 100) + receiptDay;

                                            if (!(categorySelected.equals("All") || categorySelected.equals(expenseAdapterItem.getCategory())) ||
                                                    !(memberSelected.equals("All") || memberSelected.equals(expenseAdapterItem.getName())) ||
                                                    !(receiptDate >= startDate && receiptDate <= endDate)) {
                                                i.remove();
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
                                        expenseRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("appdebug", "onCancelled");
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("appdebug", "onCancelled");
            }
        });
    }

    private void updateUI()
    {
        Log.d("appdebug", "updateUI: ");

        Map<String, Category> groupCategoryList = GroupsHelper.getCategories(groupsList);
        categoryList.clear();

        categoryList.add("All");
        if (groupCategoryList != null) {
            for (Map.Entry m : groupCategoryList.entrySet())
            {
                categoryList.add(((Category)m.getValue()).displayName);
            }
        }
        Spinner categorySpinner = rootView.findViewById(R.id.category);
        adapterCategoriesList.notifyDataSetChanged();

        Map<String, Member> groupMemberList = GroupsHelper.getMembers(groupsList);
        membersList.clear();

        membersList.add("All");
        if (groupMemberList != null) {
            for (Map.Entry m : groupMemberList.entrySet()) {
                membersList.add(((Member)m.getValue()).displayName);
            }
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
        String date = String.format("%02d", month + 1) + "/" +
                String.format("%02d", day) + "/" +
                String.format("%04d", year);
        if (view == startDate) {
            final TextView textView = rootView.findViewById(R.id.start_edit_text);
            textView.setText(date);
        }
        if (view == endDate) {
            final TextView textView = rootView.findViewById(R.id.end_edit_text);
            textView.setText(date);
        }
    }

    public interface  OnItemSelectedListener {
        public void OnListItemSelected(View sharedView, ExpenseAdapterItem expenseAdapterItem);
    }
}
