package com.fimbleenterprises.medimileage;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class BasicObjects implements Parcelable {

    String title;
    BasicObject parentObject = new BasicObject();

    public BasicObjects(BasicObject parentObject) {
        list = new ArrayList<>();
        this.parentObject = parentObject;
    }

    public BasicObjects(String title, BasicObject parentObject) {
        list = new ArrayList<>();
        this.parentObject = parentObject;
        this.title = title;
    }

    public ArrayList<BasicObject> list = new ArrayList<>();

    public BasicObject[] toArray() {
        return this.list.toArray(new BasicObject[this.list.size()]);
    }

    public static class BasicObject {

        String title;
        String subtitle;
        String middleText;
        String topRightText;
        String bottomRightText;
        Object object;
        int iconResource = -1;
        boolean isSelected = false;
        boolean isHeader = false;
        boolean isEmpty = false;
        boolean isVisible = true;

        public BasicObject() {
        }

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

        public BasicObject(String name) {
            this.isHeader = true;
            this.title = name;
        }

        @Override
        public String toString() {
            return "Title: " + this.title + ", Subtitle: " + this.subtitle;
        }

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
}
