package com.fimbleenterprises.medimileage.activities.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.adapters.TripListRecyclerAdapter;
import com.fimbleenterprises.medimileage.objects_and_containers.FullTrip;

import androidx.recyclerview.widget.RecyclerView;

public class MyRecycleViewDivider extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    TripListRecyclerAdapter adapter;

    public MyRecycleViewDivider(Context context, TripListRecyclerAdapter adapter) {
        mDivider = context.getResources().getDrawable(R.drawable.line_divider, null);
        this.adapter = adapter;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            try {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);

                FullTrip trip = adapter.mData.get(i);

                if (! trip.isSeparator) {
                    mDivider.draw(c);
                }
            } catch (Exception e) { }
        }
    }
}
