package com.app.sendemailinback;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements onItemClickListener {

    private final String TAG = MainActivity.class.getSimpleName();
    RecyclerView rvSMS;
    SMSListAdapter smsListAdapter;
    private SharedPrefUtils sharedPrefUtils;
    private LinearLayout tvLabel;
    private BroadcastReceiver broadcastReceiver;
    private CoordinatorLayout coordinatorLayout;

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

        requestPermissions();

        updateSentSMSList();

        //registerReceiverToUpdateUIWhenSMSRecd();

        //enableSwipeToDeleteAndUndo();
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
            Collections.reverse(sentSMSList);
            smsListAdapter = new SMSListAdapter(MainActivity.this, sentSMSList, this);
            rvSMS.setAdapter(smsListAdapter);
            smsListAdapter.notifyDataSetChanged();
        } else {
            rvSMS.setVisibility(View.GONE);
            tvLabel.setVisibility(View.VISIBLE);
        }
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                final int position = viewHolder.getAdapterPosition();
                final SMSModel item = smsListAdapter.getData().get(position);

                smsListAdapter.removeItem(position);


                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        smsListAdapter.restoreItem(item, position);
                        rvSMS.scrollToPosition(position);
                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(rvSMS);
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
            smsListAdapter = new SMSListAdapter(MainActivity.this, listOfSMS, this);
            rvSMS.setAdapter(smsListAdapter);
            smsListAdapter.notifyDataSetChanged();

            //sendEmailToFilteredSenderIDs(listOfSMS);
        }

    }

    private void sendEmailToFilteredSenderIDs(List<SMSModel> listOfSMS) {
        for (int i = 0; i < listOfSMS.size(); i++) {
            if (listOfSMS.get(i).smsFromNumber.equals("ADDISHTV")) {
                String strMessage = "SMS received on " + listOfSMS.get(i).smsDate + " from " + listOfSMS.get(i).smsFromNumber + " : " + listOfSMS.get(i).smsBody;
                Utils.sendEmail(this, strMessage, sharedPrefUtils.getValue(Utils.RECIPIENT_EMAIL_ID, Utils.DEFAULT_RECIPIENT_EMAIL_ID), () -> {
                });
            }
        }
    }

    private void requestPermissions() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.RECEIVE_SMS)
                .withListener(new MultiplePermissionsListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            Toast.makeText(MainActivity.this, "All the permissions are granted..", Toast.LENGTH_SHORT).show();
                            //readSMS();
                        } else if (!multiplePermissionsReport.areAllPermissionsGranted() || multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
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
            bottomSheetDialog.dismiss();
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
                startActivity(new Intent(this,AboutUsActivity.class));
                return true;
            case R.id.menu_logout:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}