package com.app.sendemailinback;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

@SuppressLint("SpecifyJobSchedulerIdRange")
    public class NetworkSchedulerService extends JobService implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = NetworkSchedulerService.class.getSimpleName();

    private ConnectivityReceiver mConnectivityReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        mConnectivityReceiver = new ConnectivityReceiver(this);
    }

    /**
     * When the app's NetworkConnectionActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        SharedPrefUtils sharedPrefUtils = SharedPrefUtils.getInstance(this);
        String message = isConnected ? "Good! Connected to Internet" : "Sorry! Not connected to internet";
        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        //Add code to send email of those SMSes which are saved in SharedPrefs
        /*if (isConnected) {
            List<SMSModel> sentSMSList = Utils.loadData(this);
            for (int i = 0; i < sentSMSList.size(); i++) {
                SMSModel smsModel = sentSMSList.get(i);
                Log.d(TAG, "onNetworkConnectionChanged: SENT SMS LIST = " + smsModel.isEmailed());
                if (!smsModel.isEmailed()) {
                    Log.d(TAG, "onNetworkConnectionChanged: Sending email for " + smsModel.getSmsBody());
                    //Utils.sendEmail(this, smsModel.smsBody, sharedPrefUtils.getValue(Utils.RECIPIENT_EMAIL_ID, Utils.DEFAULT_RECIPIENT_EMAIL_ID), smsModel, () -> Log.d(TAG, "checkIsEmailSent: TRUE"));
                }
            }
        }*/
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob" + mConnectivityReceiver);
        registerReceiver(mConnectivityReceiver, new IntentFilter(CONNECTIVITY_ACTION));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob");
        unregisterReceiver(mConnectivityReceiver);
        return true;
    }
}
