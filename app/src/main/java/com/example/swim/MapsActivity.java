package com.example.swim;


import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.Manifest.permission;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.api.Status;
import com.example.swim.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.utils.DataSingleton;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker destinationMarker;
    private LatLng clickedLatLng;
    private LatLng destLatLng;
    private LatLng currentLatLng;
    private LatLng prevLatLng;
    private Button setDestinationButton; // Declare the Button variable
    private static final String API_KEY = BuildConfig.API_KEY;
    private AutocompleteSupportFragment autocompleteFragment;
    private LocationCallback locationCallback;
    private static final String KEY_CLICKED_LATLNG = "clicked_latlng";
    private static final String PREF_NAME = "MyPrefs";




    /**
     * Flag indicating whether a requested permission has been denied after returning in {@link
     * #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clickedLatLng = loadClickedLatLng();

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent serviceIntent = new Intent(this, ServerActivity.class);
        startService(serviceIntent);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        setDestinationButton = findViewById(R.id.setDestinationButton); // Initialize the Button
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                if (currentLatLng == null){
                    return;
                }
                Location currentLocation = convertLatLngToLocation(currentLatLng);
                for (Location location : locationResult.getLocations()) {
                    // Handle the new location
                    if (location.distanceTo(currentLocation) > 0.25f) {
                        prevLatLng = currentLatLng;
                        LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
                        currentLatLng = currentLoc;
                        currentLocation = convertLatLngToLocation(currentLatLng);
                        if (destLatLng != null) {
                            if (prevLatLng != null){
                                Location prevLocation = convertLatLngToLocation(prevLatLng);
                                int vectorBearing = (int)(prevLocation.bearingTo(currentLocation));
                                String message = "Bearing to destination: " + vectorBearing;
                                //Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
                                DataSingleton.getInstance().setSharedData("destBearing", vectorBearing);
                                Location destLocation = convertLatLngToLocation(destLatLng);
                                //String message = "Distance to destination: " + currentLocation.distanceTo(destLocation);
                                //Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
                                // Update the map or do other tasks with the current location
                                //DataSingleton messenger = DataSingleton.getInstance();
                                DataSingleton.getInstance().setSharedData("distance", (int)(currentLocation.distanceTo(destLocation)));
                                int distance = (int)(currentLocation.distanceTo(destLocation));
                                int bearingToDest = (int)(currentLocation.bearingTo(destLocation));
                                sendDataToClient(String.valueOf(distance),String.valueOf(vectorBearing),String.valueOf(bearingToDest));
                            }

                        }
                    }
                }
            }
        };

        Places.initialize(getApplicationContext(), API_KEY);

        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Toast.makeText(this, "Permission Denied:\n", Toast.LENGTH_LONG).show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Restore the clickedLatLng if it was previously saved

        if (clickedLatLng != null) {
            // Restore the marker
            destinationMarker = googleMap.addMarker(new MarkerOptions().position(clickedLatLng).title("Tapped Location"));
            setDestinationButton.setVisibility(View.VISIBLE);
        }

        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        googleMap.setMyLocationEnabled(!permissionDenied);
        //Toast.makeText(this, "Permission Denied:\n" + permissionDenied, Toast.LENGTH_LONG).show();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                        //Toast.makeText(this, "Permission:\n" , Toast.LENGTH_LONG).show();
                        currentLatLng = currentLocation;
                    }
                });

        fusedLocationClient.requestLocationUpdates(createLocationRequest(), locationCallback, null);
        setDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if a LatLng has been clicked and stored
                if (clickedLatLng != null) {
                    // Use the clickedLatLng for your desired action, e.g., set it as the destination
                    // For demonstration, we'll display a toast message with the coordinates.
                    destLatLng = clickedLatLng;
                    Location destLocation = convertLatLngToLocation(destLatLng);
                    Location currentLocation = convertLatLngToLocation(currentLatLng);
                    double latitude = destLatLng.latitude;
                    double longitude = destLatLng.longitude;
                    String message = "Distance to destination: " + currentLocation.distanceTo(destLocation);
                    //Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destLatLng, 15f));

                }
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (destinationMarker != null) {
                    // Remove the previous destination marker
                    destinationMarker.remove();
                }

                // Add a marker at the tapped location
                destinationMarker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Tapped Location"));
                clickedLatLng = latLng;
                setDestinationButton.setVisibility(View.VISIBLE);
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng selectedLatLng = place.getLatLng();
                // Handle the selected place, e.g., add a marker to the map
                if (destinationMarker != null) {
                    // Remove the previous destination marker
                    destinationMarker.remove();
                }
                if (selectedLatLng != null) {
                    destinationMarker = googleMap.addMarker(new MarkerOptions()
                            .position(selectedLatLng)
                            .title(place.getName()));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
                    clickedLatLng = selectedLatLng;
                    setDestinationButton.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onError(Status status) {
                // Handle errors, if any
            }
        });



    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        fusedLocationClient.removeLocationUpdates(locationCallback);
//    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call the superclass method
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionDenied = false;
            }
        } else {
            permissionDenied = true;
        }
    }

    private LocationRequest createLocationRequest() {
        return LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public Location convertLatLngToLocation(LatLng latLng) {
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    private void saveClickedLatLng(LatLng latLng) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_CLICKED_LATLNG + "_LAT", (float) latLng.latitude);
        editor.putFloat(KEY_CLICKED_LATLNG + "_LNG", (float) latLng.longitude);
        editor.commit();
    }

    private LatLng loadClickedLatLng() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        float lat = prefs.getFloat(KEY_CLICKED_LATLNG + "_LAT", 0f);
        float lng = prefs.getFloat(KEY_CLICKED_LATLNG + "_LNG", 0f);
        // Check if the saved values are not the default (zero) values
        if (lat != 0f && lng != 0f) {
            return new LatLng(lat, lng);
        } else {
            // Return null if there is no valid saved LatLng
            return null;
        }
    }

    private void sendDataToClient(String distance, String bearing, String absoluteBearing) {
        // Get a reference to the MyServerService
        ServerActivity serverService = ServerActivity.getInstance();
        if (serverService != null && serverService.getClient() != null) {
            serverService.sendLocationData(distance, bearing,absoluteBearing);
        }
    }

    @Override
    protected void onPause() {
        Log.d("pause","pausee");
        super.onPause();

        // Save the clickedLatLng when the activity is paused
        if (clickedLatLng != null) {
            saveClickedLatLng(clickedLatLng);
        }
    }
    @Override
    protected void onDestroy() {
        Log.d("destroy","destory");
        super.onDestroy();
        Intent serviceIntent = new Intent(this, ServerActivity.class);
        stopService(serviceIntent);
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

}