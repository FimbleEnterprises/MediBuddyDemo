package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import androidx.recyclerview.widget.RecyclerView;

public class AdapterDbBackups extends RecyclerView.Adapter<AdapterDbBackups.ViewHolder> {

    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public AdapterDbBackups(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_database_restore, parent, false);


        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String db = mData.get(position);
        DateTime dtDateTime = null;
        try {
            dtDateTime = new DateTime(Long.parseLong(db.replace(".db", "")));
        } catch (Exception e) {}

        if (dtDateTime != null) {
            holder.txtDbName.setText(db + "\n" + dtDateTime);
        } else {
            holder.txtDbName.setText(db);
        }

        File file = new File(Environment.getExternalStorageDirectory() + File.separator + mData.get(position));
        holder.txtDbSize.setText(Helpers.Files.convertBytesToKb(file.length()) + " KB");
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtDbName;
        TextView txtDbSize;

        ViewHolder(View itemView) {
            super(itemView);
            txtDbName = itemView.findViewById(R.id.txt_dbname);
            txtDbSize = itemView.findViewById(R.id.txt_filesize);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
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
