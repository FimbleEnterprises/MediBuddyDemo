package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fimbleenterprises.medimileage.CrmEntities.Annotations.Annotation;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;


public class AnnotationsAdapter extends RecyclerView.Adapter<AnnotationsAdapter.ViewHolder> {
    private static final String TAG="OrderLineAdapter";
    public ArrayList<Annotation> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MySettingsHelper options;
    Context context;



    // data is passed into the constructor
    public AnnotationsAdapter(Context context, ArrayList<Annotation> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MySettingsHelper(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_annotation, parent, false);
        return new ViewHolder(view);
    }



    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Annotation annootation = mData.get(position);

        holder.txtSubject.setText(annootation.subject);
        holder.txtNoteText.setText(annootation.notetext);
        holder.txtCreatedBy.setText(annootation.createdByName);
        holder.txtCreatedOn.setText(annootation.createdon.toString());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView imgLeftIcon;
        TextView txtSubject;
        TextView txtNoteText;
        TextView txtCreatedOn;
        TextView txtCreatedBy;
        RelativeLayout layout;


        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.row_annotation);
            imgLeftIcon = itemView.findViewById(R.id.img_NoteLeftIcon);
            txtSubject = itemView.findViewById(R.id.txt_NoteSubject);
            txtNoteText = itemView.findViewById(R.id.txt_NoteBody);
            txtCreatedOn = itemView.findViewById(R.id.txt_NoteCreatedOn);
            txtCreatedBy = itemView.findViewById(R.id.txt_NoteCreatedBy);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View view) {
            Annotation annotation = mData.get(getAdapterPosition());
            return true;
        }
    }

    // convenience method for getting data at click position
    public Annotation getItem(int pos) {
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



















































































