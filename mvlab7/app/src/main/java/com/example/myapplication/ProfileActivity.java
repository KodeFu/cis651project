package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class ProfileActivity extends BaseActivity {
    private static final int MY_PERMISSIONS_CAMERA = 111;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    EditText email;
    EditText password;
    EditText displayName;
    ImageView ivProfilePhoto;

    FirebaseUser currentUser;
    FirebaseStorage storage;
    byte[] profilePhotoByteArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        buildNavDrawerAndToolbar();

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        displayName = findViewById(R.id.et_display_name);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        DatabaseReference usersRef = mRootReference.child("users").child(currentUser.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                u.uid = dataSnapshot.getKey();
                email.setText(u.email);
                displayName.setText(u.displayName);
                Glide.with(ProfileActivity.this).load(u.profilePhotoUri).into(ivProfilePhoto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
            Glide.with(this).clear(ivProfilePhoto);
            ivProfilePhoto.setImageBitmap(compressedBitmap);
        }
    }

    public void onClickSaveProfile(View view) {
        // https://firebase.google.com/docs/auth/android/manage-users
        final String emailString = email.getText().toString().trim();
        final String passwordString = password.getText().toString().trim();
        final String displayNameString = displayName.getText().toString().trim();

        if (emailString.isEmpty()
                || displayNameString.isEmpty()) {
            Toast.makeText(ProfileActivity.this, "Email and DisplayName must be provided",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (profilePhotoByteArray != null) {
            String path="images/"+ UUID.randomUUID()+".jpg";
            final StorageReference imageRef = storage.getReference(path);
            UploadTask uploadTask=imageRef.putBytes(profilePhotoByteArray);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Unable to save photo",
                                Toast.LENGTH_SHORT).show();
                        if (task.getException() != null) {
                            throw task.getException();
                        } else {
                            throw new Exception("Profile save failure");
                        }
                    }

                    // Continue with the task to get the download URL
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();
                        Log.d("appdebug", downloadUri.toString());
                        UpdateCurrentUserInDatabase(emailString, passwordString, displayNameString, downloadUri.toString());
                    } else {
                        Toast.makeText(ProfileActivity.this, "Unable to save photo",
                                Toast.LENGTH_SHORT).show();
                        if (task.getException() != null) {
                            Toast.makeText(ProfileActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            UpdateCurrentUserInDatabase(emailString, passwordString, displayNameString, null);
        }
    }

    void UpdateCurrentUserInDatabase(final String email, final String password, final String displayName, final String profilePhotoUri) {
        if (currentUser!=null) {
            final DatabaseReference usersRef = mRootReference.child("users").child(currentUser.getUid());
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User u = dataSnapshot.getValue(User.class);
                    u.uid = dataSnapshot.getKey();
                    u.email = email;
                    u.displayName = displayName;
                    if (profilePhotoUri != null) {
                        u.profilePhotoUri = profilePhotoUri;
                    }
                    usersRef.setValue(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!password.isEmpty()) {
                                currentUser.updatePassword(password)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ProfileActivity.this, "Profile save successful",
                                                            Toast.LENGTH_SHORT).show();

                                                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(ProfileActivity.this, "Unable to update password",
                                                            Toast.LENGTH_SHORT).show();
                                                    if (task.getException() != null) {
                                                        Toast.makeText(ProfileActivity.this, task.getException().getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Profile save successful",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(ProfileActivity.this, "Unable to obtain profile user from database",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
