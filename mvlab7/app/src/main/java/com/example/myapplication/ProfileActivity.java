package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class ProfileActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    EditText displayName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        buildNavDrawerAndToolbar();

        displayName = findViewById(R.id.et_display_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        displayName.setText(user.getDisplayName());
    }

    public void onClickUploadPhoto(View view) {
        Toast.makeText(getApplicationContext(), "onClickUploadPhoto",
                Toast.LENGTH_SHORT).show();
    }

    public void onClickSaveProfile(View view) {

        // FIXME: Update to use path of picture
        String urlForPicture = "https://console.firebase.google.com/u/4/project/family-budget-df281/database/mypicture.jpg";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.getText().toString())
                    .setPhotoUri(Uri.parse("http://example.com/supercontra.jpg"))
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        displayName.setText(user.getDisplayName());
                    }
                    else
                    {
                        Toast.makeText(ProfileActivity.this, "Profile Save Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
