package com.app.sendemailinback;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class AboutUsActivity extends AppCompatActivity {

    private static final String TAG = "AboutUsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
    }

    public void rateApp(View view) {
        //https://play.google.com/store/apps/details?id=com.app.sscompliancereminder
        Uri marketUri = Uri.parse("market://details?id=com.app.sscompliancereminder");
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        try {
            startActivity(marketIntent);
        }catch (Exception e){
            Log.d(TAG, "rateApp: "+e.getMessage());
        }
    }

    public void shareApp(View view) {
        Utils.shareSMS(AboutUsActivity.this,"Share CB Reminder App");
    }

    public void contactUs(View view) {
        Toast.makeText(this, "You clicked Contact Us.", Toast.LENGTH_SHORT).show();
    }
}