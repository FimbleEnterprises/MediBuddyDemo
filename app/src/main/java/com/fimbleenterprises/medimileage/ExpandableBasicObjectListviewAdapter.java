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

import com.fimbleenterprises.medimileage.objects_and_containers.BasicObjects;

import java.util.ArrayList;

public class ExpandableBasicObjectListviewAdapter extends BaseExpandableListAdapter {

    ArrayList<Group> groups = new ArrayList<>();
    ArrayList<Group> expandedGroups;
    ExpandableListView lv;
    Holder holder;

    public static final String TAG = "ExpandingListAdapter";

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<BasicObjects> parents;
    Context context;

    public ExpandableBasicObjectListviewAdapter() {
        super();
    }

    public ExpandableBasicObjectListviewAdapter(ArrayList<BasicObjects> parents,
                                      ExpandableListView lv,
                                      Activity activity) {
        this.parents = parents;
        this.expandedGroups = new ArrayList<>();
        this.lv = lv;
        this.activity = activity;
        inflater = activity.getLayoutInflater();

        for (int i = 0; i < parents.size(); i++) {
            groups.add(new Group(i));
        }

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

        /*if (getGroup(groupPosition).expanded) {
            Log.i(TAG, "getGroupView " + groupPosition + " is expanded");
            holder.icon.setImageResource(R.drawable.ic_carot_down);
        } else {
            Log.i(TAG, "getGroupView " + groupPosition + " is NOT expanded");
            holder.icon.setImageResource(R.drawable.ic_carot_right);
        }*/

        holder.title = convertView.findViewById(R.id.textView_ParentMainText);
        holder.value = convertView.findViewById(R.id.textview_parentsubtext);

        holder.title.setText(parents.get(groupPosition).parentObject.title);
        String jobTitle = parents.get(groupPosition).parentObject.title;
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
        BasicObjects parentObject = parents.get(groupPosition);
        ArrayList<BasicObjects.BasicObject> children = parentObject.list;
        BasicObjects.BasicObject child = children.get(childPosition);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_childrow, null);
        }

        holder.title = convertView.findViewById(R.id.child_title);
        holder.value = convertView.findViewById(R.id.textView_childRowSubtext);
        holder.title.setText(children.get(childPosition).title);
        holder.value.setText(children.get(childPosition).subtitle);
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
        /*Group group = (Group) getGroup(groupPosition);
        group.isExpanded(false);*/
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
        return parents.get(groupPosition).list.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
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
        return 0;
        /*try {
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
        }*/
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
        public long id;
        public boolean expanded = false;

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
