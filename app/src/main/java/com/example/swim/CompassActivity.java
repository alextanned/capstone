package com.example.swim;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;


public class CompassActivity extends AppCompatActivity {

    private ImageView compassImageView;
    private TextView headingTextView;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private Sensor accelerometer;
    private float[] magneticFieldValues = new float[3];
    private float[] accelerometerValues = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // Initialize UI elements
        compassImageView = findViewById(R.id.compassImageView);

        headingTextView = findViewById(R.id.headingTextView);

        // Initialize SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Initialize magnetic field and accelerometer sensors
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register sensor listeners
        sensorManager.registerListener(sensorEventListener, magneticFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerValues, 0, 3);
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magneticFieldValues, 0, 3);

                // Calculate azimuth
                float azimuth = calculateAzimuth();

                // Update the UI to display the heading (rotate compassImageView)
                updateUI(azimuth);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Handle accuracy changes if needed.
        }
    };

    private float calculateAzimuth() {
        float[] rotationMatrix = new float[9];
        float[] orientationValues = new float[3];

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        // Calculate the azimuth angle (in radians) and convert it to degrees
        float azimuthInRadians = orientationValues[0];
        float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

        // Ensure the azimuth is within the range of 0-360 degrees
        if (azimuthInDegrees < 0) {
            azimuthInDegrees += 360;
        }

        return azimuthInDegrees;
    }

    private void updateUI(float azimuth) {

        String heading = calculateCompassHeading(azimuth);
        headingTextView.setText(heading);

        // Update your compass UI element (e.g., rotate compassImageView). Not used right now
        // compassImageView.setRotation(-azimuth); // Negative to make the arrow point in the correct direction.
    }

    private String calculateCompassHeading(float azimuth) {
        String[] compassDirections = getResources().getStringArray(R.array.compass_heading);
        int directionIndex = Math.round(azimuth/ 22.5f);

        directionIndex %= compassDirections.length;

        return compassDirections[directionIndex];
    }
}
