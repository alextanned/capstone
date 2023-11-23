package com.example.swim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_CLICKED_LATLNG = "clicked_latlng";
    private static final String PREF_NAME = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupButtonClickListener();

    }
    private void setupButtonClickListener() {
        Button openCompassButton = findViewById(R.id.openCompassButton);
        openCompassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CompassActivity.class);
                startActivity(intent);
            }
        });

        Button openOtherActivityButton = findViewById(R.id.openDestButton);
        openOtherActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove or clear the clickedLatLng related preferences
        clearClickedLatLngPreferences();
    }

    private void clearClickedLatLngPreferences() {
        Log.d("hi","clear");
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_CLICKED_LATLNG + "_LAT");
        editor.remove(KEY_CLICKED_LATLNG + "_LNG");
        editor.commit();
    }

}