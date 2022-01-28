package com.fimbleenterprises.medimileage.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;
import com.fimbleenterprises.medimileage.objects_and_containers.LandingPageItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class LandingPageRecyclerAdapter extends RecyclerView.Adapter<LandingPageRecyclerAdapter.ViewHolder> {
    private static final String TAG="BasicObjectRecyclerAdapter";
    public ArrayList<LandingPageItem> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MyPreferencesHelper options;
    Context context;
    public int selectedIndex = -1;
    Typeface face;

    // data is passed into the constructor
    public LandingPageRecyclerAdapter(Context context, ArrayList<LandingPageItem> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MyPreferencesHelper(context);
        face = context.getResources().getFont(R.font.casual);
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_landing_page, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final LandingPageItem landingItem = mData.get(position);

        holder.txtMain.setText(landingItem.mainText);
        holder.imgLeft.setBackgroundResource(landingItem.leftIconResouceid);
        if (landingItem.rightIconResourceid == -1) {
            holder.imgRight.setVisibility(View.INVISIBLE);
        } else {
            holder.imgRight.setBackgroundResource(landingItem.rightIconResourceid);
            holder.imgRight.setVisibility(View.VISIBLE);
        }
        holder.itemView.setLongClickable(true);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        RelativeLayout layout;
        TextView txtMain;
        ImageView imgLeft;
        ImageView imgRight;


        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            txtMain = itemView.findViewById(R.id.txtMainText);
            imgLeft = itemView.findViewById(R.id.imgLeft);
            imgRight = itemView.findViewById(R.id.imgRight);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View view) {
            LandingPageItem clickedItem = mData.get(getAdapterPosition());
            return true;
        }
    }

    // convenience method for getting data at click position
    public LandingPageItem getItem(int pos) {
        return mData.get(pos);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}



















































































