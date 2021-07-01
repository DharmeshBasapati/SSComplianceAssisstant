package com.app.sendemailinback;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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
                if (Utils.isOnline(context)) {
                    Log.d("Utils", "sendEmail: Sending started...");
                    MailSender sender = new MailSender(SENDER_EMAIL_ID,
                            SENDER_EMAIL_PASSWORD);
                    sender.sendMail(EMAIL_SUBJECT, messageBody,
                            SENDER_EMAIL_ID, recipientsEmailId);
                    Log.d("Utils", "sendEmail: Sending done...");
                    emailSendListener.onEmailSent();
                }
            } catch (Exception e) {
                Log.e("SendMail", e.getMessage(), e);
            }
        }).start();
    }

}
