package com.fimbleenterprises.medimileage.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.CrmEntities;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;


public class EmailsRecyclerAdapter extends RecyclerView.Adapter<EmailsRecyclerAdapter.ViewHolder> {
    private static final String TAG="OrderLineAdapter";
    public ArrayList<CrmEntities.Emails.Email> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MyPreferencesHelper options;
    Context context;
/*    Typeface originalTypeface;
    TextView textView;*/



    // data is passed into the constructor
    public EmailsRecyclerAdapter(Context context, ArrayList<CrmEntities.Emails.Email> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MyPreferencesHelper(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_emails_list, parent, false);
        /*textView = view.findViewById(R.id.txt_customerName);
        originalTypeface = textView.getTypeface();*/
        return new ViewHolder(view);
    }



    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final CrmEntities.Emails.Email email = mData.get(position);

        // Display
        /*holder.itemView.setLongClickable(true);
        holder.txtStatus.setVisibility(View.VISIBLE);
        holder.txtSerialNumber.setVisibility(View.VISIBLE);
        holder.txtCustName.setVisibility(View.VISIBLE);
        holder.txtDate.setVisibility(View.VISIBLE);
        holder.txtIsCapital.setVisibility(View.VISIBLE);
        holder.imgProductIcon.setVisibility(View.VISIBLE);*/

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.layout.getLayoutParams();
        layoutParams.bottomMargin = 6;
        layoutParams.topMargin = 6;
        holder.layout.setLayoutParams(layoutParams);
        holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_black_border);

        // Values
        // holder.txtDate.setText(customerInventoryItem.modifiedOnFormatted);
        holder.txtTopRight.setText(email.createdOnFormatted); // Changed from modifiedon value to new_physical_date (1.83)
        holder.txtTop.setText(email.subject);
        holder.txtBottomRight.setText(email.activityTypeCodeFormatted);
        holder.txtBottom.setText(email.senderFormatted);
        holder.middle.setText(email.regardingFormatted);
        // holder.middle.loadDataWithBaseURL(null, email.description, "text/html; charset=utf-8", "UTF-8", null);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        RelativeLayout layout;
        ImageView imgProductIcon;
        TextView txtBottom;
        TextView txtBottomRight;
        TextView txtTopRight;
        TextView txtTop;
        TextView middle;


        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            txtBottom = itemView.findViewById(R.id.txtBottom);
            txtBottomRight = itemView.findViewById(R.id.txtBottomRight);
            txtTop = itemView.findViewById(R.id.txtTop);
            txtTopRight = itemView.findViewById(R.id.txtTopRight);
            middle = itemView.findViewById(R.id.txtMiddle);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View view) {

            return true;
        }
    }

    // convenience method for getting data at click position
    public CrmEntities.Emails.Email getItem(int pos) {
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



















































































