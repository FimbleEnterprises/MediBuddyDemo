package com.fimbleenterprises.medimileage.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;

import java.util.ArrayList;

/**
 * An adapter for use with an expandable listview.  Leverages the BasicObject class as well as the
 * BasicObjectGroup class (which is defined as a subclass within this class).
 * <br/><br/>
 * I adapted this from code found here: <br/>
 * https://www.journaldev.com/9942/android-expandablelistview-example-tutorial
 */
public class BasicObjectsExpandableListviewAdapter extends BaseExpandableListAdapter {

    /**
     * An object used to represent the parent/group item and its children for use in a BasicObjectsExpandableListviewAdapter
     * which is a type of expandable listview.
     */
    public static class BasicObjectGroup {
        /**
         * This will be the title of the parent/group list item.
         */
        public String title;
        /**
         * These are the children to be displayed when the parent/group item is expanded.
         */
        public ArrayList<BasicObjects.BasicObject> children = new ArrayList<>();

        @Override
        public String toString() {
            return this.title;
        }
    }

    private Context context;
    private ArrayList<BasicObjectGroup> data;

    /**
     * Constructor for the expandable listview's adapter.
     * @param context A valid context.
     * @param data An arraylist of BasicObjectGroup objects.  This class is defined within this adapter.
     */
    public BasicObjectsExpandableListviewAdapter(Context context, ArrayList<BasicObjectGroup> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public BasicObjects.BasicObject getChild(int groupPosition, int childPosition) {
        /*return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);*/

        return this.data.get(groupPosition).children.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) { return childPosition; }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        BasicObjects.BasicObject data = getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.basic_object_row, null);
        }

        TextView mainText = (TextView) convertView
                .findViewById(R.id.textView_BasicObjectMainText);
        TextView subText = (TextView) convertView
                .findViewById(R.id.textView_BasicObjectRowSubtext);
        TextView topRightText = (TextView) convertView
                .findViewById(R.id.textView_BasicObjectTopRightText);
        TextView middleText = (TextView) convertView
                .findViewById(R.id.textView_BasicObjectRowMiddleText);
        ImageView leftIcon = (ImageView) convertView.findViewById(R.id.imageView_BasicObjectIcon);

        mainText.setText(data.title);
        subText.setText(data.middleText);
        topRightText.setText(data.topRightText);
        middleText.setText(data.middleText);
        leftIcon.setImageResource(data.iconResource);

        mainText.setVisibility(data.title == null ? View.GONE : View.VISIBLE);
        subText.setVisibility(data.subtitle == null ? View.GONE : View.VISIBLE);
        topRightText.setVisibility(data.topRightText == null ? View.GONE : View.VISIBLE);
        middleText.setVisibility(data.middleText == null ? View.GONE : View.VISIBLE);
        leftIcon.setVisibility(data.iconResource == 0 ? View.GONE : View.VISIBLE);

        return convertView;
    }

    @Override
    public int getChildrenCount(int parentPosition) {
        return this.data.get(parentPosition).children.size();
    }

    @Override
    public BasicObjectGroup getGroup(int parentPosition) {
        return this.data.get(parentPosition);
    }

    @Override
    public int getGroupCount() {
        return this.data.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        String listTitle = getGroup(listPosition).title;

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }

        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
