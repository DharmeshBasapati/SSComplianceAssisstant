package com.app.sendemailinback;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

import static android.view.View.GONE;

public class EmailAddressOTPActivity extends AppCompatActivity {

    private static final String TAG = EmailAddressOTPActivity.class.getSimpleName();

    private SharedPrefUtils sharedPrefUtils;
    private EditText edtEmailOtp;
    private String REG_EMAIL_ID;
    private LinearLayout lnrProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_email_address_otp);

        sharedPrefUtils = SharedPrefUtils.getInstance(this);

        edtEmailOtp = findViewById(R.id.edtEmailOtp);
        Button btnEmailVerify = findViewById(R.id.btnEmailVerify);
        lnrProgress = findViewById(R.id.lnrProgress);
        REG_EMAIL_ID = getIntent().getStringExtra("REG_EMAIL_ID");

        btnEmailVerify.setOnClickListener(v -> {
            Log.d(TAG, "Current OTP from Prefs = " + sharedPrefUtils.getValue(Utils.CURRENT_OTP, ""));
            if (TextUtils.isEmpty(edtEmailOtp.getText().toString())) {
                Toast.makeText(EmailAddressOTPActivity.this, getResources().getString(R.string.msg_please_enter_otp), Toast.LENGTH_SHORT).show();
            } else {
                if (sharedPrefUtils.getValue(Utils.CURRENT_OTP, "").equals(edtEmailOtp.getText().toString())) {
                    //showProgress();
                    sharedPrefUtils.setValue(Utils.IS_EMAIL_VERIFIED, true);
                    sharedPrefUtils.setValue(Utils.RECIPIENT_EMAIL_ID, REG_EMAIL_ID);
                    sharedPrefUtils.setValue(Utils.CURRENT_OTP, "");
                    Intent i = new Intent(EmailAddressOTPActivity.this, MainActivity.class);

//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            hideProgress();
//                        }
//                    },3000);

                    Toast.makeText(EmailAddressOTPActivity.this, getResources().getString(R.string.msg_email_verified_successfully), Toast.LENGTH_SHORT).show();
                    startActivity(i);
                } else {
                    Toast.makeText(EmailAddressOTPActivity.this, getResources().getString(R.string.msg_enter_correct_otp), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    public void showProgress() {
        lnrProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        lnrProgress.setVisibility(GONE);
    }

}
