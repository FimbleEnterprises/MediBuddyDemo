package com.fimbleenterprises.medimileage;

import com.google.gson.Gson;

import org.joda.time.DateTime;

public class CachedTrip {
    public long tripcode;
    public int _id;
    public FullTrip fullTrip;
    public String gson;
    public long createdon;

    public CachedTrip(String gson) {
        Gson g = new Gson();
        CachedTrip trip = g.fromJson(gson, CachedTrip.class);
        this.fullTrip = trip.fullTrip;
        this.tripcode = trip.tripcode;
        this._id = trip._id;
        this.createdon = trip.createdon;
        this.gson = gson;
    }

    public String toGson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        DateTime dateTime = new DateTime(this.createdon);
        return this.tripcode + " - " + dateTime.toLocalDateTime();
    }

}
