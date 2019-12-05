package com.example.myadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {

    private UserRecord userRecord;

    class GetEmailVerified extends AsyncTask<String, Void, UserRecord> {
        @Override
        protected UserRecord doInBackground(String... strings) {
            try {
                UserRecord userRecord = FirebaseAuth.getInstance().getUser(strings[0]);
                Log.d("debug", "GetEmailVerified Success");
                return userRecord;
            } catch (Exception e) {
                Log.d("debug", "GetEmailVerified Exception: " + e.toString());
            }
            return null;
        }
    }

    class SetEmailVerified extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Boolean newValue = Boolean.valueOf(strings[0]);
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userRecord.getUid())
                        .setEmailVerified(newValue);
                UserRecord userRecord = FirebaseAuth.getInstance().updateUser(request);
                Log.d("debug", "SetEmailVerified Success");
                return true;
            } catch (Exception e) {
                Log.d("debug", "SetEmailVerified Exception: " + e.toString());
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
                    .setDatabaseUrl("https://family-budget-df281.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            Toast.makeText(this, "AdminSDK Initialize Failure: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onClickGetValue(View view) {
        try {
            EditText etUID = findViewById(R.id.et_uid);
            GetEmailVerified getEmailVerified = new GetEmailVerified();
            Log.d("debug", "etUID.getText().toString().trim(): " + etUID.getText().toString().trim());
            userRecord = getEmailVerified.execute(etUID.getText().toString().trim()).get();
            TextView textView = findViewById(R.id.tv_current_value);
            textView.setText("Email = " + String.valueOf(userRecord.getEmail()) + System.getProperty("line.separator") +
                    "PhoneNumber = " + String.valueOf(userRecord.getPhoneNumber()) + System.getProperty("line.separator") +
                    "EmailVerified = " + String.valueOf(userRecord.isEmailVerified()) + System.getProperty("line.separator") +
                    "DisplayName = " + String.valueOf(userRecord.getDisplayName()) + System.getProperty("line.separator") +
                    "PhotoUrl = " + String.valueOf(userRecord.getPhotoUrl()) + System.getProperty("line.separator") +
                    "Disabled = " + String.valueOf(userRecord.isDisabled()) + System.getProperty("line.separator")
            );
        } catch (Exception e) {
            Toast.makeText(this, "onClickGetValue Failure: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onClickSetValue(View view) {
        try {
            EditText etEmailVerified = findViewById(R.id.et_email_verified);
            SetEmailVerified setEmailVerified = new SetEmailVerified();
            if (setEmailVerified.execute(etEmailVerified.getText().toString().trim()).get()) {
                Toast.makeText(this, "Set Email Verified Successful", Toast.LENGTH_LONG).show();

                EditText etUID = findViewById(R.id.et_uid);
                GetEmailVerified getEmailVerified = new GetEmailVerified();
                Log.d("debug", "etUID.getText().toString().trim(): " + etUID.getText().toString().trim());
                userRecord = getEmailVerified.execute(etUID.getText().toString().trim()).get();
                TextView textView = findViewById(R.id.tv_current_value);
                textView.setText("Email = " + String.valueOf(userRecord.getEmail()) + System.getProperty("line.separator") +
                        "PhoneNumber = " + String.valueOf(userRecord.getPhoneNumber()) + System.getProperty("line.separator") +
                        "EmailVerified = " + String.valueOf(userRecord.isEmailVerified()) + System.getProperty("line.separator") +
                        "DisplayName = " + String.valueOf(userRecord.getDisplayName()) + System.getProperty("line.separator") +
                        "PhotoUrl = " + String.valueOf(userRecord.getPhotoUrl()) + System.getProperty("line.separator") +
                        "Disabled = " + String.valueOf(userRecord.isDisabled()) + System.getProperty("line.separator")
                );
            } else {
                Toast.makeText(this, "Set Email Verified Failed", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "onClickSetValue Failure: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
