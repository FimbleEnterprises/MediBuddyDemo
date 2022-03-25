package com.fimbleenterprises.demobuddy.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fimbleenterprises.demobuddy.objects_and_containers.MyMapMarker;
import com.fimbleenterprises.demobuddy.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = "MyInfoWindowAdapter";
    private final View myContentsView;
    private Activity parentActivity;
    private ArrayList<MyMapMarker> myMapMarkers;

    public MyInfoWindowAdapter(Activity activity){
        this.parentActivity = activity;
        myContentsView = parentActivity.getLayoutInflater().inflate(R.layout.infowindow, null);
    }

    public void setMyMapMarkers(ArrayList<MyMapMarker> myMapMarkers) {
        this.myMapMarkers = myMapMarkers;
    }

    @Override
    public View getInfoContents(final Marker marker) {

        if (myMapMarkers == null) {
            return null;
        }

        MyMapMarker selectedMarker = null;
        for (MyMapMarker myMapMarker : this.myMapMarkers) {
            if (myMapMarker.marker.getId().equals(marker.getId())) {
                selectedMarker = myMapMarker;
                Log.i(TAG, "getInfoContents ");
            }
        }

        if (selectedMarker == null) {
            return null;
        }

        ImageView imageView = myContentsView.findViewById(R.id.image);
        TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
        tvTitle.setText(selectedMarker.name);
        final TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));

        final MyMapMarker finalSelectedMarker = selectedMarker;

        tvTitle.setText(selectedMarker.accountAddress.accountName);
        tvSnippet.setText(selectedMarker.accountAddress.customertypeFormatted);

        /*if (selectedMarker.accountAddress != null) {
            if (selectedMarker.accountAddress.productnumber.toLowerCase().startsWith("mq")) {
                imageView.setImageResource(R.drawable.miraq64);
            } else if (selectedMarker.accountAddress.productnumber.toLowerCase().startsWith("vq")) {
                imageView.setImageResource(R.drawable.veriq64);
            } else {
                imageView.setImageResource(R.drawable.car_icon_circular);
            }
        }*/

        return myContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // TODO Auto-generated method stub
        return null;
    }

}
