package com.release.eancis.trackingonmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap mMap;
    private View mLayout;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LatLng mLastKnownLatLng;

    private static final int LOCATION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //it's entry point for interacting with the fused location provider
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Used for receiving notifications from the FusedLocationProviderClient when the device location
        // has changed or can no longer be determined
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateUI(location);
                }
            }
        };
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getCurrenLocationOnMap();
    }

    /**
     * Display the in the content the map with related placeholder if the required Camera
     * permission has been granted.
     */
    private void getCurrenLocationOnMap(){
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
                                updateUI(location);
                            }
                        }
                    });

            mMap.setMyLocationEnabled(true);

        }
    }

    /**
     * Requests the Location permission.
     * It's mandatory to handle permission requirements in runtime from Android 6 (Marshmallow).
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
     * It gets the user answer about permissione requirements from the Snackbar
     * **/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, getResources().getString(R.string.locatin_granted));

                    Snackbar.make( mLayout, R.string.locatin_granted,
                            Snackbar.LENGTH_SHORT).show();
                    getCurrenLocationOnMap();
                } else

                    Log.d(TAG, getResources().getString(R.string.locatin_not_granted));

                    Snackbar.make(mLayout, R.string.locatin_not_granted,
                            Snackbar.LENGTH_SHORT).show();
                break;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //To start the coordinates updating when the activity turns in foreground
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //To stop the coordinates updating when the activity goes in background
        stopLocationUpdates();
    }

    protected void startLocationUpdates() {
        //LocationRequest objects are used to request a quality of service for location updates from the FusedLocationProviderClient.
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(getResources().getInteger(R.integer.map_interval_update_rate));
        locationRequest.setFastestInterval(getResources().getInteger(R.integer.map_fastest_interval_update_rate));
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //New permission requirement it's needed before requestLocationUpdates to be sure to have permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Location permission has not been granted
            requestLocationPermission();
        }
        else {
            //Location permissions is already available, it's possible to retrieve updates
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback,null);
        }
    }

    protected void stopLocationUpdates() {
        //Stop updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * It update map anc camera with the new coordinates
     * */
    private void updateUI(Location location){
            mLastKnownLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mLastKnownLatLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLastKnownLatLng, getResources().getInteger(R.integer.default_zoom_value)));
    }
}
