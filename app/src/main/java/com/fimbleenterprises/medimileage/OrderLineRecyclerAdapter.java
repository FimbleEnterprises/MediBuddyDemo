package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;


public class OrderLineRecyclerAdapter extends RecyclerView.Adapter<OrderLineRecyclerAdapter.ViewHolder> {
    private static final String TAG="OrderLineAdapter";
    public ArrayList<CrmEntities.OrderProduct> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MySettingsHelper options;
    Context context;
    Typeface originalTypeface;
    TextView textView;



    // data is passed into the constructor
    public OrderLineRecyclerAdapter(Context context, ArrayList<CrmEntities.OrderProduct> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final CrmEntities.OrderProduct orderProduct = mData.get(position);

        if (orderProduct.isSeparator) {
            holder.txtQty.setVisibility(View.GONE);
            holder.txtAmount.setVisibility(View.GONE);
            holder.txtCustName.setVisibility(View.GONE);
            holder.txtDate.setVisibility(View.GONE);
            holder.imgProductIcon.setVisibility(View.GONE);
            holder.itemView.setLongClickable(false);
        } else {
            holder.itemView.setLongClickable(true);
        }

        holder.txtDate.setText(orderProduct.orderdateFormatted);
        holder.txtCustName.setText(orderProduct.customeridFormatted);
        holder.txtAmount.setText(orderProduct.extendedAmtFormatted);
        holder.txtQty.setText(orderProduct.qty + " x ");
        holder.txtPartNumber.setText(orderProduct.partNumber);

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
        TextView txtQty;
        TextView txtDate;
        TextView txtCustName;
        TextView txtAmount;
        RelativeLayout layout;


        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.row_orderproduct);
            imgProductIcon = itemView.findViewById(R.id.img_PartIcon);
            txtPartNumber = itemView.findViewById(R.id.txt_partNumber);
            txtQty = itemView.findViewById(R.id.txt_Qty);
            txtDate = itemView.findViewById(R.id.txt_orderDate);
            txtCustName = itemView.findViewById(R.id.txt_customerName);
            txtAmount = itemView.findViewById(R.id.txt_extendedAmt);

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
            CrmEntities.OrderProduct orderProduct = mData.get(getAdapterPosition());
            if (! orderProduct.isSeparator) {
                Log.i(TAG, "onLongClick " + orderProduct.toString());
            }
            return true;
        }
    }

    // convenience method for getting data at click position
    public CrmEntities.OrderProduct getItem(int pos) {
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



















































































