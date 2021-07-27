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

public class MobileNumberActivity extends AppCompatActivity {

    private static final String TAG = MobileNumberActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private SharedPrefUtils sharedPrefUtils;
    private EditText edtPhone;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_mobile_number);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        sharedPrefUtils = SharedPrefUtils.getInstance(this);

        edtPhone = findViewById(R.id.idEdtPhoneNumber);
        Button generateOTPBtn = findViewById(R.id.idBtnGetOtp);

        generateOTPBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                Toast.makeText(MobileNumberActivity.this, getResources().getString(R.string.msg_enter_valid_phone_number), Toast.LENGTH_SHORT).show();
            } else {
                String phone = getResources().getString(R.string.label_country_code) + edtPhone.getText().toString();
                Intent intent = new Intent(MobileNumberActivity.this, MobileNumberOTPActivity.class);
                intent.putExtra("REG_MOBILE_NUMBER", phone);
                startActivity(intent);
            }
        });

        updateScreens();

    }

    private void updateScreens() {
        if (sharedPrefUtils.getValue(Utils.IS_MOBILE_VERIFIED, false) && !sharedPrefUtils.getValue(Utils.IS_EMAIL_VERIFIED, false)) {
            Intent i = new Intent(MobileNumberActivity.this, EmailAddressActivity.class);
            startActivity(i);
            finish();
        } else if (sharedPrefUtils.getValue(Utils.IS_MOBILE_VERIFIED, false) && sharedPrefUtils.getValue(Utils.IS_EMAIL_VERIFIED, false)) {
            Intent i = new Intent(MobileNumberActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

    }


}
