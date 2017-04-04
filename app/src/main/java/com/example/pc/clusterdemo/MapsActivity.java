package com.example.pc.clusterdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,OnMapReadyCallback {

    private GoogleMap mMap;
    private ClusterManager<MyItem> mClusterManager;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    private static int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private LocationManager manager;
    private LatLng current,target;
    Marker CurrentMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (checkPlayServices()) {

            buildGoogleApiClient();
            createLocationRequest();
        }
        if (isLocationPermissionGranted()) {
        } else {
            requestLocationPermission();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }
    public  class MyItem implements ClusterItem {
        LatLng mPosition;
        String mTitle;
        String mSnippet;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        public MyItem(double lat, double lng, String title, String snippet) {
            mPosition = new LatLng(lat, lng);
            mTitle = title;
            mSnippet = snippet;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public String getTitle() {
            return mTitle;
        }

        @Override
        public String getSnippet() {
            return mSnippet;
        }
        public void setUpClusterer() {
            // Position the map.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current.latitude,current.longitude),5));

            // Initialize the manager with the context and the map.
            // (Activity extends context, so we can pass 'this' in the constructor.)
            mClusterManager = new ClusterManager<MyItem>(MapsActivity.this,mMap);

            // Point the map's listeners at the listeners implemented by the cluster
            // manager.
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);

            // Add cluster items (markers) to the cluster manager.
            addItems();
        }
        private void addItems() {

            // Set some lat/lng coordinates to start with.
            double lat = current.latitude;
            double lng = current.longitude;

            // Add ten cluster items in close proximity, for purposes of this example.
            for (int i = 0; i < 10; i++) {
                double offset = i / 60d;
                lat = lat + offset;
                lng = lng + offset;
                MyItem offsetItem = new MyItem(lat, lng);
                mClusterManager.addItem(offsetItem);
            }
        }

    }
    public boolean isLocationPermissionGranted() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    public void requestLocationPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
        }
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted now you can Access Current Location", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }



    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation=location;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latlng).draggable(true));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        }
        startLocationUpdates();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        currentLocation();
        MyItem m=new MyItem(current.latitude,current.longitude);
        m.setUpClusterer();
    }

    @Override
    public void onConnectionSuspended(int i) {
        LatLng center = mMap.getCameraPosition().target;
        Marker m= mMap.addMarker(new MarkerOptions().position(center)
                .title("center"));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
    public void currentLocation()
    {
        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Toast.makeText(MapsActivity.this,"GPS should be enabled",Toast.LENGTH_SHORT).show();
            String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equals("")) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }
        else {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                if(CurrentMarker!=null)
                {
                    CurrentMarker.remove();
                }
                LatLng curPosition = new LatLng(latitude, longitude);
                CurrentMarker= mMap.addMarker(new MarkerOptions().position(curPosition)
                        .title("Current Position"));
                current=curPosition;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 4.0f));
            }
        }
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

}
