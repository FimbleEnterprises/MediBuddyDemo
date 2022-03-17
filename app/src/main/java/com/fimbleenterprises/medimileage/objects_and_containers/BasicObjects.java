package com.fimbleenterprises.medimileage.objects_and_containers;

import android.graphics.Color;

import com.fimbleenterprises.medimileage.R;

import org.joda.time.DateTime;

import java.lang.reflect.Field;
import java.util.ArrayList;

import androidx.annotation.ColorInt;

public class BasicObjects {

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

    /**
     * A simple, container class useful for listviews.
     */
    public static class BasicObject {

        public String topText;
        public String bottomText;
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
        public boolean shouldHighlight;
        public DateTime dateTime;

        public BasicObject() { }

        /**
         * Creates a basic object with the isHeader flag set to true.  Used to break up lists using headers.
         * @param topText The header's title.
         */
        public BasicObject(String topText) {
            this.isHeader = true;
            this.topText = topText;
        }

        /**
         * Creates the simplest BasicObject possessing just a topText and bottomText - no object.
         * @param topText The main text of the object
         * @param bottomText The bottomText text.
         */
        public BasicObject(boolean isHeader, String topText, String bottomText) {
            this.isHeader = isHeader;
            this.topText = topText;
            this.middleText = bottomText;
        }

        /**
         * Creates the simplest BasicObject possessing just a topText and bottomText - no object.
         * @param topText The main text of the object
         */
        public BasicObject(boolean isHeader, String topText) {
            this.isHeader = isHeader;
            this.topText = topText;
            this.middleText = bottomText;
        }

        /**
         * Creates the simplest BasicObject possessing just a topText and bottomText - no object.
         * @param topText The main text of the object
         * @param bottomText The bottomText text.
         */
        public BasicObject(String topText, String bottomText) {
            this.isHeader = false;
            this.topText = topText;
            this.middleText = bottomText;
        }

        /**
         * Probably the most common constructor useful for most any list - topText, bottomText and associated object.
         * @param topText The main text.
         * @param bottomText bottomText.
         * @param object The associated object.
         */
        public BasicObject(String topText, String bottomText, Object object) {
            this.topText = topText;
            this.bottomText = bottomText;
            this.object = object;
            this.iconResource = R.drawable.car_icon_circular;
        }

        public BasicObject(String topText, String bottomText, String middleText, Object object) {
            this.topText = topText;
            this.middleText = middleText;
            this.bottomText = bottomText;
            this.object = object;
            this.iconResource = R.drawable.car_icon_circular;
        }

        public BasicObject(String topText, String bottomText, String middleText, String topRightText, Object object) {
            this.topText = topText;
            this.middleText = middleText;
            this.bottomText = bottomText;
            this.topRightText = topRightText;
            this.object = object;
            this.iconResource = R.drawable.car_icon_circular;
        }

        public BasicObject(String topText, String bottomText, String middleText, String topRightText, String bottomRightText, Object object) {
            this.topText = topText;
            this.middleText = middleText;
            this.bottomText = bottomText;
            this.topRightText = topRightText;
            this.bottomRightText = bottomRightText;
            this.object = object;
            this.iconResource = R.drawable.car_icon_circular;
        }

        @Override
        public String toString() {
            return "Title: " + this.topText + ", bottomText: " + this.bottomText;
        }
        
    }
}
