package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;


public class BasicEntityActivityObjectRecyclerAdapter extends RecyclerView.Adapter<BasicEntityActivityObjectRecyclerAdapter.ViewHolder> {
    private static final String TAG="BasicObjectRecyclerAdapter";
    public ArrayList<BasicEntity.EntityBasicField> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemButtonClickListener mButtonClickListener;
    private ItemSelectedListener mSpinnerChangeListener;
    MySettingsHelper options;
    Context context;
    public int selectedIndex = -1;
    Typeface face;



    // data is passed into the constructor
    public BasicEntityActivityObjectRecyclerAdapter(Context context, ArrayList<BasicEntity.EntityBasicField> data) {
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
        final BasicEntity.EntityBasicField field = mData.get(position);
        holder.txtMainText.setText(field.value);
        holder.btnMainText.setText(field.value);
        holder.txtLabel.setText(field.label);

        // Hide the middle text field if it is null
        holder.txtMainText.setVisibility(field.value == null ? View.GONE : View.VISIBLE);
        holder.txtLabel.setVisibility(field.showLabel ? View.VISIBLE : View.GONE);

        // holder.imgLabelIcon.setImageResource(object.imgResource);

        if (field.isBold) {
            holder.txtMainText.setTypeface(holder.txtMainText.getTypeface(), Typeface.BOLD);
        }

        holder.txtMainText.setEnabled(field.isEditable);

        if (field.isAccountField) {
            holder.txtMainText.setVisibility(View.GONE);
            holder.spinnerMainText.setVisibility(View.GONE);
            holder.btnMainText.setVisibility(View.VISIBLE);
            holder.btnMainText.setPaintFlags(holder.btnMainText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else if (field.isOptionSet) {
            holder.txtMainText.setVisibility(View.GONE);
            holder.btnMainText.setVisibility(View.GONE);
            holder.spinnerMainText.setVisibility(View.VISIBLE);
            ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, field.toOptionsetValueArray());
            holder.spinnerMainText.setAdapter(arrayAdapter);
        } else {
            holder.txtMainText.setVisibility(View.VISIBLE);
            holder.spinnerMainText.setVisibility(View.GONE);
            holder.btnMainText.setVisibility(View.GONE);
        }

        holder.txtMainText.setEnabled(field.isEditable);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, Spinner.OnItemSelectedListener {
        TextView txtLabel;
        EditText txtMainText;
        ImageView imgLabelIcon;
        Button btnMainText;
        Spinner spinnerMainText;
        RelativeLayout layout;

        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            txtMainText = itemView.findViewById(R.id.txtValue);
            btnMainText = itemView.findViewById(R.id.btnValue);
            btnMainText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mButtonClickListener.onItemButtonClick(view, getAdapterPosition());
                }
            });
            spinnerMainText = itemView.findViewById(R.id.spinnerValue);
            spinnerMainText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // if (mSpinnerChangeListener != null) mSpinnerChangeListener.onItemSelected(view, getAdapterPosition(), mData.get(getAdapterPosition()).optionSetValues);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

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
            BasicEntity.EntityBasicField clickedTrip = mData.get(getAdapterPosition());
            return true;
        }


        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            /*BasicEntity.EntityOptionSetField optionSetField = (BasicEntity.EntityOptionSetField) mData.get(getAdapterPosition());
            if (mSpinnerChangeListener != null) mSpinnerChangeListener.onItemSelected(view, getAdapterPosition(), optionSetField);*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    // convenience method for getting data at click position
    public BasicEntity.EntityBasicField getItem(int pos) {
        return mData.get(pos);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // allows clicks events to be caught
    public void setButtonClickListener(ItemButtonClickListener itemButtonClickListener) {
        this.mButtonClickListener = itemButtonClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // parent activity will implement this method to respond to click events
    public interface ItemButtonClickListener {
        void onItemButtonClick(View view, int position);
    }

    public interface ItemSelectedListener {
        // void onItemSelected(View view, int position, BasicEntity.EntityOptionSetField object);
    }
}



















































































