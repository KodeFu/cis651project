package com.example.myapplication;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;


public class ProfileActivity extends BaseActivity {
    private static final int MY_PERMISSIONS_CAMERA = 111;

    EditText displayName;
    ImageView ivProfilePhoto;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    FirebaseStorage storage;
    byte[] profilePhotoByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        buildNavDrawerAndToolbar();

        displayName = findViewById(R.id.et_display_name);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);

        storage = FirebaseStorage.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        displayName.setText(user.getDisplayName());
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
            profilePhotoByteArray = stream.toByteArray();
            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(profilePhotoByteArray,0, profilePhotoByteArray.length);
            ivProfilePhoto.setImageBitmap(compressedBitmap);
        }
    }

    public void onClickSaveProfile(View view) {
        // https://firebase.google.com/docs/auth/android/manage-users
        if (profilePhotoByteArray != null) {
            String path="images/"+ UUID.randomUUID()+".jpg";
            final StorageReference imageRef = storage.getReference(path);
            UploadTask uploadTask=imageRef.putBytes(profilePhotoByteArray);

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

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user!=null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName.getText().toString())
                                    .setPhotoUri(downloadUri)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ProfileActivity.this, "Profile save successful",
                                                Toast.LENGTH_SHORT).show();
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        displayName.setText(user.getDisplayName());
                                    }
                                    else
                                    {
                                        Toast.makeText(ProfileActivity.this, "Profile save failure",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    } else {
                        // Handle failures
                        Toast.makeText(ProfileActivity.this, "Photo upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user!=null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName.getText().toString())
                        .setPhotoUri(null)
                        .build();

                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Profile save successful",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            displayName.setText(user.getDisplayName());
                        }
                        else
                        {
                            Toast.makeText(ProfileActivity.this, "Profile save failure",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
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
