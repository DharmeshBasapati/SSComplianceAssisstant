package com.app.sendemailinback;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private List<SMSModel> listOfSMS;
    RecyclerView rvSMS;
    SMSListAdapter smsListAdapter;
    private SharedPrefUtils sharedPrefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefUtils = SharedPrefUtils.getInstance(this);

        rvSMS = findViewById(R.id.rvSMS);
        rvSMS.setLayoutManager(new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false));

        requestPermissions();

//        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
//        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(EmailWorker.class)
//                .setConstraints(constraints).build();
//        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void readSMS() {
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        Log.d(TAG, "readSMS: SMS COUNT = " + c.getCount());
        int totalSMS = 0;
        listOfSMS = new ArrayList<>();
        totalSMS = c.getCount();
        if (c.moveToFirst()) {
            for (int j = 0; j < totalSMS; j++) {
                String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                Date dateFormat = new Date(Long.parseLong(smsDate));
                DateFormat formatter = new SimpleDateFormat(Utils.SMS_DATE_FORMAT, Locale.getDefault());
                String today = formatter.format(dateFormat);

                String type = "";
                switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                    case Telephony.Sms.MESSAGE_TYPE_INBOX:
                        type = "inbox";
                        break;
                    case Telephony.Sms.MESSAGE_TYPE_SENT:
                        type = "sent";
                        break;
                    case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                        type = "outbox";
                        break;
                    default:
                        break;
                }
                SMSModel smsModel = new SMSModel(today, number, body, type);
                listOfSMS.add(smsModel);

                c.moveToNext();
            }
        }

        c.close();

        if (listOfSMS != null && listOfSMS.size() > 0) {
            smsListAdapter = new SMSListAdapter(MainActivity.this, listOfSMS);
            rvSMS.setAdapter(smsListAdapter);
            smsListAdapter.notifyDataSetChanged();

            //sendEmailToFilteredSenderIDs(listOfSMS);
        }

    }

    private void sendEmailToFilteredSenderIDs(List<SMSModel> listOfSMS) {
        for (int i = 0; i < listOfSMS.size(); i++) {
            if (listOfSMS.get(i).smsFromNumber.equals("ADDISHTV")) {
                String strMessage = "SMS received on " + listOfSMS.get(i).smsDate + " from " + listOfSMS.get(i).smsFromNumber + " : " + listOfSMS.get(i).smsBody;
                Utils.sendEmail(this, strMessage, sharedPrefUtils.getValue(Utils.RECIPIENT_EMAIL_ID, Utils.DEFAULT_RECIPIENT_EMAIL_ID), () -> {});
            }
        }
    }

    private void requestPermissions() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                .withListener(new MultiplePermissionsListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            Toast.makeText(MainActivity.this, "All the permissions are granted..", Toast.LENGTH_SHORT).show();
                            readSMS();
                        }else if(!multiplePermissionsReport.areAllPermissionsGranted() || multiplePermissionsReport.isAnyPermissionPermanentlyDenied()){
                            showSettingsDialog();
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        // this method is called when user grants some
                        // permission and denies some of them.
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            // this method is use to handle error
            // in runtime permissions
            @Override
            public void onError(DexterError error) {
                // we are displaying a toast message for error message.
                Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
            }
        })
                // below line is use to run the permissions
                // on same thread and to check the permissions
                .onSameThread().check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Need Permissions");

        builder.setMessage("This app needs SMS permission to proceed further. Please grant them from app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101, new Bundle());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}