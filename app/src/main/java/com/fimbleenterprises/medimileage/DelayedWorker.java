package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.os.Handler;
import android.util.Log;


public class DelayedWorker implements Runnable {
    public static final String TAG = "DoDelayedWork";

    public static final int FIVE_SECONDS = 5000;
    public static final int TEN_SECONDS = 10000;
    public static final int THIRTY_SECONDS = 30000;
    public static final int ONE_MINUTE = 60000;
    public static final int FIVE_MINUTES = 300000;

    private int repeatTimes;
    Context context;
    Handler handler;
    DelayedJob delayedJob;

    public interface DelayedJob {
        void doWork();
        void onComplete(Object object);
    }

    public DelayedWorker(int delayInMillis, DelayedJob delayedJob) {
        handler = new Handler();
        this.delayedJob = delayedJob;
        start(delayInMillis);
    }

    public DelayedWorker(int delayInMillis, int repeatHowManyTimes, DelayedJob delayedJob) {
        handler = new Handler();
        this.delayedJob = delayedJob;
        start(delayInMillis, repeatHowManyTimes);
    }

    public void cancel() {
        handler.removeCallbacks(this);
    }

    private void start(int delayInMillis) {
        handler.postDelayed(this, delayInMillis);
        Log.d(TAG, "Scheduled to run in " + (delayInMillis / 1000) + " seconds.");
    }

    private void start(int delayInMillis, int repeatHowManyTimes) {
        this.repeatTimes = repeatHowManyTimes;
        handler.postDelayed(this, delayInMillis);
        Log.d(TAG, "Scheduled to run in " + (delayInMillis / 1000) + " seconds and repeat " + repeatTimes + " times.");
    }

    @Override
    public final void run() {
        if (this.repeatTimes != 0) {
            for (int i = 0; i < this.repeatTimes; i++) {
                Log.d(TAG, "Has run " + (i + 1) + " times.");
                delayedJob.doWork();
            }
        } else {
            delayedJob.doWork();
        }
    }
}
