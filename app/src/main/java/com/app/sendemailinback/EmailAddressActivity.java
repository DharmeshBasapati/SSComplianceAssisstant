package com.app.sendemailinback;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class EmailAddressActivity extends AppCompatActivity {

    private static final String TAG = EmailAddressActivity.class.getSimpleName();

    private SharedPrefUtils sharedPrefUtils;
    private EditText edtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_email_address);

        FirebaseApp.initializeApp(this);
        sharedPrefUtils = SharedPrefUtils.getInstance(this);

        edtEmail = findViewById(R.id.edtEmail);
        Button btnGetOtpOnEmail = findViewById(R.id.btnGetOtpOnEmail);

        btnGetOtpOnEmail.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                Toast.makeText(EmailAddressActivity.this, getResources().getString(R.string.msg_enter_valid_email_address), Toast.LENGTH_SHORT).show();
            } else {
                String newOTP = Utils.getRandomNumberString();
                Log.d(TAG, "onClick: New OTP = " + newOTP);
                sharedPrefUtils.setValue(Utils.CURRENT_OTP, newOTP);
                Utils.sendEmail(EmailAddressActivity.this,
                        "Please enter this OTP - " + newOTP + " to verify your email.",
                        edtEmail.getText().toString(),
                        () -> EmailAddressActivity.this.runOnUiThread(() -> {
                            Intent intent = new Intent(EmailAddressActivity.this, EmailAddressOTPActivity.class);
                            intent.putExtra("REG_EMAIL_ID",edtEmail.getText().toString());
                            startActivity(intent);
                        }));
            }
        });
    }


}
