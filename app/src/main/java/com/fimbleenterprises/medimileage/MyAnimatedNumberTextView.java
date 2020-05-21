package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

public class MyAnimatedNumberTextView {

    TextView view;
    Context context;
    Runnable runner;
    Handler handler = new Handler();
    int target;
    int value;
    double finalVal;
    double originalVal;

    public MyAnimatedNumberTextView(Context context, TextView view) {
        this.context = context;
        this.view = view;
    }

    public void stop() {
        handler.removeCallbacks(runner);
    }

    public void setNewValue(final double finalVal, final double startingVal) {

        this.finalVal = finalVal;
        target = (int) finalVal;
        value = (int) startingVal;

        runner = new Runnable() {
            @Override
            public void run() {
                try {
                    if (target < value) {
                        value -= 1;
                        if (Helpers.Numbers.isEven(value)) {
                            view.setText(Helpers.Numbers.convertToCurrency(value));
                        }
                        handler.postDelayed(this, 1);
                    } else if (target > value) {
                        value += 1;
                        if (Helpers.Numbers.isEven(value)) {
                            view.setText(Helpers.Numbers.convertToCurrency(value));
                        }
                        handler.postDelayed(this, 1);
                    } else {
                        handler.removeCallbacks(runner);
                        view.setText(Helpers.Numbers.convertToCurrency(MyAnimatedNumberTextView.this.finalVal));
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        runner.run();
    }

}
