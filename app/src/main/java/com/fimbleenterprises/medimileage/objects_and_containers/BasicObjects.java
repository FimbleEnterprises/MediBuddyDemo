package com.fimbleenterprises.medimileage.objects_and_containers;

import android.os.Parcel;
import android.os.Parcelable;

import com.fimbleenterprises.medimileage.R;

import java.util.ArrayList;

public class BasicObjects implements Parcelable {

    public String title;
    public BasicObject parentObject = new BasicObject();

    public BasicObjects() { }

    public BasicObjects(BasicObject parentObject) {
        list = new ArrayList<>();
        this.parentObject = parentObject;
    }

    public BasicObjects(String title, BasicObject parentObject) {
        list = new ArrayList<>();
        this.parentObject = parentObject;
        this.title = title;
    }

    public void add(BasicObject obj) {
        this.list.add(obj);
    }

    public ArrayList<BasicObject> list = new ArrayList<>();

    public BasicObject[] toArray() {
        return this.list.toArray(new BasicObject[this.list.size()]);
    }

    @Override
    public String toString() {
        return this.title + " | " + this.list.size() + " items.";
    }

    protected BasicObjects(Parcel in) {
        title = in.readString();
        parentObject = (BasicObject) in.readValue(BasicObject.class.getClassLoader());
        if (in.readByte() == 0x01) {
            list = new ArrayList<BasicObject>();
            in.readList(list, BasicObject.class.getClassLoader());
        } else {
            list = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeValue(parentObject);
        if (list == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(list);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BasicObjects> CREATOR = new Parcelable.Creator<BasicObjects>() {
        @Override
        public BasicObjects createFromParcel(Parcel in) {
            return new BasicObjects(in);
        }

        @Override
        public BasicObjects[] newArray(int size) {
            return new BasicObjects[size];
        }
    };

    /**
     * A simple, container class useful for listviews.
     */
    public static class BasicObject {

        public String title;
        public String subtitle;
        public String middleText;
        public String topRightText;
        public String bottomRightText;
        /**
         * This is useful to associate the BasicObject with another object.  Don't forget to cast
         * correctly when consuming!
         */
        public Object object;
        public int iconResource = -1;
        public boolean isSelected = false;
        /**
         * If true, list adapters that consume this class will treat this as a header that cannot be clicked.
         */
        public boolean isHeader = false;
        public boolean isEmpty = false;
        public boolean isVisible = true;

        public BasicObject() {
        }

        /**
         * Creates a basic object with the isHeader flag set to true.  Used to break up lists using headers.
         * @param name The header's title.
         */
        public BasicObject(String name) {
            this.isHeader = true;
            this.title = name;
        }

        /**
         * Creates the simplest BasicObject possessing just a name and subtitle - no object.
         * @param name The main text of the object
         * @param subtitle The subtitle text.
         */
        public BasicObject(String name, String subtitle) {
            this.isHeader = false;
            this.title = name;
            this.subtitle = subtitle;
        }

        /**
         * Probably the most common constructor useful for most any list - name, subtitle and associated object.
         * @param name The main text.
         * @param subtitle Subtitle.
         * @param object The associated object.
         */
        public BasicObject(String name, String subtitle, Object object) {
            this.title = name;
            this.subtitle = subtitle;
            this.object = object;
            this.iconResource = R.drawable.car_icon_circular;
        }

        public BasicObject(String name, String subtitle, String middleText, Object object) {
            this.title = name;
            this.middleText = middleText;
            this.subtitle = subtitle;
            this.object = object;
            this.iconResource = R.drawable.car_icon_circular;
        }

        public BasicObject(String name, String subtitle, String middleText, String topRightText, Object object) {
            this.title = name;
            this.middleText = middleText;
            this.subtitle = subtitle;
            this.topRightText = topRightText;
            this.object = object;
            this.iconResource = R.drawable.car_icon_circular;
        }

        public BasicObject(String name, String subtitle, String middleText, String topRightText, String bottomRightText, Object object) {
            this.title = name;
            this.middleText = middleText;
            this.subtitle = subtitle;
            this.topRightText = topRightText;
            this.bottomRightText = bottomRightText;
            this.object = object;
            this.iconResource = R.drawable.car_icon_circular;
        }

        @Override
        public String toString() {
            return "Title: " + this.title + ", Subtitle: " + this.subtitle;
        }

    }
}
