package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

public class SubmitReceiptActivity extends BaseActivity
        implements DatePickerDialog.OnDateSetListener {

    private String userDisplayName;

    private static final int MY_PERMISSIONS_CAMERA = 111;

    TextView expenseDateTextView;
    Spinner categorySpinner;
    EditText amountEditText;
    EditText descriptionEditText;
    ImageView ivReceiptPhoto;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    FirebaseStorage storage;
    byte[] receiptPhotoByteArray;

    //HashMap<String, Group> groupsList = new HashMap<String, Group>();
    ArrayList<String> categoryList = new ArrayList<String>();
    ArrayAdapter adapterCategoriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_receipt);
        buildNavDrawerAndToolbar();

        expenseDateTextView = findViewById(R.id.expense_date);
        categorySpinner = findViewById(R.id.category);
        amountEditText = findViewById(R.id.amount);
        descriptionEditText = findViewById(R.id.et_description);
        ivReceiptPhoto = findViewById(R.id.iv_receipt);

        userDisplayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        adapterCategoriesList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categoryList);
        categorySpinner.setAdapter(adapterCategoriesList);

        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH) + 1; // Add one here since calender is zero based
        int dd = calendar.get(Calendar.DAY_OF_MONTH);

        expenseDateTextView.setText(mm + "/" + dd + "/" + yy);
        expenseDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH); // Don't add one here, datePcikerDialog handles it (it seems)
                int dd = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(SubmitReceiptActivity.this, SubmitReceiptActivity.this, yy, mm, dd);
                datePickerDialog.show();

            }
        });

        storage = FirebaseStorage.getInstance();

        groupsRef.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("appdebug", "onDataChange: ");

                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH) + 1; // Add one here since calender is zero based
                int dd = calendar.get(Calendar.DAY_OF_MONTH);

                String myGroup = GroupsHelper.getGroupUserToken(groupsList);
                String path = "/spending/" + myGroup + "/receipts/" + yy +"/" + mm + "/summary/";
                Log.d("appdebug", "db path is " + path);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference dbRef = database.getReference(path);

                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("appdebug", "onDataChange: spending EGADS MAN! Got something!");

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String key = ds.getKey();
                            Number value = (Number) ds.getValue();
                            Log.d("appdebug", "onDataChange: spending: " + key);
                            Log.d("appdebug", "onDataChange: spending: " + value);
                            Log.d("appdebug", "onDataChange: spending: " + ds.getValue() + " " + ds.getValue().getClass());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("appdebug", "spending onCancelled");
                    }
                });

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("appdebug", "onCancelled");
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = String.format("%02d", month) + "/" +
                String.format("%02d", day) + "/" +
                String.format("%04d", year);
        expenseDateTextView.setText(date);
    }

    public void onClickTakePhoto(View view) {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    MY_PERMISSIONS_CAMERA);
        } else {
            takePhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    takePhoto();
                } else {
                    Toast.makeText(this,"Need Permission to use Camera",Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void takePhoto() {
        Intent takePicturelntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicturelntent.resolveActivity(getPackageManager()) !=  null) {
            startActivityForResult(takePicturelntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap)extras.get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            receiptPhotoByteArray = stream.toByteArray();
            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(receiptPhotoByteArray,0, receiptPhotoByteArray.length);
            ivReceiptPhoto.setImageBitmap(compressedBitmap);
        }
    }

    public void onClickSubmitReceipt(View view) {
        // https://firebase.google.com/docs/auth/android/manage-users
        if (receiptPhotoByteArray != null) {
            String path="images/"+ UUID.randomUUID()+".jpg";
            final StorageReference imageRef = storage.getReference(path);
            UploadTask uploadTask=imageRef.putBytes(receiptPhotoByteArray);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.d("appdebug", downloadUri.toString());

                        // TODO: add receipt under spending in database
                        String categoryName = categorySpinner.getSelectedItem().toString();
                        Log.d("appdebug", "spending: category name is  " +  categoryName);

                        final Calendar calendar = Calendar.getInstance();
                        int yy = calendar.get(Calendar.YEAR);
                        int mm = calendar.get(Calendar.MONTH) + 1;
                        int dd = calendar.get(Calendar.DAY_OF_MONTH);

                        String myGroup = GroupsHelper.getGroupUserToken(groupsList);
                        Log.d("appdebug", "spending: my group is: " +  myGroup);

                        Receipt receipt = new Receipt();
                        receipt.amount = Double.parseDouble(amountEditText.getText().toString().trim());
                        receipt.category = categoryName;
                        receipt.description = descriptionEditText.getText().toString();
                        receipt.receipt = downloadUri.toString();

                        Long day = Long.parseLong(expenseDateTextView.getText().toString().substring(3, 5));
                        Long month = Long.parseLong(expenseDateTextView.getText().toString().substring(0, 2));
                        Long year = Long.parseLong(expenseDateTextView.getText().toString().substring(6, 10));
                        receipt.date = (year * 10000) + (month * 100) + day;

                        receipt.user = userDisplayName;

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference dbRef = database.getReference(
                                "/spending/" + myGroup + "/receipts/" + yy +"/" + mm + "/detail/" + categoryName );
                        dbRef.push().setValue(receipt);

                        Toast.makeText(SubmitReceiptActivity.this, "Receipt submit successful",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle failures
                        Toast.makeText(SubmitReceiptActivity.this, "Photo upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Must provide photo of receipt",
                    Toast.LENGTH_SHORT).show();
        }
    }

    final static class WorkerDownloadImage extends AsyncTask<String, String, Bitmap> {
        private final WeakReference<Context> parentRef;
        private final WeakReference<ImageView> imageViewRef;

        public  WorkerDownloadImage(final Context parent, final ImageView imageview)
        {
            parentRef=new WeakReference<Context>(parent);
            imageViewRef=new WeakReference<ImageView>(imageview);
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap result= HTTP_METHODS.downloadImageUsingHTTPGetRequest(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(final Bitmap result){
            final ImageView iv=imageViewRef.get();
            if(iv!=null)
            {
                iv.setImageBitmap(result);
            }
        }
    }

    public void updateUI()
    {
        Log.d("appdebug", "updateUI: ");

        Map<String, Category> groupCategoryList = GroupsHelper.getCategories(groupsList);
        categoryList.clear();

        for (Map.Entry m : groupCategoryList.entrySet())
        {
            categoryList.add(((Category)m.getValue()).displayName);
        }
        adapterCategoriesList.notifyDataSetChanged();
    }
}
