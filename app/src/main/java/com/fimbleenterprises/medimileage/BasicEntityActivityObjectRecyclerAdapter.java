package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;


public class BasicEntityActivityObjectRecyclerAdapter extends RecyclerView.Adapter<BasicEntityActivityObjectRecyclerAdapter.ViewHolder> {
    private static final String TAG="BasicObjectRecyclerAdapter";
    public ArrayList<BasicEntity.EntityBasicField> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemButtonClickListener mButtonClickListener;
    private TextWatcher mTextWather;
    MySettingsHelper options;
    Context context;
    public int selectedIndex = -1;
    Typeface face;
    public static String editColor = "#33267F00";
    OnFieldsUpdatedListener onFieldsUpdatedListener;
    OnStatusChangedListener onStatusChangedListener;
    BasicEntity.EntityStatusReason currentStatus;

    public interface OnStatusChangedListener {
        void onStatusChanged(BasicEntity.EntityStatusReason oldStatus, BasicEntity.EntityStatusReason newStatus);
    }

    public interface OnFieldsUpdatedListener {
        void onUpdated(ArrayList<BasicEntity.EntityBasicField> fields);
    }

    // data is passed into the constructor
    public BasicEntityActivityObjectRecyclerAdapter(Context context, ArrayList<BasicEntity.EntityBasicField> data, OnFieldsUpdatedListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MySettingsHelper(context);
        face = context.getResources().getFont(R.font.casual);
        this.onFieldsUpdatedListener = listener;
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

        if (field.value.contains("\n")) {
            holder.txtMainText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }

        holder.btnMainText.setText(field.value);
        holder.txtLabel.setText(field.label);

        // Hide the middle text field if it is null
        holder.txtMainText.setVisibility(field.value == null ? View.GONE : View.VISIBLE);
        holder.txtLabel.setVisibility(field.showLabel ? View.VISIBLE : View.GONE);

        // holder.imgLabelIcon.setImageResource(object.imgResource);

        if (field.isBold) {
            holder.txtMainText.setTypeface(holder.txtMainText.getTypeface(), Typeface.BOLD);
        }

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

            // Try to find and select the proper item based on the object's value.
            holder.spinnerMainText.setSelection(field.tryGetValueIndexFromName());
        } else {
            holder.txtMainText.setVisibility(View.VISIBLE);
            holder.spinnerMainText.setVisibility(View.GONE);
            holder.btnMainText.setVisibility(View.GONE);
        }

        if (field.isEditable) {
            if (!field.isReadOnly) {
                holder.spinnerMainText.setEnabled(true);
                holder.txtMainText.setEnabled(true);
                holder.spinnerMainText.setBackgroundColor(Color.parseColor(editColor));
                holder.txtMainText.setBackgroundColor(Color.parseColor(editColor));
            }
        } else {
            holder.spinnerMainText.setEnabled(false);
        }



        // Manage datetime fields
        if (field.isDateField) {
            // do something
        } else if (field.isDateTimeField) {
            // do something
        } else {
            // do something else
        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setOnStatusChangedListener(OnStatusChangedListener statusChangedListener) {
        this.onStatusChangedListener = statusChangedListener;
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

                    try {
                        String selectedValue = ((AppCompatTextView) view).getText().toString();
                        BasicEntity.EntityBasicField field = mData.get(getAdapterPosition());

                        if (!selectedValue.equals(field.value)) {
                            field.value = selectedValue;
                            onFieldsUpdatedListener.onUpdated(mData);
                            Log.i(TAG, "onItemSelected ");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Log.i(TAG, "onNothingSelected ");
                }
            });

            txtMainText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.i(TAG, "beforeTextChanged " + txtMainText.getText().toString());
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.i(TAG, "onTextChanged " + txtMainText.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        Log.i(TAG, "afterTextChanged " + txtMainText.getText().toString());
                        BasicEntity.EntityBasicField field = mData.get(getAdapterPosition());
                        if (!field.value.equals(txtMainText.getText().toString())) {
                            Log.i(TAG, "afterTextChanged Text actually changed!");
                            field.value = txtMainText.getText().toString();
                            onFieldsUpdatedListener.onUpdated(mData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            txtMainText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    try {
                        Log.i(TAG, "onEditorAction ");
                        BasicEntity.EntityBasicField field = mData.get(getAdapterPosition());
                        field.value = txtMainText.getText().toString();
                        onFieldsUpdatedListener.onUpdated(mData);
                        return false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
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
            Log.i(TAG, "onLongClick ");
            return true;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            BasicEntity.EntityBasicField clickedTrip = mData.get(getAdapterPosition());
            Log.i(TAG, "onItemSelected ");
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            BasicEntity.EntityBasicField clickedTrip = mData.get(getAdapterPosition());
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



















































































