package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;


public class ProfileActivity extends BaseActivity {
    private static final int MY_PERMISSIONS_CAMERA = 111;

    EditText email;
    EditText password;
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

        email = findViewById((R.id.et_email));
        password = findViewById(R.id.et_password);
        displayName = findViewById(R.id.et_display_name);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);

        storage = FirebaseStorage.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        email.setText(user.getEmail());
        displayName.setText(user.getDisplayName());
        Glide
                .with(this)
                .load(user.getPhotoUrl())
                .into(ivProfilePhoto);
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
        if (email.getText().toString().isEmpty() || displayName.getText().toString().isEmpty()) {
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
                        Uri downloadUri = task.getResult();
                        Log.d("appdebug", downloadUri.toString());

                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user!=null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName.getText().toString())
                                    .setPhotoUri(downloadUri)
                                    .build();
                            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        user.updateEmail(email.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            if (!password.getText().toString().isEmpty()) {
                                                                user.updatePassword(password.getText().toString())
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
                                                        } else {
                                                            Toast.makeText(ProfileActivity.this, "Unable to update email",
                                                                    Toast.LENGTH_SHORT).show();
                                                            if (task.getException() != null) {
                                                                Toast.makeText(ProfileActivity.this, task.getException().getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Unable to update Display Name and Photo URI",
                                                Toast.LENGTH_SHORT).show();
                                        if (task.getException() != null) {
                                            Toast.makeText(ProfileActivity.this, task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(ProfileActivity.this, "Unable to obtain profile user from database",
                                    Toast.LENGTH_SHORT).show();
                        }
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
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user!=null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName.getText().toString())
                        .build();
                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updateEmail(email.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                if (!password.getText().toString().isEmpty()) {
                                                    user.updatePassword(password.getText().toString())
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
                                            } else {
                                                Toast.makeText(ProfileActivity.this, "Unable to update email",
                                                        Toast.LENGTH_SHORT).show();
                                                if (task.getException() != null) {
                                                    Toast.makeText(ProfileActivity.this, task.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(ProfileActivity.this, "Unable to update Display Name",
                                    Toast.LENGTH_SHORT).show();
                            if (task.getException() != null) {
                                Toast.makeText(ProfileActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            } else {
                Toast.makeText(ProfileActivity.this, "Unable to obtain profile user from database",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
