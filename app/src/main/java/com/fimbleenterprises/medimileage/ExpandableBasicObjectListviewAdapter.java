package com.fimbleenterprises.medimileage;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.fragment.app.FragmentActivity;

public class ExpandableBasicObjectListviewAdapter extends BaseExpandableListAdapter {

    ArrayList<Group> groups = new ArrayList<>();

    static class ParentItem {

        String title;
        String value;
        BasicObjects children;

        ParentItem(String title, String value, BasicObjects children) {
            this.title = title;
            this.value = value;
            this.children = children;
        }
    }

    public static final String TAG = "ExpandingListAdapter";

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<ParentItem> parents;
    Context context;

    public ExpandableBasicObjectListviewAdapter() {
        super();
    }

    ArrayList<Group> expandedGroups;
    ExpandableListView lv;
    Holder holder;
    int parentIconImageResource;

    public ExpandableBasicObjectListviewAdapter(ArrayList<ParentItem> parents,
                                      ExpandableListView lv,
                                      int parentIconImageResource,
                                      FragmentActivity activity) {
        this.parents = parents;

        // Remove the children not marked as, "isVisible"
        for (ParentItem parent : parents) {
            BasicObjects cleanChildren = new BasicObjects();
            for (BasicObjects.BasicObject child : parent.children.toArray()) {
                if (child.isVisible) {
                    cleanChildren.list.add(child);
                }
            }
            parent.children = cleanChildren;
        }
        this.expandedGroups = new ArrayList<>();
        this.lv = lv;
        this.parentIconImageResource = parentIconImageResource;
        this.activity = activity;
    }

    public ExpandableBasicObjectListviewAdapter(ArrayList<ParentItem> parents,
                                      ExpandableListView lv,
                                      FragmentActivity activity) {
        this.parents = parents;
        this.expandedGroups = new ArrayList<>();
        this.lv = lv;
        this.parentIconImageResource = R.drawable.ic_location_city_black_24dp;
        this.activity = activity;
    }

    public void setInflater(LayoutInflater inflater, FragmentActivity activity) {
        this.inflater = inflater;
        this.activity = activity;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_parentrow, null);
        }

        /*Group thisGroup = (Group) getGroup(groupPosition);
        if (thisGroup == null || ! exists(thisGroup.id)) {
            Group group = new Group(groupPosition);
            group.isExpanded(isExpanded);
            groups.add(group);
        } else {
            ((Group) getGroup(groupPosition)).isExpanded(isExpanded);
        }*/

        Log.d(TAG, "Group at position: " + groupPosition + " expanded = " + isExpanded);

        holder = new Holder();
        holder.indicator = convertView.findViewById(R.id.parent_indicator);
        holder.icon = convertView.findViewById(R.id.parent_left_icon);
        holder.icon.setImageResource(parentIconImageResource);
        holder.title = convertView.findViewById(R.id.textView_ParentMainText);
        holder.value = convertView.findViewById(R.id.textview_parentsubtext);

        holder.title.setText(parents.get(groupPosition).title);
        String jobTitle = parents.get(groupPosition).value;
        if (jobTitle != null) {
            holder.value.setText(jobTitle);
            holder.value.setVisibility(View.VISIBLE);
        }
        if (isExpanded) {
            holder.indicator.setImageResource(R.drawable.ic_carot_down);
        } else {
            holder.indicator.setImageResource(R.drawable.ic_carot_right);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        ParentItem parentItem = parents.get(groupPosition);
        BasicObjects children = parentItem.children;
        BasicObjects.BasicObject child = children.list.get(childPosition);


        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_childrow, null);
        }

        holder.title = convertView.findViewById(R.id.child_title);
        holder.value = convertView.findViewById(R.id.textView_childRowSubtext);
        holder.title.setText(children.list.get(childPosition).title);
        holder.value.setText(children.list.get(childPosition).subtitle);
        /*convertView.setFocusable(true);
        convertView.setClickable(true);
        convertView.setLongClickable(true);*/

        return convertView;
    }

    public boolean exists(long id) {
        for (Group group : groups) {
            if (group.id == id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        /*Group group = (Group) getGroup(groupPosition);
        group.isExpanded(true);*/
    }

    @Override
    public int getGroupCount() {
        return parents.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return parents.get(groupPosition).children.list.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return 0;
        /*try {
            if (groups == null
                    || groups.size() == 0
                    || groups.get(groupPosition) == null) {
                groups = new ArrayList<>();
                groups.add(new Group(groupPosition));
                return groups.get(groupPosition);
            }
            return groups.get(groupPosition);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }*/
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        try {
            if (groups == null
                    || groups.size() == 0
                    || groups.get(groupPosition) == null) {
                groups = new ArrayList<>();
                groups.add(new Group(groupPosition));
                return groups.get(groupPosition).id;
            }
            return groups.get(groupPosition).id;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    class Group {
        int index;
        int groupPosition;
        long id;
        boolean expanded = false;
        Group(int groupPosition) {
            this.groupPosition = groupPosition;
            this.id = System.currentTimeMillis();
        }

        public void isExpanded(boolean b) {
            this.expanded = b;
        }

        public boolean isExpanded() {
            return this.expanded;
        }
    }

    class Holder {
        TextView title;
        ImageView icon;
        ImageView indicator;
        TextView value;
        boolean isSurgeon = false;
    }





}
