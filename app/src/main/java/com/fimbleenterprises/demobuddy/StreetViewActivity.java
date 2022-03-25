package com.fimbleenterprises.demobuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class StreetViewActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

    private static final String TAG = "StreetViewActivity";
    public static final String POSITION_INTENT_TAG = "POSITION";
    public static Context context;
    private StreetViewPanorama mStreetViewPanorama;
    private boolean secondLocation = false;
    public LatLng position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_street_view);

        SupportStreetViewPanoramaFragment streetViewFragment =
                (SupportStreetViewPanoramaFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.googleMapStreetView);

        setTitle("Street View");

        if (getIntent() != null && getIntent().hasExtra(POSITION_INTENT_TAG)) {
            Log.i(TAG, "onCreate | Got an intent that should be a LatLng - will build a streetview!");
            position = (LatLng) getIntent().getParcelableExtra(POSITION_INTENT_TAG);
            streetViewFragment.getStreetViewPanoramaAsync(this);
        }

    }

    public StreetViewPanorama.OnStreetViewPanoramaChangeListener panoramaChangeListener =
            new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
                @Override
                public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
                    // Log.i(TAG, "onStreetViewPanoramaChange | New location: " + streetViewPanoramaLocation.position.toString());
                }
            };

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {

        Log.i(TAG, "onStreetViewPanoramaReady | Streetview is ready!");

        mStreetViewPanorama = streetViewPanorama;

        streetViewPanorama.setPosition(position);
        streetViewPanorama.setStreetNamesEnabled(true);
        streetViewPanorama.setPanningGesturesEnabled(true);
        streetViewPanorama.setZoomGesturesEnabled(true);
        streetViewPanorama.setUserNavigationEnabled(true);
        streetViewPanorama.animateTo(
                new StreetViewPanoramaCamera.Builder().
                        orientation(new StreetViewPanoramaOrientation(20, 20))
                        .zoom(streetViewPanorama.getPanoramaCamera().zoom)
                        .build(), 2000);

        streetViewPanorama.setOnStreetViewPanoramaChangeListener(new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
            @Override
            public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
                if (streetViewPanoramaLocation != null && streetViewPanoramaLocation.links != null) {
                    // location is present
                } else {
                    // location not available
                    Toast.makeText(StreetViewActivity.this, "Street view not available for this spot.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });



    }



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        Log.i(TAG, "onPointerCaptureChanged | " + hasCapture);
    }
}