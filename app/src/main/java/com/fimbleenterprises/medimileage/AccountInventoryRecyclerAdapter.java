package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

import androidx.recyclerview.widget.RecyclerView;


public class AccountInventoryRecyclerAdapter extends RecyclerView.Adapter<AccountInventoryRecyclerAdapter.ViewHolder> {
    private static final String TAG="OrderLineAdapter";
    public ArrayList<CrmEntities.AccountProducts.AccountProduct> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MySettingsHelper options;
    Context context;
    Typeface originalTypeface;
    TextView textView;



    // data is passed into the constructor
    public AccountInventoryRecyclerAdapter(Context context, ArrayList<CrmEntities.AccountProducts.AccountProduct> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MySettingsHelper(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_cust_inventory_item, parent, false);
        textView = view.findViewById(R.id.txt_customerName);
        originalTypeface = textView.getTypeface();
        return new ViewHolder(view);
    }



    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final CrmEntities.AccountProducts.AccountProduct customerInventoryItem = mData.get(position);

        if (customerInventoryItem.isSeparator) {
            // Display
            holder.txtStatus.setVisibility(View.GONE);
            holder.txtSerialNumber.setVisibility(View.GONE);
            holder.txtCustName.setVisibility(View.GONE);
            holder.txtDate.setVisibility(View.GONE);
            holder.imgProductIcon.setVisibility(View.GONE);
            holder.txtIsCapital.setVisibility(View.GONE);
            holder.itemView.setLongClickable(false);
            holder.layout.setBackground(null);
        } else {
            // Display
            holder.itemView.setLongClickable(true);
            holder.txtStatus.setVisibility(View.VISIBLE);
            holder.txtSerialNumber.setVisibility(View.VISIBLE);
            holder.txtCustName.setVisibility(View.VISIBLE);
            holder.txtDate.setVisibility(View.VISIBLE);
            holder.txtIsCapital.setVisibility(View.VISIBLE);
            holder.imgProductIcon.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.layout.getLayoutParams();
            layoutParams.bottomMargin = 6;
            layoutParams.topMargin = 6;
            holder.layout.setLayoutParams(layoutParams);
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_black_border);

            // Values
            holder.txtDate.setText(customerInventoryItem.modifiedOnFormatted);
            holder.txtCustName.setText(customerInventoryItem.accountname);
            holder.txtSerialNumber.setText(customerInventoryItem.serialnumber);
            holder.txtStatus.setText(customerInventoryItem.statusFormatted);
            holder.txtRevision.setText(customerInventoryItem.revision);
            holder.txtPartNumber.setText(customerInventoryItem.partNumber);
            holder.txtIsCapital.setText(customerInventoryItem.isCapital ? "Capital" : "Not capital");
            holder.txtIsCapital.setTextColor(customerInventoryItem.isCapital ? Color.RED : Color.BLACK);
            holder.imgProductIcon.setImageBitmap(Helpers.Bitmaps.getImageIconForPart(customerInventoryItem.partNumber, context));
        }

        holder.txtPartNumber.setText(customerInventoryItem.partNumber);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView imgProductIcon;
        TextView txtPartNumber;
        TextView txtStatus;
        TextView txtDate;
        TextView txtCustName;
        TextView txtSerialNumber;
        TextView txtRevision;
        TextView txtIsCapital;
        RelativeLayout layout;


        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.row_orderproduct);
            imgProductIcon = itemView.findViewById(R.id.img_PartIcon);
            txtPartNumber = itemView.findViewById(R.id.txt_partNumber);
            txtStatus = itemView.findViewById(R.id.txt_status);
            txtRevision = itemView.findViewById(R.id.txt_revision);
            txtDate = itemView.findViewById(R.id.txt_orderDate);
            txtCustName = itemView.findViewById(R.id.txt_customerName);
            txtSerialNumber = itemView.findViewById(R.id.txt_serialnumber);
            txtIsCapital = itemView.findViewById(R.id.txt_is_capital);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mData.get(getAdapterPosition()).isSeparator) {
                return;
            }

            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View view) {
            CrmEntities.AccountProducts.AccountProduct orderProduct = mData.get(getAdapterPosition());
            if (! orderProduct.isSeparator) {
                Log.i(TAG, "onLongClick " + orderProduct.toString());
            }
            return true;
        }
    }

    // convenience method for getting data at click position
    public CrmEntities.AccountProducts.AccountProduct getItem(int pos) {
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



















































































