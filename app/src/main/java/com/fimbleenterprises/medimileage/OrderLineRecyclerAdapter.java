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

import static com.fimbleenterprises.medimileage.CrmEntities.OrderProducts.OrderProduct;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class OrderLineRecyclerAdapter extends RecyclerView.Adapter<OrderLineRecyclerAdapter.ViewHolder> {
    private static final String TAG="OrderLineAdapter";
    public ArrayList<OrderProduct> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MySettingsHelper options;
    Context context;
    Typeface originalTypeface;
    TextView textView;



    // data is passed into the constructor
    public OrderLineRecyclerAdapter(Context context, ArrayList<OrderProduct> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MySettingsHelper(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_orderproduct, parent, false);
        textView = view.findViewById(R.id.txt_customerName);
        originalTypeface = textView.getTypeface();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final OrderProduct orderProduct = mData.get(position);

        if (orderProduct.isSeparator) {
            // Display
            holder.txtQty.setVisibility(View.GONE);
            holder.txtAmount.setVisibility(View.GONE);
            holder.txtCustName.setVisibility(View.GONE);
            holder.txtDate.setVisibility(View.GONE);
            holder.imgProductIcon.setVisibility(View.GONE);
            holder.txtOrderNumber.setVisibility(View.GONE);
            holder.itemView.setLongClickable(false);
            holder.layout.setBackground(null);
        } else {
            // Display
            holder.itemView.setLongClickable(true);
            holder.txtQty.setVisibility(View.VISIBLE);
            holder.txtAmount.setVisibility(View.VISIBLE);
            holder.txtCustName.setVisibility(View.VISIBLE);
            holder.txtDate.setVisibility(View.VISIBLE);
            holder.txtOrderNumber.setVisibility(View.VISIBLE);
            holder.imgProductIcon.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.layout.getLayoutParams();
            layoutParams.bottomMargin = 6;
            layoutParams.topMargin = 6;
            holder.layout.setLayoutParams(layoutParams);
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_black_border);

            // Values
            holder.txtDate.setText(orderProduct.orderdateFormatted);
            holder.txtCustName.setText(orderProduct.customeridFormatted);
            holder.txtAmount.setText(orderProduct.extendedAmtFormatted);
            holder.txtQty.setText(orderProduct.qty + " x ");
            holder.txtPartNumber.setText(orderProduct.partNumber);
            holder.txtOrderNumber.setText(orderProduct.salesorderidFormatted);
            holder.imgProductIcon.setImageBitmap(Helpers.Bitmaps.getImageIconForPart(orderProduct.partNumber, context));
        }

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
        TextView txtOrderNumber;
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
            txtOrderNumber = itemView.findViewById(R.id.txt_salesOrder);

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
            OrderProduct orderProduct = mData.get(getAdapterPosition());
            if (! orderProduct.isSeparator) {
                Log.i(TAG, "onLongClick " + orderProduct.toString());
            }
            return true;
        }
    }

    // convenience method for getting data at click position
    public OrderProduct getItem(int pos) {
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



















































































