package com.example.swim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
<<<<<<< HEAD
import android.content.SharedPreferences;
=======
>>>>>>> 0a7f7a741a4e11e46b564a9c91d6a32edc4c310d
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

public class MainActivity extends AppCompatActivity {

<<<<<<< HEAD
    private static final String KEY_CLICKED_LATLNG = "clicked_latlng";
    private static final String PREF_NAME = "MyPrefs";

=======
>>>>>>> 0a7f7a741a4e11e46b564a9c91d6a32edc4c310d
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
<<<<<<< HEAD
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove or clear the clickedLatLng related preferences
        clearClickedLatLngPreferences();
    }

    private void clearClickedLatLngPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_CLICKED_LATLNG + "_LAT");
        editor.remove(KEY_CLICKED_LATLNG + "_LNG");
        editor.apply();
    }
=======

        Button openServerButton = findViewById(R.id.openServerButton);
        openServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ServerActivity.class);
                startActivity(intent);
            }
        });

        Button openClientButton = findViewById(R.id.openClientButton);
        openClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(intent);
            }
        });

    }


>>>>>>> 0a7f7a741a4e11e46b564a9c91d6a32edc4c310d

}