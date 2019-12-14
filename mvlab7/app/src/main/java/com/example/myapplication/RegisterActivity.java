package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_CAMERA = 111;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    EditText email;
    EditText password;
    EditText displayName;
    ImageView ivProfilePhoto;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseStorage storage;
    byte[] profilePhotoByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById((R.id.et_email));
        password = findViewById(R.id.et_password);
        displayName = findViewById(R.id.et_display_name);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
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
        String emailString = email.getText().toString().trim();
        String passwordString = password.getText().toString().trim();
        String displayNameString = displayName.getText().toString().trim();

        if (emailString.isEmpty()
                || passwordString.isEmpty()
                || displayNameString.isEmpty()
                || profilePhotoByteArray == null) {
            Toast.makeText(RegisterActivity.this, "All fields must be provided",
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
            createAccount(
                    emailString,
                    passwordString,
                    displayNameString,
                    profilePhotoByteArray
            );
        }
    }

    private void createAccount(
            final String email,
            final String password,
            final String displayName,
            final byte[] byteArray
    )
    {
        // https://firebase.google.com/docs/auth/android/manage-users
        Log.d("appdebug", "createAccount:" + email);
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d("appdebug", "createUserWithEmail:success");
                        user = mAuth.getCurrentUser();
                        Toast.makeText(RegisterActivity.this, "Authentication succeeded.",
                                Toast.LENGTH_SHORT).show();
                        SendVerification();

                        String path="images/"+ UUID.randomUUID()+".jpg";
                        final StorageReference imageRef = storage.getReference(path);
                        UploadTask uploadTask=imageRef.putBytes(profilePhotoByteArray);

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Unable to save photo",
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

                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(displayName)
                                            .setPhotoUri(downloadUri)
                                            .build();
                                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                User newUser = new User();
                                                newUser.email = email;
                                                newUser.displayName = displayName;
                                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("/users");
                                                usersRef.child(user.getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(RegisterActivity.this, "Profile save successful",
                                                                    Toast.LENGTH_SHORT).show();

                                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Unable to save user to database",
                                                                    Toast.LENGTH_SHORT).show();
                                                            if (task.getException() != null) {
                                                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Unable to update Display Name and Photo URI",
                                                        Toast.LENGTH_SHORT).show();
                                                if (task.getException() != null) {
                                                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Unable to save photo",
                                            Toast.LENGTH_SHORT).show();
                                    if (task.getException() != null) {
                                        Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("appdebug", "Failure:"+e.getMessage());
                        user = mAuth.getCurrentUser();
                        Toast.makeText(RegisterActivity.this, "Failure:"+e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void SendVerification(){
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Email Sent",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
