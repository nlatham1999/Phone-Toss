package com.nicklatham.phonecatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private Button mLocationButton;
    private TextView mLocationTextView;
    private TextView mOverallDistanceTextView;

    private Location mLastLocation;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationManager locationManager;

    private Location mFirstLocation;
    private Location mSecondLocation;

    private boolean startOfThrow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationButton = (Button) findViewById(R.id.getLocationButton);
        mLocationTextView = (TextView) findViewById(R.id.currentLocation);
        mOverallDistanceTextView = (TextView) findViewById(R.id.displayDistanceTextView);

        setUpThrowButtonCallback();

        //can either you the location manager method of getting the location of the fused location
        //  API.
        // Fused location API has better time but the location manager has better accruacy
        setUpFusedLocationAPI();
//        setUpLocationManager();
    }

    private void setUpThrowButtonCallback(){
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startOfThrow) {
                    mLocationButton.setText("End Throw");
                } else {
                    mLocationButton.setText("Throw Phone!");
                }
                startOfThrow = !startOfThrow;
            }
        });
    }

    private void setUpFusedLocationAPI(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                getLocation();
            }
        };

        mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null /* Looper */);
    }

    private void setUpLocationManager(){
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        //Use FINE or COARSE (or NO_REQUIREMENT) here
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        criteria.setBearingRequired(true);

        //API level 9 and up
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
//        criteria.setAccuracy(Criteria.ACCURACY_HIGH);
        locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                displayLocationInformation(location);
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
        });
    }

    private double getDistanceBetweenTwoPoints(){

        if(mFirstLocation == null || mSecondLocation == null){
            return -1;
        }

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(mFirstLocation.getLatitude() - mSecondLocation.getLatitude());
        double lonDistance = Math.toRadians(mFirstLocation.getLongitude() - mSecondLocation.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(mFirstLocation.getLatitude())) * Math.cos(Math.toRadians(mSecondLocation.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = mFirstLocation.getAltitude() - mSecondLocation.getAltitude();

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);

    }

    private void displayDistance(){
        double distance = getDistanceBetweenTwoPoints();
        distance = distance / .305;
        mOverallDistanceTextView.setText(String.valueOf(distance));
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        displayLocationInformation(location);
                    }
                });
        }
    }

    private void displayLocationInformation(Location location){
        if (location != null) {
            mLastLocation = location;
            mLocationTextView.setText(//String.valueOf(mLastLocation.getLatitude())+ " " + String.valueOf(mLastLocation.getLongitude()) + " " + mLastLocation.getAccuracy());
                    getString(R.string.location_text,
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude(),
                            mLastLocation.getAltitude(),
                            mLastLocation.getAccuracy(),
                            mLastLocation.getTime()));
            if(startOfThrow){
                mFirstLocation = mLastLocation;
            }else{
                mSecondLocation = mLastLocation;
                displayDistance();
            }
        } else {
            mLocationTextView.setText("no location found");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this,
                            "permision denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

}
