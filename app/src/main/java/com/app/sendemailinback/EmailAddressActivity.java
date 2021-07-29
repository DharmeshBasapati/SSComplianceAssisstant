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

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.view.View.GONE;
import static com.app.sendemailinback.Utils.EMAIL_VERIFICATION_SUBJECT;

public class EmailAddressActivity extends AppCompatActivity {

    private static final String TAG = EmailAddressActivity.class.getSimpleName();

    private SharedPrefUtils sharedPrefUtils;
    private EditText edtEmail;
    private LinearLayout lnrProgress;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_email_address);

        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        fab = findViewById(R.id.fab);
        edtEmail = findViewById(R.id.edtEmail);
        Button btnGetOtpOnEmail = findViewById(R.id.btnGetOtpOnEmail);
        lnrProgress = findViewById(R.id.lnrProgress);
        btnGetOtpOnEmail.setOnClickListener(v -> {
            if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                Toast.makeText(EmailAddressActivity.this, getResources().getString(R.string.msg_enter_valid_email_address), Toast.LENGTH_SHORT).show();
            } else {
                showProgress();
                String newOTP = Utils.getRandomNumberString();
                Log.d(TAG, "onClick: New OTP = " + newOTP);
                sharedPrefUtils.setValue(Utils.CURRENT_OTP, newOTP);

                String messageBody = "Dear customer," +
                        "<br/><br/>" +
                        "Please enter below OTP on Chartered Box Reminder App." +
                        "<br/><br/>" +
                        "<b>OTP: " + newOTP + "</b> "+
                        "<br/><br/>" +
                        "Kind Regards," +
                        "<br/>" +
                        "Chartered Box Reminder Team";

                Utils.sendEmail(EmailAddressActivity.this,
                        EMAIL_VERIFICATION_SUBJECT, messageBody,
                        edtEmail.getText().toString(),
                        null, () -> EmailAddressActivity.this.runOnUiThread(() -> {
                            hideProgress();
                            Intent intent = new Intent(EmailAddressActivity.this, EmailAddressOTPActivity.class);
                            intent.putExtra("REG_EMAIL_ID", edtEmail.getText().toString());
                            startActivity(intent);
                        }));
            }
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

}
