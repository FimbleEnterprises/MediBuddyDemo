package com.fimbleenterprises.medimileage.activities.ui.CustomViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MyHyperlinkTextview extends androidx.appcompat.widget.AppCompatTextView {

    private TextView textView;
    private String text;

    public MyHyperlinkTextview(Context context) {
        super(context);
        prepare();
    }

    public MyHyperlinkTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepare();
    }

    public MyHyperlinkTextview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        prepare();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        this.text = text.toString();
        SpannableString underlineSpan = new SpannableString(text);
        underlineSpan.setSpan(new UnderlineSpan(), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        super.setText(underlineSpan, type);
        prepare();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void prepare() {
        textView = this;
        final SpannableString notClickedString = new SpannableString(this.text);
        notClickedString.setSpan(new URLSpan(this.text), 0, notClickedString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        final SpannableString clickedString = new SpannableString(this.text);
        clickedString.setSpan(new BackgroundColorSpan(Color.BLUE), 0, notClickedString.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);


        clickedString.setSpan(new UnderlineSpan(), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        notClickedString.setSpan(new UnderlineSpan(), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        textView.setText(clickedString);
                        break;
                    case MotionEvent.ACTION_UP:
                        textView.setText(notClickedString, BufferType.SPANNABLE);
                        v.performClick();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        textView.setText(notClickedString, BufferType.SPANNABLE);
                        break;
                    default:
                        textView.setText(text);
                }
                return true;
            }


        });
    }

}
