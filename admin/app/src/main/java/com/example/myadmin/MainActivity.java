package com.example.myadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {

    private UserRecord userRecord;

    class GetUserRecord extends AsyncTask<String, Void, UserRecord> {
        @Override
        protected UserRecord doInBackground(String... strings) {
            try {
                UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(strings[0]);
                Log.d("appdebug", "GetUserRecord Success");
                return userRecord;
            } catch (Exception e) {
                Log.d("appdebug", "GetUserRecord Exception: " + e.toString());
            }
            return null;
        }
    }

    class SetUserRecord extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                String newEmail = strings[0];
                String newPassword = strings[1];
                String newPhone = strings[2];
                Boolean newVerified = null;
                if (strings[3] != null) newVerified = Boolean.valueOf(strings[3]);
                String newName = strings[4];
                String newPhoto = strings[5];
                Boolean newDisabled = null;
                if (strings[6] != null) newDisabled = Boolean.valueOf(strings[6]);
                if (newPassword != null) {
                    UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userRecord.getUid())
                            .setEmail(newEmail)
                            .setPassword(newPassword)
                            .setPhoneNumber(newPhone)
                            .setEmailVerified(newVerified)
                            .setDisplayName(newName)
                            .setPhotoUrl(newPhoto)
                            .setDisabled(newDisabled);
                    UserRecord userRecord = FirebaseAuth.getInstance().updateUser(request);
                    Log.d("appdebug", "SetUserRecord Success");
                } else {
                    UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userRecord.getUid())
                            .setEmail(newEmail)
                            .setPhoneNumber(newPhone)
                            .setEmailVerified(newVerified)
                            .setDisplayName(newName)
                            .setPhotoUrl(newPhoto)
                            .setDisabled(newDisabled);
                    UserRecord userRecord = FirebaseAuth.getInstance().updateUser(request);
                    Log.d("appdebug", "SetUserRecord Success");
                }
                return true;
            } catch (Exception e) {
                Log.d("appdebug", "SetUserRecord Exception: " + e.toString());
            }
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            FileInputStream serviceAccount =
                    new FileInputStream("/sdcard/Download/family-budget-admin.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://thingstodo-28b64.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            Toast.makeText(this, "AdminSDK Initialize Failure: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onClickGet(View view) {
        try {
            EditText etEmail = findViewById(R.id.et_email);
            GetUserRecord getUserRecord = new GetUserRecord();
            userRecord = getUserRecord.execute(etEmail.getText().toString().trim()).get();
            etEmail.setText(userRecord.getEmail());
            EditText etPassword = findViewById(R.id.et_password);
            etPassword.setText(null);
            EditText etPhone = findViewById(R.id.et_phone);
            etPhone.setText(userRecord.getPhoneNumber());
            EditText etVerified = findViewById(R.id.et_verified);
            etVerified.setText(String.valueOf(userRecord.isEmailVerified()));
            EditText etName = findViewById(R.id.et_name);
            etName.setText(userRecord.getDisplayName());
            EditText etPhoto = findViewById(R.id.et_photo);
            etPhoto.setText(userRecord.getPhotoUrl());
            EditText etDisabled = findViewById(R.id.et_disabled);
            etDisabled.setText(String.valueOf(userRecord.isDisabled()));

            Toast.makeText(this, "Get Profile Successful", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "onClickGetValue Failure: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onClickSet(View view) {
        try {
            EditText etEmail = findViewById(R.id.et_email);
            EditText etPassword = findViewById(R.id.et_password);
            EditText etPhone = findViewById(R.id.et_phone);
            EditText etVerified = findViewById(R.id.et_verified);
            EditText etName = findViewById(R.id.et_name);
            EditText etPhoto = findViewById(R.id.et_photo);
            EditText etDisabled = findViewById(R.id.et_disabled);

            String newEmail = null;
            if (!etEmail.getText().toString().trim().equals("")) {
                newEmail = etEmail.getText().toString().trim();
            }
            String newPassword = null;
            if (!etPassword.getText().toString().trim().equals("")) {
                newPassword = etPassword.getText().toString().trim();
            }
            String newPhone = null;
            if (!etPhone.getText().toString().trim().equals("")) {
                newPhone = etPhone.getText().toString().trim();
            }
            String newVerified = null;
            if (!etVerified.getText().toString().trim().equals("")) {
                newVerified = etVerified.getText().toString().trim();
            }
            String newName = null;
            if (!etName.getText().toString().trim().equals("")) {
                newName = etName.getText().toString().trim();
            }
            String newPhoto = null;
            if (!etPhoto.getText().toString().trim().equals("")) {
                newPhoto = etPhoto.getText().toString().trim();
            }
            String newDisabled = null;
            if (!etDisabled.getText().toString().trim().equals("")) {
                newDisabled = etDisabled.getText().toString().trim();
            }

            SetUserRecord setUserRecord = new SetUserRecord();
            String[] strings = {newEmail, newPassword, newPhone, newVerified, newName, newPhoto, newDisabled};
            if (!setUserRecord.execute(strings).get()) {
                Toast.makeText(this, "Set Profile Failed", Toast.LENGTH_LONG).show();
                return;
            }

            GetUserRecord getUserRecord = new GetUserRecord();
            userRecord = getUserRecord.execute(etEmail.getText().toString().trim()).get();
            etEmail.setText(userRecord.getEmail());
            etPassword.setText(null);
            etPhone.setText(userRecord.getPhoneNumber());
            etVerified.setText(String.valueOf(userRecord.isEmailVerified()));
            etName.setText(userRecord.getDisplayName());
            etPhoto.setText(userRecord.getPhotoUrl());
            etDisabled.setText(String.valueOf(userRecord.isDisabled()));

            Toast.makeText(this, "Set Profile Successful", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "onClickSetValue Failure: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
