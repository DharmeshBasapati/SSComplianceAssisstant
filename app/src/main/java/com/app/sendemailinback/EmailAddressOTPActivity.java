package com.app.sendemailinback;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class EmailAddressOTPActivity extends AppCompatActivity {

    private static final String TAG = EmailAddressOTPActivity.class.getSimpleName();

    private SharedPrefUtils sharedPrefUtils;
    private EditText edtEmailOtp;
    private String REG_EMAIL_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_email_address_otp);

        sharedPrefUtils = SharedPrefUtils.getInstance(this);

        edtEmailOtp = findViewById(R.id.edtEmailOtp);
        Button btnEmailVerify = findViewById(R.id.btnEmailVerify);

        REG_EMAIL_ID = getIntent().getStringExtra("REG_EMAIL_ID");

        btnEmailVerify.setOnClickListener(v -> {
            Log.d(TAG, "Current OTP from Prefs = " + sharedPrefUtils.getValue(Utils.CURRENT_OTP, ""));
            if (TextUtils.isEmpty(edtEmailOtp.getText().toString())) {
                Toast.makeText(EmailAddressOTPActivity.this, getResources().getString(R.string.msg_please_enter_otp), Toast.LENGTH_SHORT).show();
            } else {
                if (sharedPrefUtils.getValue(Utils.CURRENT_OTP, "").equals(edtEmailOtp.getText().toString())) {
                    sharedPrefUtils.setValue(Utils.IS_EMAIL_VERIFIED, true);
                    sharedPrefUtils.setValue(Utils.RECIPIENT_EMAIL_ID, REG_EMAIL_ID);
                    sharedPrefUtils.setValue(Utils.CURRENT_OTP, "");
                    Toast.makeText(EmailAddressOTPActivity.this, getResources().getString(R.string.msg_email_verified_successfully), Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(EmailAddressOTPActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(EmailAddressOTPActivity.this, getResources().getString(R.string.msg_enter_correct_otp), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }


}
