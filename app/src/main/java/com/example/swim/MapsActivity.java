package com.example.swim;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import android.Manifest.permission;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.view.View;

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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker destinationMarker;
    private LatLng clickedLatLng;
    private Button setDestinationButton; // Declare the Button variable
    private static final String API_KEY = BuildConfig.API_KEY;
    private AutocompleteSupportFragment autocompleteFragment;


    /**
     * Flag indicating whether a requested permission has been denied after returning in {@link
     * #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        setDestinationButton = findViewById(R.id.setDestinationButton); // Initialize the Button
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if a LatLng has been clicked and stored
                if (clickedLatLng != null) {
                    // Use the clickedLatLng for your desired action, e.g., set it as the destination
                    // For demonstration, we'll display a toast message with the coordinates.
                    double latitude = clickedLatLng.latitude;
                    double longitude = clickedLatLng.longitude;
                    String message = "Destination set to Latitude: " + latitude + ", Longitude: " + longitude;
                    Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Places.initialize(getApplicationContext(), API_KEY);

        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        Toast.makeText(this, "Permission Denied:\n", Toast.LENGTH_LONG).show();
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
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        googleMap.setMyLocationEnabled(!permissionDenied);
        Toast.makeText(this, "Permission Denied:\n" + permissionDenied, Toast.LENGTH_LONG).show();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                        Toast.makeText(this, "Permission:\n" , Toast.LENGTH_LONG).show();
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
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
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

}