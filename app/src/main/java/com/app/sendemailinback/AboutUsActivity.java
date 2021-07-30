package com.app.sendemailinback;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AboutUsActivity extends AppCompatActivity {

    private static final String TAG = "AboutUsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    public void rateApp(View view) {
        //https://play.google.com/store/apps/details?id=com.app.sscompliancereminder
        Uri marketUri = Uri.parse("market://details?id=com.app.sscompliancereminder");
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        try {
            startActivity(marketIntent);
        } catch (Exception e) {
            Log.d(TAG, "rateApp: " + e.getMessage());
        }
    }

    public void shareApp(View view) {
        Utils.shareSMS(AboutUsActivity.this, "Hey,\n\nChartered Box Reminder App is a service that allows to forward your SMS to registered Email address.\n\nGet it for free at\nmarket://details?id=com.app.sscompliancereminder");
    }

    public void contactUs(View view) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_contact_us);
        bottomSheetDialog.show();
    }
}