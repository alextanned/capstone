package com.example.swim;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SpeedFragment extends Fragment {
    // Example TextView for displaying pace
    private TextView paceTextView;
    private SensorManager sensorManager;
    private Sensor linearAccelerationSensor;
    private final float[] linearAcceleration = new float[3];
    private long startTime;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_speed, container, false);
        paceTextView = view.findViewById(R.id.speedTextView);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(sensorEventListener, linearAccelerationSensor, SensorManager.SENSOR_DELAY_UI);
        // Assuming a method to update pace, possibly triggered by sensor events
//         updatePace(pace);
        // Listen for key events on the view
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    // Toggle the unit display
//                    getActivity().runOnUiThread(() -> paceTextView.setText(String.format("%s min/100m", linearAcceleration[0])));
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                System.arraycopy(event.values, 0, linearAcceleration, 0, event.values.length);

                // Process the linear acceleration data to estimate distance and speed
                // This is highly simplified; real implementations require complex logic

                updatePace(linearAcceleration[0]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Handle sensor accuracy changes if needed
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        if (linearAccelerationSensor != null) {
            sensorManager.registerListener(sensorEventListener, linearAccelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
        startTime = System.currentTimeMillis();
    }

    // Method to update the displayed swimming pace
    public void updatePace(float pace) {
        if (paceTextView != null) {
            getActivity().runOnUiThread(() -> paceTextView.setText(String.format("%s min/100m", pace)));
        }
    }
}
