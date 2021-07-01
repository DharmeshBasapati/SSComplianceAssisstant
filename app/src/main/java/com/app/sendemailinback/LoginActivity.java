package com.app.sendemailinback;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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


import static android.view.View.GONE;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private EditText edtPhone, edtOTP;
    private String verificationId;
    private EditText edtEmail, edtEmailOtp;
    private LinearLayout lnrMobile, lnrEmail;
    private SharedPrefUtils sharedPrefUtils;
    private LinearLayout lnrProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        sharedPrefUtils = SharedPrefUtils.getInstance(this);

        lnrProgress = findViewById(R.id.lnrProgress);

        lnrMobile = findViewById(R.id.lnrMobile);
        edtPhone = findViewById(R.id.idEdtPhoneNumber);
        edtOTP = findViewById(R.id.idEdtOtp);
        Button verifyOTPBtn = findViewById(R.id.idBtnVerify);
        Button generateOTPBtn = findViewById(R.id.idBtnGetOtp);

        lnrEmail = findViewById(R.id.lnrEmail);
        edtEmail = findViewById(R.id.edtEmail);
        edtEmailOtp = findViewById(R.id.edtEmailOtp);
        Button btnGetOtpOnEmail = findViewById(R.id.btnGetOtpOnEmail);
        Button btnEmailVerify = findViewById(R.id.btnEmailVerify);

        updateLayouts();

        generateOTPBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.msg_enter_valid_phone_number), Toast.LENGTH_SHORT).show();
            } else {
                String phone = getResources().getString(R.string.label_country_code) + edtPhone.getText().toString();
                sendVerificationCode(phone);
            }
        });

        verifyOTPBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edtOTP.getText().toString())) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.msg_please_enter_otp), Toast.LENGTH_SHORT).show();
            } else if (verificationId == null) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.msg_enter_correct_otp), Toast.LENGTH_SHORT).show();
            } else {
                verifyCode(edtOTP.getText().toString());
            }
        });

        btnGetOtpOnEmail.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.msg_enter_valid_email_address), Toast.LENGTH_SHORT).show();
            } else {
                showProgress();
                String newOTP = Utils.getRandomNumberString();
                Log.d(TAG, "onClick: New OTP = " + newOTP);
                sharedPrefUtils.setValue(Utils.CURRENT_OTP, newOTP);
                Utils.sendEmail(LoginActivity.this,
                        "Please enter this OTP - " + newOTP + " to verify your email.",
                        edtEmail.getText().toString(),
                        () -> LoginActivity.this.runOnUiThread(() -> lnrProgress.setVisibility(View.GONE)));
            }
        });

        btnEmailVerify.setOnClickListener(v -> {
            Log.d(TAG, "Current OTP from Prefs = " + sharedPrefUtils.getValue(Utils.CURRENT_OTP, ""));
            if (TextUtils.isEmpty(edtEmailOtp.getText().toString())) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.msg_please_enter_otp), Toast.LENGTH_SHORT).show();
            } else {
                if (sharedPrefUtils.getValue(Utils.CURRENT_OTP, "").equals(edtEmailOtp.getText().toString())) {
                    sharedPrefUtils.setValue(Utils.IS_EMAIL_VERIFIED, true);
                    sharedPrefUtils.setValue(Utils.RECIPIENT_EMAIL_ID, edtEmail.getText().toString());
                    sharedPrefUtils.setValue(Utils.CURRENT_OTP, "");
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.msg_email_verified_successfully), Toast.LENGTH_SHORT).show();
                    updateLayouts();
                } else {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.msg_enter_correct_otp), Toast.LENGTH_SHORT).show();
                }
            }

        });


    }

    private void updateLayouts() {

        if (!sharedPrefUtils.getValue(Utils.IS_MOBILE_VERIFIED, false)) {
            lnrMobile.setVisibility(View.VISIBLE);
            lnrEmail.setVisibility(View.GONE);
        } else if (sharedPrefUtils.getValue(Utils.IS_MOBILE_VERIFIED, false) && !sharedPrefUtils.getValue(Utils.IS_EMAIL_VERIFIED, false)) {
            lnrEmail.setVisibility(View.VISIBLE);
            lnrMobile.setVisibility(View.GONE);
        } else if (sharedPrefUtils.getValue(Utils.IS_MOBILE_VERIFIED, false) && sharedPrefUtils.getValue(Utils.IS_EMAIL_VERIFIED, false)) {
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    hideProgress();
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.msg_phone_number_verified_successfully), Toast.LENGTH_LONG).show();
                        sharedPrefUtils.setValue(Utils.IS_MOBILE_VERIFIED, true);
                        sharedPrefUtils.setValue(Utils.VERIFIED_MOBILE_NUMBER, edtPhone.getText().toString());
                        updateLayouts();
                    } else {
                        Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void sendVerificationCode(String number) {
        showProgress();
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
            verificationId = s;
            hideProgress();
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            final String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                edtOTP.setText(code);
                hideProgress();
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            hideProgress();
        }
    };

    private void verifyCode(String code) {
        showProgress();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    public void showProgress() {
        lnrProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        lnrProgress.setVisibility(GONE);
    }

}