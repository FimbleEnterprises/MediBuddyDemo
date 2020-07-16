package com.fimbleenterprises.medimileage;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.medimileage.MySettingsHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import io.grpc.LoadBalancer;

public class Activity_ParkingMap extends FragmentActivity implements OnMarkerClickListener,
		OnInfoWindowClickListener,
		OnMapLongClickListener,
		OnMyLocationChangeListener,
		OnMapClickListener,
		OnCameraChangeListener {

	public static final String SAVED_SPOT_FLAG = "SAVED_SPOT_FLAG";

	public GoogleMap map;

	float distanceSinceLastRouteCalc = 0f;
	Location curLocation;
	LinearLayout llMaster;
	Location locationAtLastRouteCalc;
	boolean firstRouteCalc = true;
	public static float totalDistance = 0;

	PolylineOptions calculatedPolyline;
	PolylineOptions straightPolyline;
	Polyline calculatedLine;
	Polyline straightLine;
	Marker parkMark;

	MapLocationProvider provider;
	float currentZoom = 14f;
	float currentBearing = 0f;
	boolean isCameraMoving = false;

	MySettingsHelper options;
	TextView txtDistance;
	TextView txtWalkingDistance;
	TextView txtAccuracy;
	TextView txtDateTime;
	ProgressBar progress;
	Button btn_ToggleLock;
	Button btn_ToggleMap;
	Location myCurLoc;
	float defaultZoom = 21;

	Location savedSpot;

	CameraPosition defaultPosition;
	// This flag will be true while the camera is moving back to its default position
	boolean cameraIsDefaulting = true;
	static Context myContext;
	final static String TAG = "Activity_ParkingMap.";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		options = new MySettingsHelper(this);
		Log.e(TAG + "onCreate", "Creating!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_map);

		// Show the Up button in the action bar.
		setupActionBar();

		myContext = this;
		provider = new MapLocationProvider(this, false);

		txtDistance = (TextView) findViewById(R.id.TextView_Parking_DistanceValue);
		txtWalkingDistance = (TextView) findViewById(R.id.TextView_Parking_WalkingDistanceValue);
		txtAccuracy = (TextView) findViewById(R.id.TextView_Parking_GPS_Acc);
		txtDateTime = (TextView) findViewById(R.id.TextView_Parking_Timestamp);
		btn_ToggleLock = (Button) findViewById(R.id.btn_ParkingMapOverlay_toggleLock);
		btn_ToggleMap = (Button) findViewById(R.id.btn_ParkingMapOverlay_toggleMap);
		progress = (ProgressBar) findViewById(R.id.progressBar_ParkingWaitingOnGps);
		llMaster = (LinearLayout) findViewById(R.id.layout_master_FindParkingSpot);

		if (getIntent().getParcelableExtra(SAVED_SPOT_FLAG) != null) {
			savedSpot = getIntent().getParcelableExtra(SAVED_SPOT_FLAG);
		}

		((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				map = googleMap;
				if (ActivityCompat.checkSelfPermission(MyApp.getAppContext(), Manifest.permission
						.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(MyApp.getAppContext(),
						Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

					return;
				}
				map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.getUiSettings().setZoomControlsEnabled(false);
                map.setTrafficEnabled(false);
                map.setOnMyLocationChangeListener(Activity_ParkingMap.this);
                map.setOnCameraChangeListener(Activity_ParkingMap.this);
                map.setOnInfoWindowClickListener(Activity_ParkingMap.this);
                map.setOnMapClickListener(Activity_ParkingMap.this);
                map.setOnMapLongClickListener(Activity_ParkingMap.this);
                map.setOnMarkerClickListener(Activity_ParkingMap.this);

                // Have the camera initially hover over the US in toto as opposed to Lat=0 / Lng=0 (which is in the middle of the Atlantic)
                moveCameraToShowUSA(false);

                options = new MySettingsHelper(Activity_ParkingMap.this);
                getSpot();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (myCurLoc != null) {
                            //getSpot();
                            moveCameraToShowMarkers(300, 200);
                            handler.removeCallbacks(this);
                        } else {
                            handler.postDelayed(this, 500);
                        }
                    }
                }, 1100);

				map.setLocationSource(provider);

            }
        });


		
	}
	
	@Override
	protected void onStart() {
		

		
		super.onStart();
	}
	
	@Override
	protected void onPause() {
		provider.deactivate();
		super.onPause();
	}

	// Capture key presses
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// If the button was the 'Back' button then we do stuff
		if (keyCode == KeyEvent.KEYCODE_BACK) { // Key pressed was the 'Back' button
			
			try {
				// Check if the camera is currently centered on our location.  If it isn't then we default it back 
				// to a nice angle such that all points of interest are in view
				if (isCameraOnMyLocationRightNow() == false) {
					btn_ToggleLock.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_padlock_unlocked, 0, 0, 0);
					// Camera is out of position.  Move it to the default position.
					moveCameraToShowMarkers(300, 200);
					return false;
				} else {
					// Camera is already in the default location.  Exit the activity by simply forwarding the back key press as normal to the OS.
					return super.onKeyDown(keyCode, event); 
				}
			} catch (Exception e) {
				// On any error we exit the activity by simply forwarding the back key press as normal to the OS.
				return super.onKeyDown(keyCode, event); 
			}
		}
		// Default action is to pass along the key press to the super method
		return super.onKeyDown(keyCode, event); // -->All others key will work		
	}
	
	/**
	 * Set userSwipedDown the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.activity__parking_map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate userSwipedDown one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		
		// If cameraIsDefaulting it means the activity has just begun and the 
		// camera was moved for the very first time to show the parking spot and our loc.
		// Once this initial camera change finishes we clear the defaulting flag
		// and save the camera's current position and zoom level as the default camera position.
		// These values will be compared against current camera position and zoom when the user
		// presses the "Back" key.  If they match we close the activity, if the don't match 
		// then we move the camera TO the default position and zoom.
		if (cameraIsDefaulting == true) {
			cameraIsDefaulting = false;
			defaultPosition = position;
			defaultZoom = position.zoom;
		}
		
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMyLocationChange(Location arg0) {
		
		curLocation = arg0;
		myCurLoc = arg0;
		int actualValue = (int) arg0.getAccuracy();
		float pct = (getCurrentAccAsPct(arg0.getAccuracy()));
		int val = (int) pct;
		txtAccuracy.setText(val + "% ( \u2245 " + actualValue + " meters )");

		if (savedSpot != null) {
			// Get the distance to the parking spot evry time we move
			double d = arg0.distanceTo(savedSpot);
			String feet = Helpers.Geo.convertMetersToFeet(d, this, false);
			float fltFeet = Float.valueOf(feet);
			float miles = 0;
			DecimalFormat df = new DecimalFormat("#.##");
			
			// Convert to miles if necessary (long way to walk!)
			if (fltFeet > 5280) {
				miles = fltFeet / 5280;
				txtDistance.setText(df.format(miles) + " miles");
			} else {
				txtDistance.setText(feet + " feet");
			}
			// Show the timestamp of the saved parking spot and convert it to teh pretty's
			long strMilis = savedSpot.getTime();
			DateTime timeStamp = new DateTime(strMilis);
			String str;
			String pattern = "E, MMM dd, yyyy hh:mm a";
			DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
			str = fmt.print(timeStamp);			
			txtDateTime.setText(str);
			
			// Clear the map, always
			map.clear();
			// make the parkMarker, always
			makeMarker();
			// Draw straight polyline, always
			drawPolyToParking(arg0, savedSpot);
			
			
			/***************************************************************************************************
		    Check if we should renew the calculated polyline.  If we don't throttle it somewhat it 
			creates what could be almost described as 'flickering' while the existing line is removed
			and the replacement line is drawn.  Not to mention unnecessary Google API calls.     
			****************************************************************************************************/
			// We're going to evaluate this value when determining whether or not to draw the calculated poly.  If 
			// it's null we set it to the current location as that means we haven't drawn it even once yet. 
			if (locationAtLastRouteCalc == null) {
				locationAtLastRouteCalc = arg0;
			}
			
			// Get the distance travelled since the last time the poly's were drawn
			distanceSinceLastRouteCalc = locationAtLastRouteCalc.distanceTo(curLocation);
			// If that distance exceeds an arbitrary distance we clear and redraw them (the marker as well)
			if (calculatedLine == null || distanceSinceLastRouteCalc >= 15f || firstRouteCalc == true) {
				try {
					// Update our last loc so we can try to prevent excessive poly redraws
					locationAtLastRouteCalc = arg0;
					// Check if the absolute distance to the parking spot is less than 175 meters.  If it is then 
					// there's no functional need for the calculated poly (the car should be in sight) so we don't draw it. 
					// We also populate the calculated line's label value to reflect this fact.
					double dblFt = (double) arg0.distanceTo(savedSpot);
					// We want to strip the decimal places off the result otherwise it looks wierd when we're saying, "Probably about: "
					String ft = Helpers.Geo.convertMetersToFeet(Double.valueOf(dblFt), this, false);
					int finalVal = Double.valueOf(ft).intValue();
					// If we're within 175 meters of our parking spot we stop drawing the calculated poly assuming that the car is probably 
					// in sight or close enough that calculated directions aren't going to be useful any longer.  
					if (dblFt >= 175) { // Distance is still farther than 175 - draw the line
						drawMappedPolyToSpot(new LatLng(arg0.getLatitude(), arg0.getLongitude()),
								new LatLng(savedSpot.getLatitude(), savedSpot.getLongitude()));
					} else { // we're close enough, don't draw the line and start approximating the walking distance
						txtWalkingDistance.setText("Probably about " + finalVal + " feet");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// We always trip this flag so that it can only be true once
				firstRouteCalc = false;
			} else { // We found a calculated polyline cached and we havn't travelled far enough and this isn't the first time we've made this check
				// Reuse our cached calculated polyline
				map.addPolyline(calculatedPolyline);
			}
		}

		moveFollowCamera(arg0);

	}
	
	@Override
	public void onMapLongClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {

	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void toggleLock(View v) {
		
		if (curLocation == null) {
			Toast.makeText(this, "Must have a GPS fix on your location first.  Be patient.", Toast.LENGTH_SHORT).show();
			return;
		}
	}
	
	public void toggleMap(View v) {

	}
	
	/** Checks the txtDistance and txtWalkingDistance values for the phrase "Calculating..."
		if found it returns true, false otherwise. */
	public boolean shouldShowProgressBar(boolean waitForGoogle) {
		// Determine whether or not to hide the progress bar
		String textWeHate = "Calculating...";
		String txtDist = txtDistance.getText().toString();
		String txtWalkDist = txtWalkingDistance.getText().toString();
		
		if (waitForGoogle == true) {
			if (txtDist.equals(textWeHate) || txtWalkDist.equals(textWeHate)) {
				return true;
			} else {
				return false;
			}
		} else {
			if (txtDist.equals(textWeHate)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	public boolean isCameraOnMyLocationRightNow() {
		// Get the camera's current LatLng values from its current position as well as the default, startup camera position's LatLng values
		LatLng camLatLng = map.getCameraPosition().target;
		LatLng defLatLng = defaultPosition.target;

		// Add userSwipedDown the lat and lng values for the current camera's position
		double camLatLngSum = camLatLng.latitude + camLatLng.longitude;
		// Add userSwipedDown the lat and lng values from the default camera's position
		double defLatLngSum = defLatLng.latitude + defLatLng.longitude;
		// Get the current camera's zoom
		double curZoom = map.getCameraPosition().zoom;
		
		// Get the difference between the summed camera's latitude and longitude and the default
		// camera position's latitude and longitude
		double positionDelta = camLatLngSum - defLatLngSum;
		// Get the difference between the current camera's zoom level and the default camera position's zoom level
		double zoomDelta = curZoom - defaultZoom;
		
		// Finally add the position and zoom deltas together for a value representing the deviation of the camera's 
		// current position from the camera's default position.  In other words, is the camera's current position
		// balls-on-accurate to the camera's default position?  
		double deviation = Math.abs(positionDelta + zoomDelta);	
		
		// If the result of that formula is anything greater than 0.0 we know the camera's current position is NOT the
		// camera's default position.  As such we will move the camera quickly to the default position (effectively resetting
		// it for the user).  If the result of the formula IS 0.0 then the user probably wants to exit so we'll oblige.
		if (deviation > 0.0) {
			return false;
		} else {
			return true;
		}	
	}
	
/*	private void moveCameraToMe(boolean zoomOutNow) {
		
		Log.d(TAG + "moveCameraToMe", "Moving the camera to my location");
		
		double lat, lng;
		Location myLoc = MyLocationManager.getCurrentLocation();
		
		if (myLoc == null) { 
			return;
		}
		
		lat = myLoc.getLatitude();
		lng = myLoc.getLongitude();		
		float zoom;
		if (zoomOutNow == true) {
			zoom = 14;
		} else {
			zoom = calculateZoomAccordingToSpeed(myLoc.getSpeed());
		}
		CameraPosition currentPlace = new CameraPosition.Builder()
        .target(new LatLng(lat, lng))
        .bearing(myLoc.getBearing()).tilt(90f).zoom(zoom).build();
		map.resetCameraToCurrentAndEnableTripClipping(CameraUpdateFactory.newCameraPosition(currentPlace));
		isCameraMoving = true;
	}	
	*/
	private void moveFollowCamera(Location arg0) {
		
		float rawSpeed = arg0.getSpeed();
		double lat, lng;
		float bearing = arg0.getBearing();
		lat = arg0.getLatitude();
		lng = arg0.getLongitude();
		LatLng latLon = new LatLng(lat, lng);
		
		Log.d(TAG + "Map.onMyLocationChange", "Map has detected a location change.  The cameraFollowLocation flag is currently set to true so we're going to follow the user.");
		float setZoom = currentZoom;
		setZoom = calculateZoomAccordingToSpeed(rawSpeed);
		currentZoom = setZoom;
		currentBearing = bearing;
		
		Log.d(TAG + "Map.onMyLocationChange", "Syncing the camera to the user's bearing since that's what they like");
		CameraPosition currentPlace = new CameraPosition.Builder()
        .target(new LatLng(lat, lng))
        .bearing(currentBearing).tilt(90f).zoom(setZoom).build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
		isCameraMoving = true;
	}
	
	private float calculateZoomAccordingToSpeed(float speedTravelling) {
    	
    	float setZoom = currentZoom;
    	Log.d(TAG , "Speed supplied was: " + speedTravelling);
    	
    	if (speedTravelling < 3) {
			setZoom = 20;
		}
		if (speedTravelling > 5 && speedTravelling < 8) {
			setZoom = 19;
		}
		if (speedTravelling > 11 && speedTravelling < 14) {
			setZoom = 18;
		}
		if (speedTravelling > 16 && speedTravelling < 19) {
			setZoom = 17;
		}
		if (speedTravelling > 21) {
			setZoom = 16;
		}
		
		return setZoom;
		
    }
	
	/** Returns an integer between the values of 0 and 100 which represents a percentage.  Higher is more accurate **/
	public int getCurrentAccAsPct(float accuracy) {
		float a = accuracy;
		if (a > 100f) { a = 100f; }
		float d = a / 100f; // should rslt in a decimal between 0 and 1.  Higher is worse.
		float pct = 1f - d;
		float rslt = pct * 100;
		int intRslt = (int) rslt;
		return intRslt;
	}
	
	public LatLng makeMarker() {
		LatLng spotLatLng = new LatLng(savedSpot.getLatitude(), savedSpot.getLongitude());
		// Build a map marker
		Marker parkingSpot = map.addMarker(new MarkerOptions().position(spotLatLng));
		parkingSpot.setTitle("You're parked here!");
		parkingSpot.showInfoWindow();
		
		return spotLatLng;
	}
	
	public void getSpot() {
		
		LatLng spotLatLng = makeMarker();
		
		txtDistance.setText("Calculating...");
		
		CameraPosition currentPlace = new CameraPosition.Builder()
        .target(spotLatLng)
        .bearing(0f).tilt(0f).zoom(12f).build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
		
	}
	
	/** Using the list of TripEntryObjects extend a polyline through each position **/
	private void drawPolyToParking(Location myLocation, Location savedLocation) {
		// Start building our poly line
	    PolylineOptions line = new PolylineOptions();
	    line.width(5);
	    line.color(Color.RED);
	    line.add(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
	    line.add(new LatLng(savedLocation.getLatitude(), savedLocation.getLongitude()));
	    
	    // Now that the polyline is built we simply add it to the map
	    map.addPolyline(line);
	}
	
	/** Starts the async task that contacts Google and attempts to draw a polyline indicating the driving path one would 
	 *  have to take to arrive at the parking spot's location. **/
	public void drawMappedPolyToSpot(LatLng src, LatLng dest) {
		// Getting URL to the Google Directions API
		new ContactGoogle().execute(src,dest,null);
	}
	
	/** Launches an intent that lets the user select a navigation app which after selection gets handed the parking spot's lat/lng values. **/
 	public void startNavigation(LatLng parkingSpot) {
		Toast.makeText(this, "Starting navigation...", Toast.LENGTH_SHORT).show();
		Location myLocation = map.getMyLocation();
		double myLat = myLocation.getLatitude();
		double myLng = myLocation.getLongitude();
		
		double placeLat = parkingSpot.latitude;
		double placeLng = parkingSpot.longitude;
		
		Uri uri = Uri.parse("http://maps.google.com/maps?saddr=" + myLat + "," + myLng + "&daddr=" + placeLat + "," + placeLng + "&z=" + 14);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
 	
 	/** Effectively moves the camera to show the USA in total from afar.  Meant as the initial camera position **/ 
 	private void moveCameraToShowUSA(boolean animate) {
		
		Log.d(TAG + "moveCameraToMe", "Moving the camera to my location");
		
		double lat, lng;		
		lat = 40.4655;
		lng = -95.5473;	
		Location startingLoc = Helpers.Geo.createLocFromLatLng(new LatLng(lat,lng));
		CameraPosition overUSA = new CameraPosition.Builder()
        .target(new LatLng(lat, lng))
        .bearing(startingLoc.getBearing()).tilt(90f).zoom(3).build();
		if (animate == true) {
			map.animateCamera(CameraUpdateFactory.newCameraPosition(overUSA));
		} else {
			map.moveCamera(CameraUpdateFactory.newCameraPosition(overUSA));
		}
	}
 	
	/** moves the camera to a position such that both the start and end map markers are viewable on screen. 
	 *  cameraAnimationDuration represents how long the animation should take (effectively the speed) in ms **/
	private void moveCameraToShowMarkers(int cameraAnimationDuration, int padding) {
		// Create a new LatLngBounds.Builder object
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		
		// While the camera is in motion we trip this flag.  It will be turned back off when the camera arrives on position.
		cameraIsDefaulting = true;

		// Check that the two markers representing the start and end points are present
		if (savedSpot != null && myCurLoc != null) {
			// Loop through the array (array's a strong term though accurate as there's only two items (start and end points))
			// Add the current marker's position to the builder object
		    builder.include(new LatLng(savedSpot.getLatitude(), savedSpot.getLongitude()));
	    	builder.include(new LatLng(myCurLoc.getLatitude(), myCurLoc.getLongitude()));
			
			// Create a populated LatLngBounds object by calling the builder object's build() method
			LatLngBounds markerBounds = builder.build();
			// Create a CameraUpdate object using the new markerBounds object (and a bit of padding) 
			// that we can feed to the map's resetCameraToCurrentAndEnableTripClipping method to actually and finally move the
			// camera to a position such that both start and end markers are visible. 
			CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(markerBounds, padding);
			
			try {
				// Finally we actually get to move the damn camera
				map.animateCamera(cu, cameraAnimationDuration, null);
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}

		
	}
	
	/** Async class that contacts Google and using the returned data draws a polyline indicating the driving/waling path
	 *  one would have to take to arrive at the saved parking spot **/
	public class ContactGoogle extends AsyncTask<LatLng, LatLng, Boolean>
	{
		/*	Here's how you would call this class and execute your async task:
		
		-= Code begin =-
		String a=GetTodaysDate();
		String b=GetTodaysDate();
		new ContactGoogle().execute(a,b);
		-= Code end =-
		*/
		public String rslt;
		MyMapRouteHelper gmap = new MyMapRouteHelper();
		ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
		Document doc;
		Context context;
		boolean operationFailed = false;

		@Override
		protected void onPreExecute() {
			txtWalkingDistance.setText("Calculating...");
		}
		
		@Override
		protected Boolean doInBackground(LatLng... params) {
			try {
				LatLng startLat = params[0];
				LatLng endLat = params[1];
				doc = gmap.getDocument(startLat, endLat, MyMapRouteHelper.MODE_WALKING);
				latLngList = gmap.getDirection(doc);
				return true;
			} catch (Exception e) {
				operationFailed = true;
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			
			if (result == false) {
				return;
			}
			
			 PolylineOptions line = new PolylineOptions();
			    line.width(5);
			    line.color(Color.BLUE);

			    for (LatLng latLng : latLngList) {
			        line.add(latLng);
			    }
			    
			    calculatedPolyline = line;
			    calculatedLine = map.addPolyline(calculatedPolyline);
			    int dis = gmap.getTotalDistanceInmeters(doc);
			    totalDistance = dis;
			    txtWalkingDistance.setText(Helpers.Geo.convertMetersToFeet(dis, getApplicationContext(), true));
		}
		
	}			

}
