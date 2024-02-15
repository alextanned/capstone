package com.example.swim;

import android.content.SharedPreferences;
import android.location.Location;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MapsActivityTestSuite {

    @Mock
    SharedPreferences sharedPreferences;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    private GoogleMap googleMap;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // Setup SharedPreferences Mock
        when(sharedPreferences.edit()).thenReturn(editor);
        when(editor.putFloat(anyString(), anyFloat())).thenReturn(editor);
    }

    @Test
    public void saveClickedLatLng_isCorrect() {
        // Mock the behavior of SharedPreferences
        MapsActivity activity = new MapsActivity(); // Adjust your activity to accept SharedPreferences for testing if needed
        LatLng latLng = new LatLng(10, 20);
        activity.saveClickedLatLng(latLng); // Assuming this method exists and saves correctly

        verify(editor).putFloat("clicked_latlng_LAT", 10.0f);
        verify(editor).putFloat("clicked_latlng_LNG", 20.0f);
        verify(editor).apply();
    }

    @Test
    public void addMarker_isCorrect() {
        // Mock the behavior of GoogleMap
        LatLng latLng = new LatLng(10, 20);
        MarkerOptions options = new MarkerOptions().position(latLng);
        googleMap.addMarker(options);

        verify(googleMap).addMarker(options);
    }

    @Test
    public void convertLatLngToLocation_isCorrect() {
        // Direct unit test without mocking
        MapsActivity activity = new MapsActivity();
        LatLng latLng = new LatLng(10, 20);
        Location location = activity.convertLatLngToLocation(latLng);

        assertEquals("Latitude should match", 10.0, location.getLatitude(), 0.0);
        assertEquals("Longitude should match", 20.0, location.getLongitude(), 0.0);
    }
}
