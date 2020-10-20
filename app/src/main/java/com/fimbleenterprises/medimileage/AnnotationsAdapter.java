package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fimbleenterprises.medimileage.CrmEntities.Annotations.Annotation;
import com.google.rpc.Help;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;


public class AnnotationsAdapter extends RecyclerView.Adapter<AnnotationsAdapter.ViewHolder> {
    private static final String TAG="OrderLineAdapter";
    public ArrayList<Annotation> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MySettingsHelper options;
    Context context;
    MediUser curUser = MediUser.getMe();

    // data is passed into the constructor
    public AnnotationsAdapter(Context context, ArrayList<Annotation> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MySettingsHelper(context);
    }

    public void updateAnnotationAndReload(CrmEntities.Annotations.Annotation modifiedAnnotation) {
        for (Annotation annotation : this.mData) {
            if (annotation.annotationid.equals(modifiedAnnotation.annotationid)) {
                annotation = modifiedAnnotation;
                Log.i(TAG, "updateAnnotationAndReload Updated local list, notifying recycler that data has changed...");
                notifyDataSetChanged();
            }
        }
    }

    public void removeAnnotationAndReload(CrmEntities.Annotations.Annotation modifiedAnnotation) {
        for (int i = 0; i < mData.size(); i++) {
            Annotation annotation = mData.get(i);
            if (annotation.annotationid.equals(modifiedAnnotation.annotationid)) {
                mData.remove(i);
                notifyDataSetChanged();
                return;
            }
        }
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_annotation_simple, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Annotation annootation = mData.get(position);

        DateTime created = annootation.createdon;

        int days = Helpers.DatesAndTimes.daysBetween(created);
        if (days <= 3) {
            holder.txtNew.setVisibility(View.VISIBLE);
            Helpers.Animations.pulseAnimation(holder.txtNew, 1.15f, 1.25f, 9999, 900);
        } else {
            holder.txtNew.setVisibility(View.GONE);
        }

        if (annootation.createdByValue.equals(curUser.systemuserid)) {
            holder.txtCreatedBy.setTextColor(Color.parseColor("#19870A"));
        }

        // Show/hide the attachment layout accordingly
        holder.layout_attachments_inner.setVisibility(annootation.isDocument ? View.VISIBLE : View.INVISIBLE);

        holder.txtSubject.setText(annootation.subject);
        holder.txtNoteText.setText(annootation.notetext);
        holder.txtCreatedBy.setText(annootation.createdByName);
        holder.txtCreatedOn.setText(Helpers.DatesAndTimes.getPrettyDateAndTime(annootation.createdon));
        holder.imgLeftIcon.setImageResource(R.drawable.sms_64);
        holder.txtFilename.setText(annootation.filename);
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
        TextView txtNew;
        LinearLayout layout_attachments_outer;
        RelativeLayout layout_attachments_inner;
        TextView txtFilename;
        ImageButton imgFileIcon;
        RelativeLayout layout;


        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.row_annotation);
            imgLeftIcon = itemView.findViewById(R.id.img_NoteLeftIcon);
            txtSubject = itemView.findViewById(R.id.txt_NoteSubject);
            txtNoteText = itemView.findViewById(R.id.txt_NoteBody);
            txtCreatedOn = itemView.findViewById(R.id.txt_NoteCreatedOn);
            txtCreatedBy = itemView.findViewById(R.id.txt_NoteCreatedBy);
            txtNew = itemView.findViewById(R.id.txt_New);
            layout_attachments_outer = itemView.findViewById(R.id.layout_attachments);
            layout_attachments_inner = itemView.findViewById(R.id.layout_attachments_inner);
            txtFilename = itemView.findViewById(R.id.txtFilename);
            imgFileIcon = itemView.findViewById(R.id.imgFileIcon);

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



















































































