package com.app.sendemailinback;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements onItemClickListener, OnDeclarationListener {

    private final String TAG = MainActivity.class.getSimpleName();
    RecyclerView rvSMS;
    SMSListAdapter smsListAdapter;
    private SharedPrefUtils sharedPrefUtils;
    private LinearLayout tvLabel;
    private BroadcastReceiver broadcastReceiver;
    private CoordinatorLayout coordinatorLayout;
    private BottomSheet bottomSheetDeclaration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        tvLabel = findViewById(R.id.tvLabel);
        rvSMS = findViewById(R.id.rvSMS);
        rvSMS.setLayoutManager(new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false));

        rvSMS.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        bottomSheetDeclaration = new BottomSheet(this);
        bottomSheetDeclaration.setCancelable(false);

        checkPermission();

        registerReceiverToUpdateUIWhenSMSRecd();

    }

    private void checkPermission() {
        PackageManager pm = getPackageManager();
        int hasPerm = pm.checkPermission(
                Manifest.permission.RECEIVE_SMS,
                getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {//If not granted
            Toast.makeText(this, "Permission NOT granted", Toast.LENGTH_SHORT).show();
            bottomSheetDeclaration.show(getSupportFragmentManager(), bottomSheetDeclaration.getTag());
        }else{
            updateSentSMSList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiverToUpdateUIWhenSMSRecd();
    }

    private void registerReceiverToUpdateUIWhenSMSRecd() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateSentSMSList();
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("some_intent_filter"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    private void updateSentSMSList() {
        List<SMSModel> sentSMSList = Utils.loadData(this);

        if (sentSMSList.size() > 0) {
            rvSMS.setVisibility(View.VISIBLE);
            tvLabel.setVisibility(View.GONE);

            List<SMSModel> isEmailedSMSList = new ArrayList<>();
            for (int i = 0; i < sentSMSList.size(); i++) {
                if (sentSMSList.get(i).isEmailed()) {
                    isEmailedSMSList.add(sentSMSList.get(i));
                }
            }

            Collections.reverse(isEmailedSMSList);
            smsListAdapter = new SMSListAdapter(MainActivity.this, isEmailedSMSList, this);
            rvSMS.setAdapter(smsListAdapter);
            smsListAdapter.notifyDataSetChanged();
        } else {
            rvSMS.setVisibility(View.GONE);
            tvLabel.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void readSMS() {
        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        Log.d(TAG, "readSMS: SMS COUNT = " + c.getCount());
        int totalSMS = 0;
        List<SMSModel> listOfSMS = new ArrayList<>();
        totalSMS = c.getCount();
        if (c.moveToFirst()) {
            for (int j = 0; j < 15; j++) {
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
                SMSModel smsModel = new SMSModel(today, number, body, type, true);
                listOfSMS.add(smsModel);

                c.moveToNext();
            }
        }

        c.close();

        if (listOfSMS.size() > 0) {
            smsListAdapter = new SMSListAdapter(MainActivity.this, listOfSMS, this);
            rvSMS.setAdapter(smsListAdapter);
            smsListAdapter.notifyDataSetChanged();
            rvSMS.setVisibility(View.VISIBLE);
            tvLabel.setVisibility(View.GONE);
        }

    }

    private void sendEmailToFilteredSenderIDs(List<SMSModel> listOfSMS) {
        for (int i = 0; i < listOfSMS.size(); i++) {
            if (listOfSMS.get(i).smsFromNumber.equals("ADDISHTV")) {
                String strMessage = "SMS received on " + listOfSMS.get(i).smsDate + " from " + listOfSMS.get(i).smsFromNumber + " : " + listOfSMS.get(i).smsBody;
            }
        }
    }

    private void requestPermissions() {

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.RECEIVE_SMS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Log.i(TAG, "onPermissionGranted: "+response.toString());
                        if (bottomSheetDeclaration != null) {
                            bottomSheetDeclaration.dismiss();
                        }
                        Toast.makeText(MainActivity.this, "SMS permission is granted.", Toast.LENGTH_SHORT).show();
                        updateSentSMSList();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Log.i(TAG, "onPermissionDenied: "+response.toString());
                        Toast.makeText(MainActivity.this, "SMS permission is NOT granted.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

//        Dexter.withActivity(this)
//                .withPermissions( Manifest.permission.RECEIVE_SMS)
//                .withListener(new MultiplePermissionsListener() {
//                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//                    @Override
//                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
//                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
//                            if(bottomSheetDeclaration!=null){
//                                bottomSheetDeclaration.dismiss();
//                            }
//                            Toast.makeText(MainActivity.this, "All the permissions are granted..", Toast.LENGTH_SHORT).show();
//                        } else if (!multiplePermissionsReport.areAllPermissionsGranted() || multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
////                            if(bottomSheetDeclaration!=null){
////                                bottomSheetDeclaration.dismiss();
////                            }
////                            bottomSheetDeclaration.show(getSupportFragmentManager(), bottomSheetDeclaration.getTag());
//                            //showSettingsDialog();
//                            //requestPermissions();
//                        }
//
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//                        // this method is called when user grants some
//                        // permission and denies some of them.
//                        permissionToken.continuePermissionRequest();
//                    }
//                }).withErrorListener(new PermissionRequestErrorListener() {
//            // this method is use to handle error
//            // in runtime permissions
//            @Override
//            public void onError(DexterError error) {
//                Log.e(TAG, "onError: "+error.toString());
//                // we are displaying a toast message for error message.
//                Toast.makeText(getApplicationContext(), "Some Error occurred! ", Toast.LENGTH_SHORT).show();
//            }
//        })
//                // below line is use to run the permissions
//                // on same thread and to check the permissions
//                .onSameThread().check();
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

    @Override
    public void onItemClick(SMSModel smsModel) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_sms_detail);

        ImageView imgShare = bottomSheetDialog.findViewById(R.id.imgShare);
        ImageView imgClose = bottomSheetDialog.findViewById(R.id.imgClose);
        TextView tvSMSFrom = bottomSheetDialog.findViewById(R.id.tvSMSFrom);
        TextView tvSMSBody = bottomSheetDialog.findViewById(R.id.tvSMSBody);
        TextView tvSMSDate = bottomSheetDialog.findViewById(R.id.tvSMSDate);

        imgShare.setOnClickListener(v -> {
            Utils.shareSMS(MainActivity.this,smsModel.smsBody+"\n\n- Sent from Chartered Box Reminder");
            //bottomSheetDialog.dismiss();
        });

        imgClose.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        tvSMSFrom.setText(smsModel.getSmsFromNumber());
        tvSMSDate.setText(smsModel.getSmsDate());
        tvSMSBody.setText(smsModel.getSmsBody());

        bottomSheetDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about_us:
                startActivity(new Intent(this, AboutUsActivity.class));
                return true;
            case R.id.menu_logout:
                //Clear All App Data
                sharedPrefUtils.setValue(Utils.IS_MOBILE_VERIFIED, false);
                sharedPrefUtils.setValue(Utils.VERIFIED_MOBILE_NUMBER, "");
                sharedPrefUtils.setValue(Utils.IS_EMAIL_VERIFIED, false);
                sharedPrefUtils.setValue(Utils.RECIPIENT_EMAIL_ID, "");
                sharedPrefUtils.setValue(Utils.CURRENT_OTP, "");

                //We can also clear Sent SMS list if needed
                //sharedPrefUtils.setValue("SENT_SMS_LIST", null);

                startActivity(new Intent(this,MobileNumberActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAgreeClicked() {
        requestPermissions();
    }

}