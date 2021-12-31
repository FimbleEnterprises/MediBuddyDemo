package com.fimbleenterprises.medimileage.activities.ui.CustomViews;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * This field was made for the purposes of displaying autocomplete entries on zero length.  Meaning,
 * once the field gains focus it shows its autocomplete entries.
 */
public class MyAutoCompleteEditText extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {

    /**
     * Enable this to show on zero length.  Note this gets automatically enabled if you set the
     * setThreshold(int) method to a value less than 1.
     */
    public boolean showAutocompleteOnZeroLength = false;

    public MyAutoCompleteEditText(Context context) {
        super(context);
    }

    public MyAutoCompleteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyAutoCompleteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        super.setOnClickListener(listener);
    }

    @Override
    public void setCompletionHint(CharSequence hint) {
        super.setCompletionHint(hint);
    }

    @Override
    public CharSequence getCompletionHint() {
        return super.getCompletionHint();
    }

    /**
     * Setting this to a value of less than 1 will automatically set showAutocompleteOnZeroLength to true.
     * @param threshold
     */
    @Override
    public void setThreshold(int threshold) {
        showAutocompleteOnZeroLength = threshold < 1;
        super.setThreshold(threshold);
    }

    @Override
    public boolean enoughToFilter() {
        return showAutocompleteOnZeroLength;
    }

    @Override
    public void showDropDown() {
        super.showDropDown();
    }


}
