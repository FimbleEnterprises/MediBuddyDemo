package com.fimbleenterprises.demobuddy.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fimbleenterprises.demobuddy.objects_and_containers.CrmEntities.Annotations.Annotation;
import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.objects_and_containers.MediUser;
import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class AnnotationsAdapter extends RecyclerView.Adapter<AnnotationsAdapter.ViewHolder> {

    public static final String PREFACE_ADDING = "(ADDING)";
    public static final String PREFACE_REMOVING = "(deleting...)";
    public static final String PREFACE_UPDATING = "PREFACE_UPDATING";

    private static final String TAG="OrderLineAdapter";
    public ArrayList<Annotation> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    MyPreferencesHelper options;
    Context context;
    MediUser curUser = MediUser.getMe();

    /**
     * Constructor.  Need a valid context and an arraylist of Annotation objects.
     * @param context A valid context - valid would mean having the ability build and show a dialog.
     * @param data An arraylist of annotation objects.
     */
    public AnnotationsAdapter(Context context, ArrayList<Annotation> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.options = new MyPreferencesHelper(context);
    }

    /**
     * Updates a specific annotation in the local array and then rebuilds the list.
     * @param modifiedAnnotation
     */
    public void updateAnnotationAndReload(Annotation modifiedAnnotation) {
        for (Annotation annotation : this.mData) {
            if (annotation.entityid.equals(modifiedAnnotation.entityid)) {
                annotation = modifiedAnnotation;
                Log.i(TAG, "updateAnnotationAndReload Updated local list, notifying recycler that data has changed...");
                notifyDataSetChanged();
            }
        }
    }

    /**
     * Removes the specified annotation from the adapters local array and then rebuilds the list.
     * @param modifiedAnnotation
     */
    public void removeAnnotationAndReload(Annotation modifiedAnnotation) {
        for (int i = 0; i < mData.size(); i++) {
            Annotation annotation = mData.get(i);
            if (annotation.entityid.equals(modifiedAnnotation.entityid)) {
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

        // The annotation for this row
        final Annotation annootation = mData.get(position);

        // Logic to show/hide the "New" label
        int days = Helpers.DatesAndTimes.daysBetween(annootation.createdon);
        if (days <= 2) {
            holder.txtNew.setVisibility(View.VISIBLE);
            Helpers.Animations.pulseAnimation(holder.txtNew, 1.15f, 1.25f, 9999, 900);
        } else {
            holder.txtNew.setVisibility(View.GONE);
        }

        // Logic to determine if the current user is the author of the current annotation.
        if (annootation.createdByValue.equals(curUser.systemuserid)) {
            holder.txtCreatedBy.setTextColor(Color.parseColor("#19870A"));
            holder.layout.setBackgroundResource(R.drawable.btn_glass_orange_border_white);
        } else {
            holder.txtCreatedBy.setTextColor(Color.parseColor("#19870A"));
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_black_border);
        }

        // Show/hide the attachment layout accordingly
        holder.layout_attachments_inner.setVisibility(annootation.isDocument ? View.VISIBLE : View.INVISIBLE);

        holder.txtSubject.setText(annootation.subject);
        holder.txtNoteText.setText(annootation.notetext);
        holder.txtCreatedBy.setText(annootation.createdByName);
        holder.txtCreatedOn.setText(Helpers.DatesAndTimes.getPrettyDateAndTime(annootation.createdon));
        holder.imgLeftIcon.setImageResource(R.drawable.sms_64);
        holder.txtFilename.setText(annootation.filename);

        holder.layout.setEnabled(!annootation.inUse);

        // If this annotation is an attachment do things like find an appropriate icon for the file type.
        if (annootation.isDocument) {
            int extensionImageResource = Helpers.Bitmaps.returnProperIconResource(annootation.filename, R.drawable.paperclip_icon_64x64, context);
            holder.imgLeftIcon.setImageResource(extensionImageResource);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        // Try to color the ADDING, UPDATING, DELETING text if the subject begins with that string.
        // It should make it more obvious that the current not is in the process of being changed
        // at the server.
        if (holder.txtSubject.getText().toString().startsWith(PREFACE_ADDING)) {
            Log.i(TAG, "onBindViewHolder ");
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_orange_border_bolder);
        } else if (holder.txtSubject.getText().toString().startsWith(PREFACE_REMOVING)) {
            Log.i(TAG, "onBindViewHolder ");
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_orange_border_bolder);
        } else if (holder.txtSubject.getText().toString().startsWith(PREFACE_UPDATING)) {
            Log.i(TAG, "onBindViewHolder ");
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_orange_border_bolder);
        } else {
            holder.layout.setBackgroundResource(R.drawable.btn_glass_gray_black_border);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * The container used to store and recycle views as they are scrolled off screen
     */
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

        private ViewHolder(View itemView) {
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

    /**
     * Enables/allows clicks events to be caught and percolate to the hosting activity/fragment
     * @param itemClickListener A click listener (instantiated by the caller) that can be referenced
     *                          from here in the adapter.
     */
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    /**
     * Parent activity can implement this method to respond to click events.
     */
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}



















































































