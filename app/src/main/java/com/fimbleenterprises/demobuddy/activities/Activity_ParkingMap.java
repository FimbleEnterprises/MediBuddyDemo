package com.fimbleenterprises.demobuddy.activities;

import android.Manifest;
import android.app.Activity;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fimbleenterprises.demobuddy.Helpers;
import com.fimbleenterprises.demobuddy.MyApp;
import com.fimbleenterprises.demobuddy.MyMapRouteHelper;
import com.fimbleenterprises.demobuddy.MyPreferencesHelper;
import com.fimbleenterprises.demobuddy.MySqlDatasource;
import com.fimbleenterprises.demobuddy.R;
import com.fimbleenterprises.demobuddy.StreetViewActivity;
import com.fimbleenterprises.demobuddy.dialogs.MyProgressDialog;
import com.fimbleenterprises.demobuddy.dialogs.MyYesNoDialog;
import com.fimbleenterprises.demobuddy.objects_and_containers.FullTrip;
import com.fimbleenterprises.demobuddy.objects_and_containers.SavedParkingSpot;
import com.fimbleenterprises.demobuddy.objects_and_containers.TripEntry;
import com.fimbleenterprises.demobuddy.services.FollowMeLocationSource;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class Activity_ParkingMap extends AppCompatActivity implements OnMarkerClickListener {

	public static final String SAVED_SPOT_FLAG = "SAVED_SPOT_FLAG";
	private static final int MAP_PADDING = 125;// In meters
	public static final float MIN_DISTANCE_BETWEEN_ROUTE_CALCS = 300f;
	public static final String DO_STREETVIEW = "DO_STREETVIEW";

	Activity activity;
	public GoogleMap map;
	private FollowMeLocationSource followMeLocationSource;

	Button btnSaveSpot;
	Button btnClearSpot;
	Button btnToggleMap;
	Button btnToggleCameraLock;
	Button btnStreetview;
	FloatingActionButton fabNavigate;

	RelativeLayout layoutMapView;

	FollowMeLocationSource mapSource;

	float distanceSinceLastRouteCalc = 0f;
	Location curLocation;
	RelativeLayout layoutMaster;
	Location locationAtLastRouteCalc;
	boolean firstRouteCalc = true;
	public static double totalDistance = 0;

	PolylineOptions calculatedPolyline;
	PolylineOptions straightPolyline;
	Polyline calculatedLine;
	Polyline straightLine;
	Marker parkMark;
	public static boolean isLocked = false;
	public static boolean isCalculatingRoute = false;

	// MapLocationProvider provider;
	float currentZoom = 14f;
	float currentBearing = 0f;
	boolean userOverrideCamera = false;
	TextView txtDistance;
	TextView txtWalkingDistance;
	TextView txtAccuracy;
	TextView txtDateTime;
	ProgressBar progress;
	Location myCurLoc;
	float defaultZoom = 21;

	public MyProgressDialog waitingForLocationProgress;

	// Location savedSpot;
	MyPreferencesHelper prefs;
	CameraPosition defaultPosition;
	// This flag will be true while the camera is moving back to its default position
	boolean cameraIsDefaulting = true;
	boolean cameraIsMoving = false;
	boolean userInitiatedCameraMove = false;
	static Context context;
	final static String TAG = "Activity_ParkingMap.";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		prefs = new MyPreferencesHelper(this);
		Log.e(TAG + "onCreate", "Creating!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_map);
		activity = this;

		layoutMapView = findViewById(R.id.layout_MapView);
		// layoutMapView.setVisibility(View.INVISIBLE);
		waitingForLocationProgress = new MyProgressDialog(activity, "Waiting for a good location...", MyProgressDialog.PROGRESS_TYPE);
		waitingForLocationProgress.setCancelable(true);
		waitingForLocationProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				finish();
			}
		});
		waitingForLocationProgress.show();

		// Show the Up button in the action bar.
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		context = this;
		// provider = new MapLocationProvider(this, false);

		txtDistance = (TextView) findViewById(R.id.TextView_Parking_DistanceValue);
		txtWalkingDistance = (TextView) findViewById(R.id.TextView_Parking_WalkingDistanceValue);
		txtAccuracy = (TextView) findViewById(R.id.TextView_Parking_GPS_Acc);
		txtDateTime = (TextView) findViewById(R.id.TextView_Parking_Timestamp);
		progress = (ProgressBar) findViewById(R.id.progressBar_ParkingWaitingOnGps);
		layoutMaster = (RelativeLayout) findViewById(R.id.layout_master_FindParkingSpot);

		btnToggleCameraLock = findViewById(R.id.btnToggleCameraLock);
		btnToggleMap = findViewById(R.id.btnMapStyle);
		btnClearSpot = findViewById(R.id.btnClearSpot);
		btnSaveSpot = findViewById(R.id.btnSaveSpot);
		btnStreetview = findViewById(R.id.btnStreetviewSpot);
		fabNavigate = findViewById(R.id.fabNavigate);

		fabNavigate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (prefs.getParkingSpot() != null) {
					Helpers.Geo.launchDirections(activity, prefs.getParkingSpot().toPosition());
				} else {
					Toast.makeText(activity, "No parking spot!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		btnClearSpot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				MyYesNoDialog.show(context, getString(R.string.confirm_delete_parking_spot), new MyYesNoDialog.YesNoListener() {
					@Override
					public void onYes() {
						prefs.setParkingSpot(null);
						makeMarker();
						toggleLock(false);
					}

					@Override
					public void onNo() { }
				});
			}
		});

		btnToggleMap.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int curMapType = map.getMapType();
				if (curMapType == 4) {
					map.setMapType(0);
				} else {
					map.setMapType(curMapType + 1);
				}
			}
		});

		btnSaveSpot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (prefs.getParkingSpot() != null) {
					MyYesNoDialog.show(activity, "Are you sure you want to overwrite your old parking spot?", new MyYesNoDialog.YesNoListener() {
						@Override
						public void onYes() {
							saveSpot();
						}

						@Override
						public void onNo() {

						}
					});
				} else {
					saveSpot();
				}
			}
		});

		btnToggleCameraLock.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toggleLock();
				onMyLocationChange(curLocation);
			}
		});

		btnStreetview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getStreetviewAtParkingSpot();
			}
		});

		mapSource = new FollowMeLocationSource(false, context, new FollowMeLocationSource.OnFollowMeLocationChanged() {
			@Override
			public void onLocationChanged(Location location) {
				onMyLocationChange(location);
			}
		});

		/*if (getIntent().getParcelableExtra(SAVED_SPOT_FLAG) != null) {
			savedSpot = getIntent().getParcelableExtra(SAVED_SPOT_FLAG);
		}*/

		((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				map = googleMap;
				if (ActivityCompat.checkSelfPermission(MyApp.getAppContext(), Manifest.permission
						.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(MyApp.getAppContext(),
						Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					Helpers.Permissions.RequestContainer container = new Helpers.Permissions.RequestContainer();
					container.add(Helpers.Permissions.PermissionType.ACCESS_FINE_LOCATION);
					container.add(Helpers.Permissions.PermissionType.ACCESS_BACKGROUND_LOCATION);
					container.add(Helpers.Permissions.PermissionType.ACCESS_COARSE_LOCATION);
					requestPermissions(container.toArray(), 1);
					return;
				}
				map.setMyLocationEnabled(true);
				map.getUiSettings().setCompassEnabled(true);
				map.setBuildingsEnabled(true);
				map.setIndoorEnabled(true);
				map.getUiSettings().setMyLocationButtonEnabled(false);
				map.getUiSettings().setZoomControlsEnabled(false);
				map.setTrafficEnabled(true);
				map.setOnMarkerClickListener(Activity_ParkingMap.this);
				map.setOnMapLongClickListener(new OnMapLongClickListener() {
					@Override
					public void onMapLongClick(LatLng latLng) {
						saveSpot(latLng);
					}
				});
				map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
					@Override
					public void onCameraMove() {
						Log.i(TAG, "onCameraMove ");
						cameraIsMoving = true;
					}
				});

				map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
					@Override
					public void onCameraIdle() {
						Log.i(TAG, "onCameraIdle ");
						cameraIsMoving = false;
					}
				});

				// Have the camera initially hover over the US in toto as opposed to Lat=0 / Lng=0 (which is in the middle of the Atlantic)
				moveCameraToShowUSA(false);

				prefs = new MyPreferencesHelper(Activity_ParkingMap.this);
				makeMarker();

				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (myCurLoc != null) {
							moveCameraToShowMarkers(300);
							handler.removeCallbacks(this);
						} else {
							handler.postDelayed(this, 500);
						}
					}
				}, 1100);

				map.setLocationSource(mapSource);

			}
		});

		// Unlock the camera by default
		toggleLock(false);

		Log.i(TAG, "onCreate ");
	}

	@Override
	protected void onStart() {
		super.onStart();

		// If there isn't a saved spot we can try to get the last entry of the most recent trip
		// and try to get a location from it.
		if (prefs.getParkingSpot() == null) {
			autoGenerateParkingSpotFromLatestTrip();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyApp.setIsVisible(true, this);
		enableDisableButtons();
		drawMappedPolyToSpot();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		// provider.deactivate();
		super.onPause();
		MyApp.setIsVisible(false, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_parking, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Helpers.Strings.applyFontToMenuItem(context, menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate userSwipedDown one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				onBackPressed();
				return true;
			case R.id.action_auto_generate_parking_spot:
				autoGenerateParkingSpotFromLatestTrip();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void autoGenerateParkingSpotFromLatestTrip() {
		MySqlDatasource ds = new MySqlDatasource();
		FullTrip latestTrip = ds.getLatestNonManualTrip();
		if (latestTrip != null) {
			ArrayList<TripEntry> entries = latestTrip.getTripEntries();
			if (entries != null && entries.size() > 0) {
				TripEntry lastEntry = entries.get(entries.size() - 1);
				SavedParkingSpot autoGeneratedParkingSpot = new SavedParkingSpot(lastEntry.makeLocation());
				autoGeneratedParkingSpot.dateTimeIsMs = lastEntry.getMilis();
				prefs.setParkingSpot(autoGeneratedParkingSpot);
				Log.i(TAG, "onResume | Auto-generated a parking spot from the last trip.");
				Toast.makeText(activity, "Auto-generated a parking spot from your last trip!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(activity, "Latest trip could not be used.", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(activity, "Could not find your latest (non-manual) trip.  Sorry!", Toast.LENGTH_SHORT).show();
		}
	}

	public void updateCamera(float bearing) {
		CameraPosition currentPlace = new CameraPosition.Builder()
				.target(new LatLng(0, 0))
				.bearing(bearing).tilt(65.5f).zoom(18f).build();
		map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

	}

	/**
	 * Creates a saved parking spot using the user's current location (if known).
	 */
	void saveSpot() {
		if (curLocation != null) {
			SavedParkingSpot newSpot = new SavedParkingSpot(curLocation);
			newSpot.save();
			makeMarker();
			moveCameraToShowMarkers(1400);
			drawMappedPolyToSpot();
			Toast.makeText(activity, "Parking spot saved!", Toast.LENGTH_SHORT).show();
		}
		enableDisableButtons();
	}

	/**
	 * Creates a saved parking spot.
	 * @param location The location to use as the parking spot.
	 */
	void saveSpot(Location location) {
		if (curLocation != null) {
			SavedParkingSpot newSpot = new SavedParkingSpot(location);
			newSpot.save();
			makeMarker();
			moveCameraToShowMarkers(1400);
			drawMappedPolyToSpot();
		}
		enableDisableButtons();
	}

	/**
	 * Creates a saved parking spot.
	 * @param position The position to use as the parking spot.
	 */
	void saveSpot(LatLng position) {
		if (curLocation != null) {
			SavedParkingSpot newSpot = new SavedParkingSpot(position);
			newSpot.save();
			makeMarker();
			moveCameraToShowMarkers(1400);
			drawMappedPolyToSpot();
		}
		enableDisableButtons();
	}

/*	public void updateLastKnownLocation() {
		Location lastKnownLoc = MyLocationService.getLastKnownCachedLocationFromOS(this);
		if (lastKnownLoc == null) {
			Log.w(TAG, "updateLastKnownLocation: | NOT KNOWN!");
			MyLocationService.getLastKnownCachedLocationFromOS(this, new OnSuccessListener<Location>() {
				@Override
				public void onSuccess(Location location) {
					curLocation = location;
					Log.i(TAG, "onSuccess | Current position retrieved from FusedLocationProvider!");
				}
			});
		} else {
			curLocation = lastKnownLoc;
			Log.i(TAG, "updateLastKnownLocation | Found last known loc from the OS!");
			int mins = Helpers.DatesAndTimes.convertMilisToMinutes(System.currentTimeMillis() - lastKnownLoc.getTime());
			Log.i(TAG, "updateLastKnownLocation | Cached location is " + mins + " old.");

		}
	}*/

	// Capture key presses
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// If the button was the 'Back' button then we do stuff
		if (keyCode == KeyEvent.KEYCODE_BACK) { // Key pressed was the 'Back' button
			try {
				// Check if the camera locked and if so, simply unlock it.
				if (isLocked) {
					toggleLock(false);
					return false;
				} else {
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

	public void onMyLocationChange(Location loc) {

		// layoutMapView.setVisibility(View.VISIBLE);
		waitingForLocationProgress.dismiss();

		enableDisableButtons();

		// Calculate the accuracy.
		curLocation = loc;
		myCurLoc = loc;
		int actualValue = (int) loc.getAccuracy();
		float pct = (getCurrentAccAsPct(loc.getAccuracy()));
		int val = (int) pct;
		txtAccuracy.setText(val + "% ( \u2245 " + actualValue + " meters )");

		if (prefs.getParkingSpot() != null) {
			// Get the distance to the parking spot evry time we move
			double d = loc.distanceTo(prefs.getParkingSpot().toLocation());
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
			// long strMilis = savedSpot.getTime();
			// DateTime timeStamp = new DateTime(strMilis);
			String str;
			String pattern = "E, MMM dd, yyyy hh:mm a";
			DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
			str = fmt.print(prefs.getParkingSpot().getDate());
			txtDateTime.setText(str);

			// Clear the map, always
			map.clear();
			// make the parkMarker, always
			makeMarker();

			// Draw straight polyline, always
			drawPolyToParking(loc, prefs.getParkingSpot().toLocation());

			/***************************************************************************************************
			 Check if we should renew the calculated polyline.  If we don't throttle it somewhat it
			 creates what could be almost described as 'flickering' while the existing line is removed
			 and the replacement line is drawn.  Not to mention unnecessary Google API calls.
			 ****************************************************************************************************/
			// We're going to evaluate this value when determining whether or not to draw the calculated poly.  If 
			// it's null we set it to the current location as that means we haven't drawn it even once yet. 
			if (locationAtLastRouteCalc == null) {
				locationAtLastRouteCalc = loc;
			}

			// Get the distance travelled since the last time the poly's were drawn
			distanceSinceLastRouteCalc = locationAtLastRouteCalc.distanceTo(curLocation);
			// If that distance exceeds an arbitrary distance we clear and redraw them (the marker as well)
			if (calculatedLine == null || distanceSinceLastRouteCalc >= MIN_DISTANCE_BETWEEN_ROUTE_CALCS || firstRouteCalc) {

				// Check if a route is currently being calculated so we don't bombard the API server with requests.
				if (isCalculatingRoute) {

					// We still want to update the camera before we exit the function.
					if (isLocked) {
						moveFollowCamera(loc);
					} else {
						moveCameraToShowMarkers(1200);
					}

					// Exit
					return;
				}

				try {
					// Update our last loc so we can try to prevent excessive poly redraws
					locationAtLastRouteCalc = loc;
					// Check if the absolute distance to the parking spot is less than 175 meters.  If it is then 
					// there's no functional need for the calculated poly (the car should be in sight) so we don't draw it. 
					// We also populate the calculated line's label value to reflect this fact.
					double dblFt = (double) loc.distanceTo(prefs.getParkingSpot().toLocation());
					// We want to strip the decimal places off the result otherwise it looks wierd when we're saying, "Probably about: "
					String ft = Helpers.Geo.convertMetersToFeet(Double.valueOf(dblFt), this, false);
					int finalVal = Double.valueOf(ft).intValue();
					// If we're within 175 meters of our parking spot we stop drawing the calculated poly assuming that the car is probably 
					// in sight or close enough that calculated directions aren't going to be useful any longer.  
					if (dblFt >= 175) { // Distance is still farther than 175 - draw the line
						drawMappedPolyToSpot(new LatLng(loc.getLatitude(), loc.getLongitude()),
								prefs.getParkingSpot().toPosition());
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

		if (isLocked) {
			moveFollowCamera(loc);
		} else {
			moveCameraToShowMarkers(1200);
		}

	}

	private void enableDisableButtons() {
		btnSaveSpot.setEnabled(curLocation != null);
		btnClearSpot.setEnabled(prefs.getParkingSpot() != null);
		btnStreetview.setEnabled(prefs.getParkingSpot() != null);
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void toggleLock() {
		isLocked = !isLocked;

		if (isLocked) {
			btnToggleCameraLock.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_padlock_locked, 0, 0, 0);
			moveFollowCamera(curLocation);
		} else {
			btnToggleCameraLock.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_padlock_unlocked, 0, 0, 0);
		}

	}

	public void toggleLock(boolean lockCamera) {
		isLocked = lockCamera;

		if (isLocked) {
			moveFollowCamera(curLocation);
			btnToggleCameraLock.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_padlock_locked, 0, 0, 0);
		} else {
			moveCameraToShowMarkers(1400);
			btnToggleCameraLock.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_padlock_unlocked, 0, 0, 0);
		}

	}

	public void toggleMap(View v) {

	}

	public void getStreetviewAtParkingSpot() {
		if (prefs.getParkingSpot() != null) {
			Intent intent = new Intent(context, StreetViewActivity.class);
			intent.putExtra(StreetViewActivity.POSITION_INTENT_TAG, prefs.getParkingSpot().toPosition());
			startActivity(intent);
		}
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

	/*public boolean isCameraOnMyLocationRightNow() {
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

		return deviation > 0;
	}*/

	void centerCameraOnMe(Location location) {
		CameraPosition currentPlace = new CameraPosition.Builder()
				.target(new LatLng(location.getLatitude(), location.getLongitude()))
				.bearing(currentBearing).tilt(90f).zoom(12).build();
		map.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 1000, new GoogleMap.CancelableCallback() {
			@Override
			public void onFinish() {
				Log.i(TAG, "onFinish ");
			}

			@Override
			public void onCancel() {
				Log.i(TAG, "onCancel ");
			}
		});
		userInitiatedCameraMove = false;
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
	private void moveFollowCamera(Location location) {

		float rawSpeed = location.getSpeed();
		double lat, lng;
		float bearing = location.getBearing();
		lat = location.getLatitude();
		lng = location.getLongitude();
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
		map.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace), 1000, new GoogleMap.CancelableCallback() {
			@Override
			public void onFinish() {
				Log.i(TAG, "onFinish ");
			}

			@Override
			public void onCancel() {
				Log.i(TAG, "onCancel ");
			}
		});
		userInitiatedCameraMove = false;
	}

	private float calculateZoomAccordingToSpeed(float speedTravelling) {

		float setZoom = currentZoom;
		Log.d(TAG, "Speed supplied was: " + speedTravelling);

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
		if (a > 100f) {
			a = 100f;
		}
		float d = a / 100f; // should rslt in a decimal between 0 and 1.  Higher is worse.
		float pct = 1f - d;
		float rslt = pct * 100;
		int intRslt = (int) rslt;
		return intRslt;
	}

	public void makeMarker() {

		if (map != null) {
			map.clear();
		}

		if (prefs.getParkingSpot() == null) {
			return;
		}

		LatLng spotLatLng = prefs.getParkingSpot().toPosition();
		// Build a map marker
		Marker parkingSpot = map.addMarker(new MarkerOptions().position(spotLatLng));
		parkingSpot.setTitle("You're parked here!");
		parkingSpot.showInfoWindow();

		// return spotLatLng;
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
		new ContactGoogle().execute(src, dest, null);
	}

	/** Starts the async task that contacts Google and attempts to draw a polyline indicating the driving path one would
	 *  have to take to arrive at the parking spot's location. **/
	public void drawMappedPolyToSpot() {

		if (curLocation == null || prefs.getParkingSpot() == null) {
			// moveCameraToShowUSA(true);
			return;
		}

		// Getting URL to the Google Directions API
		new ContactGoogle().execute(new LatLng(curLocation.getLatitude(), curLocation.getLongitude())
				, prefs.getParkingSpot().toPosition(), null);
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
		Location startingLoc = Helpers.Geo.createLocFromLatLng(new LatLng(lat, lng));
		CameraPosition overUSA = new CameraPosition.Builder()
				.target(new LatLng(lat, lng))
				.bearing(startingLoc.getBearing()).tilt(90f).zoom(3).build();
		if (animate) {
			map.animateCamera(CameraUpdateFactory.newCameraPosition(overUSA));
		} else {
			map.moveCamera(CameraUpdateFactory.newCameraPosition(overUSA));
		}
		userInitiatedCameraMove = false;
	}

	/** moves the camera to a position such that both the start and end map markers are viewable on screen. 
	 *  cameraAnimationDuration represents how long the animation should take (effectively the speed) in ms **/
	private void moveCameraToShowMarkers(int cameraAnimationDuration) {
		// Create a new LatLngBounds.Builder object
		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		// While the camera is in motion we trip this flag.  It will be turned back off when the camera arrives on position.
		cameraIsDefaulting = true;

		// Check that the two markers representing the start and end points are present
		if (myCurLoc != null) {
			// Loop through the array (array's a strong term though accurate as there's only two items (start and end points))
			// Add the current marker's position to the builder object
			if (prefs.getParkingSpot() != null) {
				builder.include(prefs.getParkingSpot().toPosition());
			}

			builder.include(new LatLng(myCurLoc.getLatitude(), myCurLoc.getLongitude()));

			// Create a populated LatLngBounds object by calling the builder object's build() method
			LatLngBounds markerBounds = builder.build();

			// Create a CameraUpdate object using the new markerBounds object (and a bit of padding) 
			// that we can feed to the map's resetCameraToCurrentAndEnableTripClipping method to actually and finally move the
			// camera to a position such that both start and end markers are visible. 
			CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(markerBounds, MAP_PADDING);

			try {
				// Finally we actually get to move the damn camera
				map.animateCamera(cu, cameraAnimationDuration, new GoogleMap.CancelableCallback() {
					@Override
					public void onFinish() {
						cameraIsMoving = false;
					}

					@Override
					public void onCancel() {
						cameraIsMoving = false;
					}
				});
				userInitiatedCameraMove = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


	}

	/** Async class that contacts Google and using the returned data draws a polyline indicating the driving/waling path
	 *  one would have to take to arrive at the saved parking spot **/
	public class ContactGoogle extends AsyncTask<LatLng, LatLng, Boolean> {
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
			isCalculatingRoute = true;
		}

		@Override
		protected Boolean doInBackground(LatLng... params) {
			try {
				// Leave if a null position is supplied.
				if (params[0] == null || params[1] == null) {
					return false;
				}

				LatLng startPos = params[0];
				LatLng endPos = params[1];

				// Don't waste API calls for trivial distances.
				if (Helpers.Geo.getDistanceBetweenInMeters(startPos, endPos) < 500) {
					return false;
				}

				doc = gmap.getDocument(startPos, endPos, MyMapRouteHelper.MODE_WALKING);
				latLngList = gmap.getDirection(doc);
				return true;
			} catch (Exception e) {
				operationFailed = true;
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {

			if (!success) {
				// txtWalkingDistance.setText(txtDistance.getText());
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
			float walkingDistance = Helpers.Geo.convertMetersToMiles(totalDistance, 2);
			txtWalkingDistance.setText(walkingDistance + " miles");
			isCalculatingRoute = false;
		}

		@Override
		protected void onCancelled(Boolean aBoolean) {
			isCalculatingRoute = false;
			super.onCancelled(aBoolean);
		}

		@Override
		protected void onCancelled() {
			isCalculatingRoute = false;
			super.onCancelled();
		}
	}
}
