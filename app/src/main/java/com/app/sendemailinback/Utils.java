package com.app.sendemailinback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.core.app.ShareCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

    public static final String SENDER_EMAIL_ID = "Spacestem3@gmail.com";
    public static final String SENDER_EMAIL_PASSWORD = "Spacestem@1234";

    public static final String DEFAULT_RECIPIENT_EMAIL_ID = "Spacestem3@gmail.com";
    public static final String EMAIL_SUBJECT = "Chartered Box Reminder";
    public static final String EMAIL_VERIFICATION_SUBJECT  = "Chartered Box Reminder Email Verification";
    public static final String FROM_DISPLAY_NAME = "Chartered Box Reminder";
    public static final String TO_DISPLAY_NAME = "Chartered Box Reminder";

    public static final String RECIPIENT_EMAIL_ID = "RECIPIENT_EMAIL_ID";
    public static final String CURRENT_OTP = "CURRENT_OTP";
    public static final String VERIFIED_MOBILE_NUMBER = "VERIFIED_MOBILE_NUMBER";
    public static final String IS_MOBILE_VERIFIED = "IS_MOBILE_VERIFIED";
    public static final String IS_EMAIL_VERIFIED = "IS_EMAIL_VERIFIED";

//    public static final String SMS_DATE_FORMAT = "hh:mm a dd/MM/yyyy ";
    public static final String SMS_DATE_FORMAT = "HH:MM dd-MMM-yy ";

    public static void shareSMS(Activity activity, String textToShare) {
        Intent intent2 = new Intent();
        intent2.setAction(Intent.ACTION_SEND);
        intent2.setType("text/plain");
        intent2.putExtra(Intent.EXTRA_TEXT, textToShare );
        activity.startActivity(Intent.createChooser(intent2, "Share via"));

    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static String getConnectivityStatusString(Context context) {
        String status = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                status = "Wifi enabled";
                return status;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                status = "Mobile data enabled";
                return status;
            }
        } else {
            status = "No internet is available";
            return status;
        }
        return null;
    }

    public static void sendEmail(Context context, String emailSubject, String messageBody, String recipientsEmailId, SMSModel sentSMS, EmailSendListener emailSendListener) {
        new Thread(() -> {
            try {
                if (Utils.isOnline(context)) {
                    Log.d("CBR-MAIL-SENDER - ", "sendEmail: Sending started...");
                    MailSender sender = new MailSender(SENDER_EMAIL_ID,
                            SENDER_EMAIL_PASSWORD);
                    sender.sendMail(emailSubject, messageBody,
                            SENDER_EMAIL_ID, recipientsEmailId);
                    Log.d("CBR-MAIL-SENDER - ", "sendEmail: Sending done...");
                    emailSendListener.onEmailSent();

                    if (sentSMS != null) {
                        sentSMS.setEmailed(true);
                    }
                }

                if (sentSMS != null) {
                    Utils.saveData(context, sentSMS);
                }

                //This is to update the SENT SMS LIST in HOME SCREEN(MainActivity.java)
                Intent in = new Intent("some_intent_filter");
                context.sendBroadcast(in);

            } catch (Exception e) {
                Log.e("CBR-MAIL-SENDER - ", e.getMessage(), e);
            }
        }).start();
    }

    public static List<SMSModel> loadData(Context context) {
        SharedPrefUtils sharedPrefUtils = SharedPrefUtils.getInstance(context);

        Gson gson = new Gson();

        String json = sharedPrefUtils.getValue("SENT_SMS_LIST", null);

        Type type = new TypeToken<ArrayList<SMSModel>>() {
        }.getType();

        List<SMSModel> sentSMSList = gson.fromJson(json, type);

        if (sentSMSList == null) {
            sentSMSList = new ArrayList<>();
        }

        return sentSMSList;
    }

    public static void saveData(Context context, SMSModel sentSMS) {

        Log.d("###UTILS", "savedData: " + sentSMS.toString());

        SharedPrefUtils sharedPrefUtils = SharedPrefUtils.getInstance(context);

        List<SMSModel> sentSMSList = loadData(context);

        sentSMSList.add(sentSMS);

        Gson gson = new Gson();

        String json = gson.toJson(sentSMSList);

        sharedPrefUtils.setValue("SENT_SMS_LIST", json);

    }

}
