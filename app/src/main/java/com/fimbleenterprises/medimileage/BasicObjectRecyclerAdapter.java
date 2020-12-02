package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;


public class BasicObjectRecyclerAdapter extends RecyclerView.Adapter<BasicObjectRecyclerAdapter.ViewHolder> {
    private static final String TAG="BasicObjectRecyclerAdapter";
    public ArrayList<BasicObject> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MySettingsHelper options;
    Context context;
    public int selectedIndex = -1;
    Typeface face;

    // data is passed into the constructor
    public BasicObjectRecyclerAdapter(Context context, ArrayList<BasicObject> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MySettingsHelper(context);
        face = context.getResources().getFont(R.font.casual);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.basic_object_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final BasicObject object = mData.get(position);

        holder.txtMainText.setText(object.title);
        holder.txtSubtext.setText(object.subtitle);
        holder.txtMiddleText.setText(object.middleText);
        holder.txtTopRightText.setText(object.topRightText);
        holder.itemView.setLongClickable(true);

        // Hide the icon if it is not explicitly set
        if (object.iconResource != -1) {
            holder.img.setImageResource(object.iconResource);
            holder.img.setVisibility(View.VISIBLE);
            if (object.isSelected) {
                holder.img.setImageResource(R.drawable.av_checkmark);
                holder.txtMainText.setTypeface(face, Typeface.BOLD_ITALIC);
                holder.txtSubtext.setTypeface(null, Typeface.BOLD_ITALIC);
            }
        } else {
            holder.img.setVisibility(View.GONE);
        }

        // Hide the main text field if it is null
        holder.txtMainText.setVisibility(object.title == null ? View.GONE : View.VISIBLE);

        // Hide the sub text field if it is null
        holder.txtSubtext.setVisibility(object.subtitle == null ? View.GONE : View.VISIBLE);

        // Hide the middle text field if it is null
        holder.txtMiddleText.setVisibility(object.middleText == null ? View.GONE : View.VISIBLE);

        // Hide the middle text field if it is null
        holder.txtTopRightText.setVisibility(object.topRightText == null ? View.GONE : View.VISIBLE);

        // Hide/show fields based on whether this object is being used as a header/title row
        holder.txtSubtext.setVisibility(object.isHeader ? View.GONE : View.VISIBLE);
        holder.img.setVisibility(object.isHeader ? View.GONE : View.VISIBLE);

        if (object.isHeader) {
            holder.txtMainText.setTypeface(face, Typeface.BOLD);
            holder.layout.setBackground(null);
        } else {
            holder.txtMainText.setTypeface(face, Typeface.BOLD);
            holder.layout.setBackgroundResource(R.drawable.rounded_container_white_thin_border);
        }

        if (object.isEmpty) {
            holder.txtMiddleText.setTypeface(face, Typeface.NORMAL);
            holder.txtMiddleText.setText(object.title);
            holder.txtMiddleText.setVisibility(View.VISIBLE);
            holder.img.setVisibility(View.INVISIBLE);
            holder.txtMainText.setVisibility(View.INVISIBLE);
            holder.txtSubtext.setVisibility(View.INVISIBLE);
        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView txtMainText;
        TextView txtMiddleText;
        TextView txtSubtext;
        TextView txtTopRightText;
        RelativeLayout layout;
        ImageView img;


        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            txtMainText = itemView.findViewById(R.id.textView_BasicObjectMainText);
            txtMiddleText = itemView.findViewById(R.id.textView_BasicObjectRowMiddleText);
            txtSubtext = itemView.findViewById(R.id.textView_BasicObjectRowSubtext);
            txtTopRightText = itemView.findViewById(R.id.textView_BasicObjectTopRightText);
            img = itemView.findViewById(R.id.imageView_BasicObjectIcon);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View view) {
            BasicObject clickedTrip = mData.get(getAdapterPosition());
            return true;
        }
    }

    // convenience method for getting data at click position
    public BasicObject getItem(int pos) {
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



















































































