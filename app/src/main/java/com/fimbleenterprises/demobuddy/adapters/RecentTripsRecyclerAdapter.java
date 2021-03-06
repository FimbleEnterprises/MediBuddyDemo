package com.fimbleenterprises.demobuddy.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.objects_and_containers.BasicObjects;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class RecentTripsRecyclerAdapter extends RecyclerView.Adapter<RecentTripsRecyclerAdapter.ViewHolder> {
    private static final String TAG="RecentTripsRecyclerAdapter";
    public ArrayList<BasicObjects.BasicObject> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemClickListener mDeleteButtonClickListener;
    private ItemClickListener mEditButtonClickListener;
    MyPreferencesHelper options;
    Context context;
    public int lastKnownPosition = 0;
    public int selectedIndex = -1;
    Typeface face;

    // data is passed into the constructor
    public RecentTripsRecyclerAdapter(Context context, ArrayList<BasicObjects.BasicObject> data,
                                      ItemClickListener deleteButtonClickListener) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MyPreferencesHelper(context);
        face = context.getResources().getFont(R.font.casual);
        this.mDeleteButtonClickListener = deleteButtonClickListener;
        // this.mEditButtonClickListener = editButtonClickListener;
    }

    // data is passed into the constructor
    public RecentTripsRecyclerAdapter(Context context, ArrayList<BasicObjects.BasicObject> data,
                                      ItemClickListener deleteButtonClickListener,
                                      ItemClickListener editButtonClickListener) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MyPreferencesHelper(context);
        face = context.getResources().getFont(R.font.casual);
        this.mDeleteButtonClickListener = deleteButtonClickListener;
        this.mEditButtonClickListener = editButtonClickListener;
    }

    public void setOnEditClickListener(ItemClickListener listener) {
        this.mEditButtonClickListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if(manager instanceof LinearLayoutManager && getItemCount() > 0) {
            LinearLayoutManager llm = (LinearLayoutManager) manager;
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int visiblePosition = llm.findFirstCompletelyVisibleItemPosition();
                    Log.i(TAG, "onScrolled | Top most visible item index: " + visiblePosition);
                    if(visiblePosition > -1) {
                        View v = llm.findViewByPosition(visiblePosition);
                        lastKnownPosition = visiblePosition;
                    }
                }
            });
        }
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_recent_trip, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final BasicObjects.BasicObject object = mData.get(position);

        holder.txtMainText.setText(object.topText);
        holder.txtSubtext.setText(object.bottomText);
        holder.itemView.setLongClickable(true);

        // Hide the icon if it is not explicitly set
        if (object.iconResource != -1) {
            // holder.imgIcon.setImageResource(object.iconResource);
            holder.imgIcon.setVisibility(View.VISIBLE);
            if (object.isSelected) {
                holder.txtMainText.setTypeface(face, Typeface.BOLD_ITALIC);
                holder.txtSubtext.setTypeface(null, Typeface.BOLD_ITALIC);
            }
        } else {
            holder.imgIcon.setVisibility(View.GONE);
        }

        // Hide the main text field if it is null
        holder.txtMainText.setVisibility(object.topText == null ? View.GONE : View.VISIBLE);

        // Hide the sub text field if it is null
        holder.txtSubtext.setVisibility(object.bottomText == null ? View.GONE : View.VISIBLE);

        // Hide/show fields based on whether this object is being used as a header/title row
        holder.txtSubtext.setVisibility(object.isHeader ? View.GONE : View.VISIBLE);
        holder.imgIcon.setVisibility(object.isHeader ? View.GONE : View.VISIBLE);

        if (object.isHeader) {
            holder.txtMainText.setTypeface(face, Typeface.BOLD);
            holder.layout.setBackground(null);
        } else {
            holder.txtMainText.setTypeface(face, Typeface.BOLD);
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_black_border_label_bg);
        }

        if (object.isEmpty) {
            holder.imgIcon.setVisibility(View.INVISIBLE);
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
        RelativeLayout layout;
        ImageView imgIcon;
        TextView txtMainText;
        TextView txtSubtext;
        ImageButton btnDelete;
        ImageButton btnEdit;

        ViewHolder(final View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            imgIcon = itemView.findViewById(R.id.imgLeft);
            txtMainText = itemView.findViewById(R.id.txtMainText);
            txtSubtext = itemView.findViewById(R.id.txtSubtext);
            btnDelete = itemView.findViewById(R.id.imgRight);
            btnEdit = itemView.findViewById(R.id.imgRight2);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDeleteButtonClickListener.onItemClick(itemView, getAdapterPosition());
                }
            });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mEditButtonClickListener.onItemClick(itemView, getAdapterPosition());
                }
            });
        }

        @Override
        public void onClick(View view) {

            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View view) {
            BasicObjects.BasicObject clickedTrip = mData.get(getAdapterPosition());
            return true;
        }
    }

    // convenience method for getting data at click position
    public BasicObjects.BasicObject getItem(int pos) {
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



















































































