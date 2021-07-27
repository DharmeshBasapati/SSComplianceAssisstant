package com.app.sendemailinback;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MobileNumberOTPActivity extends AppCompatActivity {

    private static final String TAG = MobileNumberOTPActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private SharedPrefUtils sharedPrefUtils;
    private EditText edtOTP;
    private String verificationId;
    private String REG_MOBILE_NUMBER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_mobile_number_otp);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        sharedPrefUtils = SharedPrefUtils.getInstance(this);

        edtOTP = findViewById(R.id.idEdtOtp);
        Button verifyOTPBtn = findViewById(R.id.idBtnVerify);

        REG_MOBILE_NUMBER = getIntent().getStringExtra("REG_MOBILE_NUMBER");

        sendVerificationCode(REG_MOBILE_NUMBER);

        verifyOTPBtn.setOnClickListener(v -> {
            verifyOTP();
        });

    }

    private void sendVerificationCode(String number) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallBack)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.d(TAG, "onCodeSent: CALLED");
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted: CALLED");
            final String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                //OPEN OTP SCREEN
                edtOTP.setText(code);

            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.d(TAG, "onVerificationFailed: CALLED");
            Toast.makeText(MobileNumberOTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void verifyOTP() {
        if (TextUtils.isEmpty(edtOTP.getText().toString())) {
            Toast.makeText(MobileNumberOTPActivity.this, getResources().getString(R.string.msg_please_enter_otp), Toast.LENGTH_SHORT).show();
        } else if (verificationId == null) {
            Toast.makeText(MobileNumberOTPActivity.this, getResources().getString(R.string.msg_enter_correct_otp), Toast.LENGTH_SHORT).show();
        } else {
            verifyCode(edtOTP.getText().toString());
        }
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MobileNumberOTPActivity.this, getResources().getString(R.string.msg_phone_number_verified_successfully), Toast.LENGTH_LONG).show();
                        sharedPrefUtils.setValue(Utils.IS_MOBILE_VERIFIED, true);
                        sharedPrefUtils.setValue(Utils.VERIFIED_MOBILE_NUMBER, REG_MOBILE_NUMBER);
                        Intent intent = new Intent(MobileNumberOTPActivity.this, EmailAddressActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    } else {
                        Toast.makeText(MobileNumberOTPActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
