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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.UUID;

public class SubmitReceiptActivity extends BaseActivity
        implements DatePickerDialog.OnDateSetListener {
    String[] categories = { "Clothing", "Groceries", "Dining", "Ride Share", "Entertainment", "Gifts", "Fuel / Gas", "Automobile", "Home Improvement", "Credit Cards"};

    private static final int MY_PERMISSIONS_CAMERA = 111;

    ImageView ivReceiptPhoto;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    FirebaseStorage storage;
    byte[] receiptPhotoByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_receipt);
        buildNavDrawerAndToolbar();

        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);

        TextView expenseDateTextView = findViewById(R.id.expense_date);
        expenseDateTextView.setText(mm + "/" + dd + "/" + yy);
        expenseDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int yy = calendar.get(Calendar.YEAR);
                int mm = calendar.get(Calendar.MONTH);
                int dd = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(SubmitReceiptActivity.this, SubmitReceiptActivity.this, yy, mm, dd);
                datePickerDialog.show();
            }
        });

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, categories);
        Spinner category = (Spinner) findViewById(R.id.category);
        category.setAdapter(adapter);

        ivReceiptPhoto = findViewById(R.id.iv_receipt);

        storage = FirebaseStorage.getInstance();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = String.format("%02d", month) + "/" +
                String.format("%02d", day) + "/" +
                String.format("%04d", year);
        final TextView textView = findViewById(R.id.expense_date);
        textView.setText(date);
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
}
