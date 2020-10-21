package com.fimbleenterprises.medimileage;

class BasicObject {

    String title;
    String subtitle;
    Object object;
    int iconResource = -1;
    boolean isSelected = false;
    boolean isHeader = false;

    public BasicObject() {  }

    public BasicObject (String name, String subtitle, Object object) {
        this.title = name;
        this.subtitle = subtitle;
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
