package com.app.sendemailinback;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class EmailWorker extends Worker{
    public EmailWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        mailCachedSMS();
        return Result.success();
    }

    private void mailCachedSMS() {

    }
}
