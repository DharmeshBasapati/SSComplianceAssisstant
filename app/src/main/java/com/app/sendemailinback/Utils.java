package com.app.sendemailinback;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

    public static final String SENDER_EMAIL_ID = "Spacestem3@gmail.com";
    public static final String DEFAULT_RECIPIENT_EMAIL_ID = "Spacestem3@gmail.com";
    public static final String SENDER_EMAIL_PASSWORD = "Spacestem@1234";
    public static final String EMAIL_SUBJECT = "Compliance Assistant Alert";
    public static final String FROM_DISPLAY_NAME = "Compliance Assistant";
    public static final String TO_DISPLAY_NAME = "Compliance Assistant";

    public static final String RECIPIENT_EMAIL_ID = "RECIPIENT_EMAIL_ID";
    public static final String CURRENT_OTP = "CURRENT_OTP";
    public static final String VERIFIED_MOBILE_NUMBER = "VERIFIED_MOBILE_NUMBER";
    public static final String IS_MOBILE_VERIFIED = "IS_MOBILE_VERIFIED";
    public static final String IS_EMAIL_VERIFIED = "IS_EMAIL_VERIFIED";

    public static final String SMS_DATE_FORMAT = "dd/MM/yyyy hh:mm a";

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

    public static void sendEmail(Context context, String messageBody, String recipientsEmailId, EmailSendListener emailSendListener) {
        new Thread(() -> {
            try {
//                if (Utils.isOnline(context)) {
                    Log.d("Utils", "sendEmail: Sending started...");
                    MailSender sender = new MailSender(SENDER_EMAIL_ID,
                            SENDER_EMAIL_PASSWORD);
                    sender.sendMail(EMAIL_SUBJECT, messageBody,
                            SENDER_EMAIL_ID, recipientsEmailId);
                    Log.d("Utils", "sendEmail: Sending done...");
                    emailSendListener.onEmailSent();
                    //This is to update the SENT SMS LIST in HOME SCREEN(MainActivity.java)
                    Intent in = new Intent("some_intent_filter");
                    context.sendBroadcast(in);
//                }
            } catch (Exception e) {
                Log.e("SendMail", e.getMessage(), e);
            }
        }).start();
    }

    public static List<SMSModel> loadData(Context context) {
        SharedPrefUtils sharedPrefUtils = SharedPrefUtils.getInstance(context);

        // creating a variable for gson.
        Gson gson = new Gson();

        // below line is to get to string present from our
        // shared prefs if not present setting it as null.
        String json = sharedPrefUtils.getValue("SENT_SMS_LIST", null);

        // below line is to get the type of our array list.
        Type type = new TypeToken<ArrayList<SMSModel>>() {}.getType();

        // in below line we are getting data from gson
        // and saving it to our array list
        List<SMSModel> sentSMSList = gson.fromJson(json, type);

        // checking below if the array list is empty or not
        if (sentSMSList == null) {
            // if the array list is empty
            // creating a new array list.
            sentSMSList = new ArrayList<>();
        }

        return sentSMSList;
    }

    public static void saveData(Context context, SMSModel sentSMS) {

        SharedPrefUtils sharedPrefUtils = SharedPrefUtils.getInstance(context);

        List<SMSModel> sentSMSList = loadData(context);

        sentSMSList.add(sentSMS);

        // creating a new variable for gson.
        Gson gson = new Gson();

        // getting data from gson and storing it in a string.
        String json = gson.toJson(sentSMSList);

        // below line is to save data in shared
        // prefs in the form of string.
        sharedPrefUtils.setValue("SENT_SMS_LIST", json);

        // after saving data we are displaying a toast message.
        //Toast.makeText(context, "Saved Array List to Shared preferences. ", Toast.LENGTH_SHORT).show();
    }

}
