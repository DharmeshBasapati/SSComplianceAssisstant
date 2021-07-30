package com.app.sendemailinback;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;

public class MobileNumberOTPActivity extends AppCompatActivity {

    private static final String TAG = MobileNumberOTPActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private SharedPrefUtils sharedPrefUtils;
    private EditText edtOTP;
    private String verificationId;
    private String REG_MOBILE_NUMBER;
    private LinearLayout lnrProgress;
    private Button verifyOTPBtn;
    private FloatingActionButton fab;
    private TextView txtOtpLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_mobile_number_otp);

        mAuth = FirebaseAuth.getInstance();
        sharedPrefUtils = SharedPrefUtils.getInstance(this);

        txtOtpLabel = findViewById(R.id.txtOtpLabel);

        edtOTP = findViewById(R.id.idEdtOtp);
        verifyOTPBtn = findViewById(R.id.idBtnVerify);
        fab = findViewById(R.id.fab);
        lnrProgress = findViewById(R.id.lnrProgress);

        REG_MOBILE_NUMBER = getIntent().getStringExtra("REG_MOBILE_NUMBER");

        txtOtpLabel.setText(String.format("We have sent you an SMS with a 6-digit verification code on +91 %s", REG_MOBILE_NUMBER));

        sendVerificationCode(REG_MOBILE_NUMBER);

        verifyOTPBtn.setOnClickListener(v -> {
            verifyOTP();
        });

    }

    public void showProgress() {
        fab.setCompatElevation(0);
        lnrProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        fab.setCompatElevation(2);
        lnrProgress.setVisibility(GONE);
    }

    private void sendVerificationCode(String number) {
        Toast.makeText(this, "Waiting for OTP...", Toast.LENGTH_SHORT).show();
        //showProgress();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+number)
                        .setTimeout(30L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallBack)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String message) {
            super.onCodeAutoRetrievalTimeOut(message);
            Log.d(TAG, "onCodeAutoRetrievalTimeOut: CALLED - "+message);
            edtOTP.setEnabled(true);
            verifyOTPBtn.setEnabled(true);
            Toast.makeText(MobileNumberOTPActivity.this, "OTP Code Auto Retrieval Timed Out.", Toast.LENGTH_LONG).show();
        }

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
                //hideProgress();
                edtOTP.setText(code);
                edtOTP.setEnabled(true);
                verifyOTPBtn.setEnabled(true);
                showProgress();
                verifyCode(code);

            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.d(TAG, "onVerificationFailed: CALLED");
            edtOTP.setEnabled(true);
            verifyOTPBtn.setEnabled(true);
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
                    hideProgress();
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
