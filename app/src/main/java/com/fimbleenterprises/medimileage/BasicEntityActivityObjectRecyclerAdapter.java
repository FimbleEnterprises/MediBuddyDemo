package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;


public class BasicEntityActivityObjectRecyclerAdapter extends RecyclerView.Adapter<BasicEntityActivityObjectRecyclerAdapter.ViewHolder> {
    private static final String TAG="BasicObjectRecyclerAdapter";
    public ArrayList<BasicEntity.BasicEntityField> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MySettingsHelper options;
    Context context;
    public int selectedIndex = -1;
    Typeface face;

    // data is passed into the constructor
    public BasicEntityActivityObjectRecyclerAdapter(Context context, ArrayList<BasicEntity.BasicEntityField> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MySettingsHelper(context);
        face = context.getResources().getFont(R.font.casual);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.basic_activity_entry_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final BasicEntity.BasicEntityField object = mData.get(position);
        holder.txtMainText.setText(object.value);
        holder.txtLabel.setText(object.label);


        // Hide the middle text field if it is null
        holder.txtMainText.setVisibility(object.value == null ? View.GONE : View.VISIBLE);
        holder.txtLabel.setVisibility(object.showLabel ? View.VISIBLE : View.GONE);

        // holder.imgLabelIcon.setImageResource(object.imgResource);

        if (object.isBold) {
            holder.txtMainText.setTypeface(holder.txtMainText.getTypeface(), Typeface.BOLD);
        }

        holder.txtMainText.setEnabled(object.isEditable);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView txtLabel;
        EditText txtMainText;
        ImageView imgLabelIcon;
        RelativeLayout layout;

        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            txtMainText = itemView.findViewById(R.id.txtValue);
            txtLabel = itemView.findViewById(R.id.txtLabel);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View view) {
            BasicEntity.BasicEntityField clickedTrip = mData.get(getAdapterPosition());
            return true;
        }
    }

    // convenience method for getting data at click position
    public BasicEntity.BasicEntityField getItem(int pos) {
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



















































































