package com.fimbleenterprises.medimileage.objects_and_containers;

import com.fimbleenterprises.medimileage.R;

import androidx.annotation.DrawableRes;

public class LandingPageItem {

    public static final int CHANGE_TERRITORY_CODE = -1;
    public String mainText;
    @DrawableRes public int leftIconResouceid;
    @DrawableRes public int rightIconResourceid;
    public int pageIndex;

    public LandingPageItem(String mainText, int pageIndex) {
        this.mainText = mainText;
        this.leftIconResouceid = R.drawable.search;
        this.rightIconResourceid = -1;
        this.pageIndex = pageIndex;
    }

    public LandingPageItem(String mainText, int pageIndex, @DrawableRes int leftIconResourceId, @DrawableRes int rightIconResourceId) {
        this.mainText = mainText;
        this.pageIndex = pageIndex;
        this.leftIconResouceid = leftIconResourceId;
        this.rightIconResourceid = rightIconResourceId;
    }

}
