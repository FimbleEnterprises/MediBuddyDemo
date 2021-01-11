package com.fimbleenterprises.medimileage;

import java.util.ArrayList;

class BasicObjects {

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

}
