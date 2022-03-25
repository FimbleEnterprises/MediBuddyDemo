package com.fimbleenterprises.demobuddy.activities.ui.views;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

import com.fimbleenterprises.demobuddy.Helpers;

public class MyAnimatedNumberTextView {

    TextView view;
    Context context;
    Runnable runner;
    Handler handler = new Handler();
    int target;
    int value;
    double finalVal;
    double originalVal;
    public boolean isRunning = false;

    public MyAnimatedNumberTextView(Context context, TextView view) {
        this.context = context;
        this.view = view;
    }

    public void stop() {
        handler.removeCallbacks(runner);
    }

    public void setNewValue(final double finalVal, final double startingVal, final boolean asCurrency) {

        this.finalVal = finalVal;
        target = (int) finalVal;
        value = (int) startingVal;

        runner = new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                try {
                    if (target < value) {
                        value -= 1;
                        if (Helpers.Numbers.isEven(value)) {
                            if (asCurrency) {
                                view.setText(Helpers.Numbers.convertToCurrency(MyAnimatedNumberTextView.this.finalVal));
                            } else {
                                view.setText(Math.round(MyAnimatedNumberTextView.this.finalVal) + " mi");
                            }
                        }
                        handler.postDelayed(this, 1);
                    } else if (target > value) {
                        value += 1;
                        if (Helpers.Numbers.isEven(value)) {
                            if (asCurrency) {
                                view.setText(Helpers.Numbers.convertToCurrency(MyAnimatedNumberTextView.this.finalVal));
                            } else {
                                view.setText(Math.round(MyAnimatedNumberTextView.this.finalVal) + " mi");
                            }
                        }
                        handler.postDelayed(this, 1);
                    } else {
                        handler.removeCallbacks(runner);
                        if (asCurrency) {
                            view.setText(Helpers.Numbers.convertToCurrency(MyAnimatedNumberTextView.this.finalVal));
                        } else {
                            view.setText(Math.round(MyAnimatedNumberTextView.this.finalVal) + " mi");
                        }
                        isRunning = false;
                        return;
                    }
                } catch (Exception e) {
                    isRunning = false;
                    e.printStackTrace();
                }
            }
        };
        runner.run();
    }

}
