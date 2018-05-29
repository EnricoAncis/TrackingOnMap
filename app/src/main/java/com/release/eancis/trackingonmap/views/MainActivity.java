package com.release.eancis.trackingonmap.views;

/**
 * Created by Enrico Ancis on 25/05/18.
 */

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.release.eancis.trackingonmap.R;
import com.release.eancis.trackingonmap.controllers.DataController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap mMap;
    private View mLayout;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean isChecked;
    private LocationCallback mLocationCallback;
    private List<Polyline> mGpsTracksList;
    private Map<Integer, List<LatLng>> mTracksDictionary;
    private long mlastTrackId;
    private DataController mDataController;
    private  float mZoomLevel;

    private static final int LOCATION_REQUEST_CODE = 101;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
    public static final String SHARE_PREFS_NAME = "TrackingStatus";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateUI(location, mMap.getCameraPosition().zoom);
                }
            }
        };

        mGpsTracksList = new ArrayList<Polyline>();
        mTracksDictionary =  new HashMap<Integer, List<LatLng>>();
        mDataController = new DataController(MainActivity.this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSettings = mMap.getUiSettings();
        //It adds Zoom buttons on the map
        uiSettings.setZoomControlsEnabled(true);
        //Ii enables compass icon when the map needs orientation.
        uiSettings.setCompassEnabled(true);

        checkCurrentState();

        getCurrenLocationOnMap(mZoomLevel);
    }

    /**
     * It displays in the content the map with related placeholder if the required Camera
     * permission has been granted.
     */
    private void getCurrenLocationOnMap(final float zoomLevel){

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Location permission has not been granted
            requestLocationPermission();
        } else {
            //Location permissions is already available, show the map.

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                updateUI(location, zoomLevel);
                            }
                        }
                    });

            mMap.setMyLocationEnabled(true);

        }
    }

    /**
     * Requests the Location permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Locaiton permission request.

            Log.d(TAG, getResources().getString(R.string.permission_location_rationale));

            Snackbar.make(mLayout, R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok_btn, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    LOCATION_REQUEST_CODE);
                        }
                    })
                    .show();
        }
        // Locaiton permission has not been granted yet. Request it directly.
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_REQUEST_CODE);
    }

    /**
     * It sets the last app state
     */
    private void checkCurrentState(){

        if( mTracksDictionary.size() != 0) {

            if(isChecked){
                startLocationUpdates();
            }

            for (Map.Entry<Integer, List<LatLng>> entry : mTracksDictionary.entrySet()) {
                Integer color = entry.getKey();
                List<LatLng> coordsList = entry.getValue();

                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(color);
                polylineOptions.width(getResources().getInteger(R.integer.track_width));
                Polyline gpsTrack = mMap.addPolyline(polylineOptions);
                mGpsTracksList.add(gpsTrack);

                int lastTrackIndex = mGpsTracksList.size();
                List<LatLng> points = mGpsTracksList.get(lastTrackIndex - 1).getPoints();
                points.addAll(coordsList);
                mGpsTracksList.get(lastTrackIndex - 1).setPoints(points);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, getResources().getString(R.string.locatin_granted));

                    Snackbar.make( mLayout, R.string.locatin_granted,
                            Snackbar.LENGTH_SHORT).show();
                    getCurrenLocationOnMap(getResources().getInteger(R.integer.default_zoom_value));
                } else

                    Log.d(TAG, getResources().getString(R.string.locatin_not_granted));

                    Snackbar.make(mLayout, R.string.locatin_not_granted,
                            Snackbar.LENGTH_SHORT).show();
                break;

        }

    }

    /**
     * It does the update of the location on the map
     * */
    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(getResources().getInteger(R.integer.map_interval_update_rate));
        locationRequest.setFastestInterval(getResources().getInteger(R.integer.map_fastest_interval_update_rate));
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Location permission has not been granted
            requestLocationPermission();
        }
        else {
            //Location permissions is already available, show the map.
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback,null);
        }
    }

    /**
     * It stops the update of the location on the map
     * */
    protected void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Refresh the map with the new location retrieved from LocationCallback by GPS
     *
     * @param location  value of the location retrieved from LocationCallback
     * @param zoomLevel current zoom level of the map
     *
     */
    private void updateUI(Location location, float zoomLevel){
        if(isChecked) {
            LatLng lastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLatLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, zoomLevel));
            updateTrack(location);
        }
    }

    /**
     * Draw the path of the current recording track
     *
     * @param location  value of the location retrieved from LocationCallback
     *
     */
    private void updateTrack(Location location) {

        LatLng point  = new LatLng(location.getLatitude(), location.getLongitude());
        int lastTrackIndex = mGpsTracksList.size();
        List<LatLng> points = mGpsTracksList.get(lastTrackIndex - 1).getPoints();
        points.add(point);
        mGpsTracksList.get(lastTrackIndex - 1).setPoints(points);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.map_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        MenuItem item = (MenuItem) menu.findItem(R.id.action_recoder);

        item.setActionView(R.layout.recorder_switch);
        Switch recorderSwitch = (Switch) item.getActionView().findViewById(R.id.recorder_switch);
        recorderSwitch.setChecked(isChecked);

        recorderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean switchIsChecked) {

                trackRecorder(switchIsChecked);
            }
        });

        return true;
    }

    /**
     * Set the track recording ON/OFF
     *
     * @param isSwitchChecked  value of the switch button
     *
     */
    private void trackRecorder(boolean isSwitchChecked) {

        isChecked = isSwitchChecked;

        if (isSwitchChecked) {
            startLocationUpdates();

            Random rnd = new Random();
            int color = Color.argb(getResources().getInteger(R.integer.color_alpha),
                                    rnd.nextInt(getResources().getInteger(R.integer.color_component)),
                                    rnd.nextInt(getResources().getInteger(R.integer.color_component)),
                                    rnd.nextInt(getResources().getInteger(R.integer.color_component)));

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(color);
            polylineOptions.width(getResources().getInteger(R.integer.track_width));
            Polyline gpsTrack = mMap.addPolyline(polylineOptions);
            mGpsTracksList.add(gpsTrack);

            String startTime = dateFormat.format(new Date());

            mlastTrackId = mDataController.createTrack(startTime, null, null, color);
        } else {
            stopLocationUpdates();

            String endTime = dateFormat.format(new Date());
            int lastTrackIndex = mGpsTracksList.size();

            mDataController.closeTrack(mlastTrackId, endTime, mGpsTracksList.get(lastTrackIndex - 1).getPoints());
        }

    }

    /**
     * Here it's managed the status of the activity across configuration changes like
     * a diplay orientation change
     * */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(SHARE_PREFS_NAME, MODE_PRIVATE);
        isChecked = prefs.getBoolean(getResources().getString(R.string.share_preferences_is_checked), false);
        mlastTrackId = prefs.getLong(getResources().getString(R.string.share_preferences_last_track_id), -1);
        mZoomLevel =  prefs.getFloat(getResources().getString(R.string.share_preferences_zoom_level), getResources().getInteger(R.integer.default_zoom_value));

        if(mTracksDictionary.size() == 0){
            //If the list is empty, bucause no track are still stored (first time app launching),
            //because the activity has been destroyed or
            //because the app has been relaunched
            //it has to be reloaded the tracks form the db
            mTracksDictionary = mDataController.getAllTracksList(MainActivity.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        int lastTrackIndex = mGpsTracksList.size();
        if(isChecked) {
            //it stores on db the currente state of the current track
            mDataController.closeTrack(mlastTrackId, null, mGpsTracksList.get(lastTrackIndex - 1).getPoints());
        }
        SharedPreferences.Editor editor = getSharedPreferences(SHARE_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(getResources().getString(R.string.share_preferences_is_checked), isChecked);
        editor.putLong(getResources().getString(R.string.share_preferences_last_track_id), mlastTrackId);
        editor.putFloat(getResources().getString(R.string.share_preferences_zoom_level), mMap.getCameraPosition().zoom);

        editor.apply();

    }


}
