package com.app.sendemailinback;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MobileNumberActivity extends AppCompatActivity {

    private static final String TAG = MobileNumberActivity.class.getSimpleName();

    private SharedPrefUtils sharedPrefUtils;
    private EditText edtPhone;
    private String verificationId;
    private ImageView imgMobile;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_mobile_number);

        FirebaseApp.initializeApp(this);

        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        fab = findViewById(R.id.fab);
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
        //scheduleJob();

    }

    /*@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scheduleJob() {
        JobInfo myJob = new JobInfo.Builder(0, new ComponentName(this, NetworkSchedulerService.class))
                .setRequiresCharging(false)
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(myJob);
    }

    @Override
    protected void onStop() {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        stopService(new Intent(this, NetworkSchedulerService.class));
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start service and provide it a way to communicate with this class.
        Intent startServiceIntent = new Intent(this, NetworkSchedulerService.class);
        startService(startServiceIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

            //return true if in App's Battery settings "Not optimized" and false if "Optimizing battery use"
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                Toast.makeText(this, "Great - App is not optimizing battery.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "OOPS - App is optimizing battery.", Toast.LENGTH_SHORT).show();

                *//*Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);*//*
            }
        }


    }*/

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
