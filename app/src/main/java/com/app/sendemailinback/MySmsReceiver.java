package com.app.sendemailinback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.app.sendemailinback.Utils.EMAIL_SUBJECT;

public class MySmsReceiver extends BroadcastReceiver {

    private static final String TAG =
            MySmsReceiver.class.getSimpleName();
    public static final String pdu_type = "pdus";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the SMS message.
        SharedPrefUtils sharedPrefUtils = SharedPrefUtils.getInstance(context);
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        StringBuilder strMessage = new StringBuilder();
        String format = bundle.getString("format");
        // Retrieve the SMS message received.
        Object[] pdus = (Object[]) bundle.get(pdu_type);
        if (pdus != null) {
            // Check the Android version.
            boolean isVersionM =
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
            // Fill the msgs array.
            msgs = new SmsMessage[pdus.length];
            SMSModel sentSMS;
            String smsDate = "", smsFromNumber = "", smsType = "";
            StringBuilder smsBody = new StringBuilder();
            for (int i = 0; i < msgs.length; i++) {
                // Check Android version and use appropriate createFromPdu.
                if (isVersionM) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                // Build the message to show.
                if (i == 0) {
                    Date dateFormat = new Date(msgs[i].getTimestampMillis());
                    DateFormat formatter = new SimpleDateFormat(Utils.SMS_DATE_FORMAT, Locale.getDefault());
                    String today = formatter.format(dateFormat);
                    smsDate = today;

                    strMessage = new StringBuilder("Dear customer,<br/><br/>Below SMS has been sent from <b>'" + msgs[i].getOriginatingAddress() + "'</b> at " + today + ".<br/><br/><p style=\"color:#1769aa;\">");
                }

                strMessage.append(msgs[i].getMessageBody());

                smsFromNumber = msgs[i].getOriginatingAddress();
                smsBody.append(msgs[i].getMessageBody());
                smsType = "";

            }

            strMessage.append("</p><br/><br/>Kind Regards," +
                    "<br/>" +
                    "Chartered Box Reminder Team");

            sentSMS = new SMSModel(smsDate, smsFromNumber, smsBody.toString(), smsType, false);

            try {
                // Log and display the SMS message.
                Log.d(TAG, "onReceive: " + strMessage);
                Toast.makeText(context, "SMS Received - " + smsBody.toString(), Toast.LENGTH_LONG).show();

                Utils.sendEmail(context, EMAIL_SUBJECT, strMessage.toString(), sharedPrefUtils.getValue(Utils.RECIPIENT_EMAIL_ID, Utils.DEFAULT_RECIPIENT_EMAIL_ID), sentSMS, () -> Log.d(TAG, "checkIsEmailSent: TRUE"));

                //Utils.saveData(context, sentSMS);//This is to save the RECD SMS, email that RECD SMS, and display that SMS on Home Screen of the app.

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}