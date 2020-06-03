package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.graphics.Typeface;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fimbleenterprises.medimileage.ui.mileage.MileageFragment;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;



public class TripListRecyclerAdapter extends RecyclerView.Adapter<TripListRecyclerAdapter.ViewHolder> {
    private static final String TAG="TripListRecyclerAdapter";
    public ArrayList<FullTrip> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MySettingsHelper options;
    public boolean isInEditMode = false;
    MileageFragment mileageFragment;
    Context context;
    Typeface originalTypeface;
    TextView textView;



    // data is passed into the constructor
    public TripListRecyclerAdapter(Context context, ArrayList<FullTrip> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MySettingsHelper(context);
    }

    // data is passed into the constructor
    public TripListRecyclerAdapter(Context context, ArrayList<FullTrip> data, MileageFragment callingFrag) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.mileageFragment = callingFrag;
        this.options = new MySettingsHelper(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.triplist_row, parent, false);
        textView = view.findViewById(R.id.textView_FullTripRowMainText);
        originalTypeface = textView.getTypeface();
        return new ViewHolder(view);
    }



    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FullTrip trip = mData.get(position);

        holder.txtSubtext.setText(Helpers.Geo.convertMetersToMiles(trip.getDistance(),2) + " miles - " +
                trip.calculatePrettyReimbursement());
        holder.txtManual.setText((trip.getIsManualTrip()) ? "MANUAL" : "");
        holder.txtEdited.setText((trip.getIsEdited()) ? "EDITED" : "");

        if (trip.getIsEdited()) {
            Log.i(TAG, "onBindViewHolder Edited trip");
        }

        holder.chkbxSelectTrip.setVisibility((isInEditMode) ? View.VISIBLE : View.INVISIBLE);
        holder.chkbxSelectTrip.setChecked(trip.isChecked);
        holder.chkbxSelectTrip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                trip.isChecked = b;
            }
        });
        holder.imgSubmitStatus.setImageResource((trip.getIsSubmitted())
                ? (int) R.drawable.av_checkmark : (int) R.drawable.upload_icon2_48x48);

        holder.txtSubtext.setVisibility((trip.isSeparator) ? View.GONE : View.VISIBLE);
        holder.txtManual.setVisibility((trip.isSeparator) ? View.GONE : View.VISIBLE);
        holder.txtEdited.setVisibility((trip.isSeparator) ? View.GONE : View.VISIBLE);
        holder.imgSubmitStatus.setVisibility((trip.isSeparator) ? View.GONE : View.VISIBLE);

        if (trip.isSeparator) {
            holder.txtMainText.setText(trip.getTitle());
            holder.txtMainText.setTypeface(originalTypeface, Typeface.BOLD);
            holder.chkbxSelectTrip.setVisibility(View.GONE);
            holder.layout.setBackground(null);
            holder.imgUserStarted.setVisibility(View.GONE);
            holder.imgUserStopped.setVisibility(View.GONE);
        } else {
            holder.txtMainText.setTypeface(originalTypeface);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.layout.getLayoutParams();
            layoutParams.bottomMargin = 6;
            layoutParams.topMargin = 6;
            holder.layout.setLayoutParams(layoutParams);
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_black_border);
            if (options.getDebugMode()) {
                holder.imgUserStarted.setVisibility(View.VISIBLE);
                holder.imgUserStopped.setVisibility(View.VISIBLE);
                holder.imgUserStarted.setImageResource((trip.getUserStartedTrip())
                        ? R.drawable.green_dot_32x32 : R.drawable.red_dot_32x32);
                holder.imgUserStopped.setImageResource((trip.getUserStoppedTrip())
                        ? R.drawable.green_dot_32x32 : R.drawable.red_dot_32x32);
            } else {
                holder.imgUserStarted.setVisibility(View.GONE);
                holder.imgUserStopped.setVisibility(View.GONE);
            }
            holder.txtMainText.setText(trip.getTitle() + " - " + Helpers.DatesAndTimes.getPrettyDate(trip.getDateTime()));
            holder.chkbxSelectTrip.setVisibility((isInEditMode) ? View.VISIBLE : View.INVISIBLE);
            // holder.txtMainText.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setLongClickable(true);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setEditModeEnabled(boolean value) {
        isInEditMode = value;
        if (! isInEditMode) {
            for (FullTrip trip : mData) {
                trip.isChecked = false;
            }
        }
        this.notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView imgSubmitStatus;
        TextView txtMainText;
        TextView txtSubtext;
        ImageView imgUserStarted;
        ImageView imgUserStopped;
        TextView txtManual;
        TextView txtEdited;
        CheckBox chkbxSelectTrip;
        RelativeLayout layout;


        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            imgSubmitStatus = itemView.findViewById(R.id.imageView_FullTripRowLeftIcon);
            txtMainText = itemView.findViewById(R.id.textView_FullTripRowMainText);
            txtSubtext = itemView.findViewById(R.id.textView_FullTripRowSubtext);
            imgUserStarted = itemView.findViewById(R.id.imageView_started);
            imgUserStopped = itemView.findViewById(R.id.imageView_stopped);
            txtManual = itemView.findViewById(R.id.txtManual);
            txtEdited = itemView.findViewById(R.id.txtEdited);
            chkbxSelectTrip = itemView.findViewById(R.id.checkBox_selectTrip);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mData.get(getAdapterPosition()).isSeparator) {
                return;
            }

            if (isInEditMode) {
                chkbxSelectTrip.setChecked(!chkbxSelectTrip.isChecked());
            }

            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View view) {
            FullTrip clickedTrip = mData.get(getAdapterPosition());
            if (! clickedTrip.isSeparator) {
                Log.i(TAG, "onLongClick " + clickedTrip.toString());


                if (isInEditMode) {
                    chkbxSelectTrip.setChecked(!chkbxSelectTrip.isChecked());
                } else {
                    mileageFragment.setEditMode(true);
                    chkbxSelectTrip.setChecked(!chkbxSelectTrip.isChecked());
                }

            }
            return true;
        }
    }

    // convenience method for getting data at click position
    public FullTrip getItem(int pos) {
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



















































































