package com.fimbleenterprises.medimileage.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fimbleenterprises.medimileage.objects_and_containers.FullTrip;
import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.activities.ui.mileage.MileageFragment;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;



public class TripListRecyclerAdapter extends RecyclerView.Adapter<TripListRecyclerAdapter.ViewHolder> {
    private static final String TAG="TripListRecyclerAdapter";
    public ArrayList<FullTrip> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MyPreferencesHelper options;
    public boolean isInEditMode = false;
    MileageFragment mileageFragment;
    Context context;
    Typeface originalTypeface;
    TextView textView;

    Uri imgAssociationPath = Uri.parse("android.resource://com.fimbleenterprises.medimileage/" + R.drawable.exclamation_blue_64);

    // data is passed into the constructor
    public TripListRecyclerAdapter(Context context, ArrayList<FullTrip> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MyPreferencesHelper(context);
    }

    // data is passed into the constructor
    public TripListRecyclerAdapter(Context context, ArrayList<FullTrip> data, MileageFragment callingFrag) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.mileageFragment = callingFrag;
        this.options = new MyPreferencesHelper(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.triplist_row, parent, false);
        textView = view.findViewById(R.id.txtMainText);
        originalTypeface = textView.getTypeface();
        return new ViewHolder(view);
    }


    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FullTrip trip = mData.get(position);

        if (options.isExplicitMode()) {
            holder.txtSubtext.setText(Helpers.Geo.convertMetersToMiles(
                    trip.getDistance(),2) + " fucking miles - " + trip.calculatePrettyReimbursement());
        } else {
            holder.txtSubtext.setText(Helpers.Geo.convertMetersToMiles(
                    trip.getDistance(),2) + " miles - " + trip.calculatePrettyReimbursement());
        }

        // Set special status fields as appropriate
        if (trip.wasAutoKilled()) {
            holder.txtIsAutoStoppedTrip.setText(options.isExplicitMode() ?
                    context.getString(R.string.is_autostopped_explicit) :
                    context.getString(R.string.is_autostopped));
            holder.txtIsAutoStoppedTrip.setTextColor(Color.BLUE);
        } else {
            holder.txtIsAutoStoppedTrip.setText("");
        }

        if (trip.getIsEdited()) {
            holder.txtIsEditedOrManual.setText(options.isExplicitMode() ? context.getString(R.string.is_edited_explicit)
                    : context.getString(R.string.is_edited));
            holder.txtIsEditedOrManual.setTextColor(Color.RED);
        } else if (trip.getIsManualTrip()) {
            holder.txtIsEditedOrManual.setText(options.isExplicitMode() ? context.getString(
                    R.string.is_manual_explicit) : context.getString(R.string.is_manual));
            holder.txtIsEditedOrManual.setTextColor(Color.RED);
        } else {
            holder.txtIsEditedOrManual.setText("");
        }

        if (trip.isChecked) {
            Log.i(TAG, "onBindViewHolder Checked trip found (" + trip.getTripcode() + ")");
        }

        holder.chkbxSelectTrip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                trip.isChecked = b;
                mClickListener.onItemClick(holder.itemView, position);
            }
        });
        holder.imgSubmitStatus.setImageResource((trip.getIsSubmitted())
                ? (int) R.drawable.av_checkmark : (int) R.drawable.upload_icon2_48x48);

        if (options.getDebugMode()) {
            holder.imgHasAssociations.setVisibility(trip.hasAssociations() ? View.VISIBLE : View.GONE);
        } else {
            holder.imgHasAssociations.setVisibility(View.GONE);
        }

        if (trip.isSeparator) {
            holder.txtMainText.setText(trip.getTitle());
            holder.txtMainText.setTypeface(originalTypeface, Typeface.BOLD);
            holder.txtSubtext.setVisibility(View.GONE);
            holder.chkbxSelectTrip.setVisibility(View.GONE);
            holder.layout.setBackground(null);
            holder.imgHasAssociations.setVisibility(View.GONE);
            holder.txtIsAutoStoppedTrip.setVisibility(View.GONE);
            holder.txtIsEditedOrManual.setVisibility(View.GONE);
            holder.imgSubmitStatus.setVisibility(View.GONE);
        } else {
            Log.i(TAG, "onBindViewHolder " + trip.getTitle() + " is auto-killed: " + trip.wasAutoKilled());
            holder.txtMainText.setTypeface(originalTypeface);
            holder.imgSubmitStatus.setVisibility(View.VISIBLE);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)
                    holder.layout.getLayoutParams();
            layoutParams.bottomMargin = 6;
            layoutParams.topMargin = 6;
            holder.layout.setLayoutParams(layoutParams);
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_black_border);
            holder.txtMainText.setText(trip.getTitle() + " - " + Helpers.DatesAndTimes.getPrettyDate
                    (trip.getDateTime()));
            holder.chkbxSelectTrip.setVisibility((isInEditMode) ? View.VISIBLE : View.INVISIBLE);
            holder.chkbxSelectTrip.setChecked(trip.isChecked);
            holder.txtIsEditedOrManual.setVisibility(trip.getIsEditedOrIsManual() ? View.VISIBLE : View.INVISIBLE);
            holder.txtIsAutoStoppedTrip.setVisibility(trip.getTripMinderKilledTrip() ? View.VISIBLE : View.INVISIBLE);
            /*try {
                if (options.showOpportunityOptions()) {
                    if (trip.hasNearbyAssociations == 1) {
                        holder.imgHasAssociations.setImageResource(R.drawable.exclamation_blue_64);
                        holder.imgHasAssociations.setVisibility(View.VISIBLE);
                        Helpers.Animations.pulseAnimation(holder.layout, 1.01f,
                                1.01f, 9000, 350);
                        holder.layout.setBackgroundResource(R.drawable.btn_glass_navy_border);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/
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
        try {
            this.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView imgSubmitStatus;
        TextView txtMainText;
        TextView txtSubtext;
        ImageView imgHasAssociations;
        TextView txtIsEditedOrManual;
        TextView txtIsAutoStoppedTrip;
        CheckBox chkbxSelectTrip;
        RelativeLayout layout;


        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            imgSubmitStatus = itemView.findViewById(R.id.leftIcon);
            txtMainText = itemView.findViewById(R.id.txtMainText);
            txtSubtext = itemView.findViewById(R.id.txtSubtext);
            imgHasAssociations = itemView.findViewById(R.id.imageView_has_associations);
            txtIsEditedOrManual = itemView.findViewById(R.id.txtIsEditedOrManual);
            txtIsAutoStoppedTrip = itemView.findViewById(R.id.txtIsAutoStopped);
            chkbxSelectTrip = itemView.findViewById(R.id.btnDeleteTrip);

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



















































































