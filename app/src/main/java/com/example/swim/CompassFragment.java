package com.example.swim;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CompassFragment extends Fragment implements HostActivity.ServerDataListener{
    private PerspectiveImageView compassImageView;
    private TextView headingTextView;
    private TextView directionTextView;
    private ImageView cloudImageView;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private Sensor rotationVectorSensor;
    private Sensor accelerometer;
    private Sensor gravitySensor;
    private float[] magneticFieldValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private float[] gravityValues = new float[3];
    private Integer[] phoneData = new Integer[3]; //distance, delta bearing, bearing latlng
    private String weather;

    private float headingDegrees = 0f;


    private Socket socket = null;
    static final float ALPHA = 0.25f;
    private volatile boolean destroy;
    private float prevAzimuth = -1000;
    private boolean orientation = false;
    private Handler handler = new Handler();
    private int flashCount = 0;
    private boolean flashSequenceStarted = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_compass, container, false);
        setRetainInstance(true);
//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//        setContentView(R.layout.activity_compass);

        // Initialize UI elements
        compassImageView = view.findViewById(R.id.compassImageView);

        headingTextView = view.findViewById(R.id.headingTextView);
        Log.d("onCreate", "OnCreate");
        directionTextView = view.findViewById(R.id.directionTextView);

        cloudImageView = view.findViewById(R.id.cloudImageView);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorManager.registerListener(sensorEventListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
//        if (rotationVectorSensor == null) {
//            headingTextView.setText("NOT AVAILABLE!");
//        }else{
//            headingTextView.setText("AVAILABLE!");
//        }
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


//        // Initialize magnetic field and accelerometer sensors
//        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        // Register sensor listeners
//        sensorManager.registerListener(sensorEventListener, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);

        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_UI);

        destroy = false;
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        // Register the sensor listener when the activity is resumed
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(sensorEventListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (accelerometer != null){
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (gravitySensor != null){
            sensorManager.registerListener(sensorEventListener, gravitySensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the sensor listener when the activity is paused
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HostActivity) {
            ((HostActivity) context).setServerDataListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() instanceof HostActivity) {
            ((HostActivity) getActivity()).setServerDataListener(null);
        }
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        Log.d("onDestrony", "destroy");
        if (socket != null && !socket.isClosed()) {
            Log.d("onDestroy", "close socket");
            try {
                InputStream inputStream = socket.getInputStream();
                inputStream.close();
                socket.close(); // Close the socket

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        executorService.shutdown();
        destroy = true;
        handler.removeCallbacksAndMessages(null);
    }



    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor == accelerometer){
                accelerometerValues = lowPass(event.values.clone(), accelerometerValues);
            }
            if (event.sensor == gravitySensor){
                gravityValues = event.values;
            }
//            Log.d("accuracy", String.valueOf(accuracy));
            if (event.sensor == rotationVectorSensor) {

                float azimuth = calculateAzimuth(event);
                updateUI(azimuth);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private float calculateAzimuth(SensorEvent event) {
        /*
        float[] rotationMatrix = new float[16];
        float[] orientationValues = new float[3];
        float[] mappedRotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);


//        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(mappedRotationMatrix, orientationValues);

        // Calculate the azimuth angle (in radians) and convert it to degrees
//        float azimuth = orientationValues[0]; // azimuth (yaw) in radians
//        float pitch = orientationValues[1];   // pitch in radians
//        float roll = orientationValues[2];    // roll in radians
        float azimuthInRadians = orientationValues[0];
        */

        orientation = Math.abs(gravityValues[1]) > Math.abs(gravityValues[2]);

        float[] rotationMatrix = new float[16];
        float[] adjustedRotationMatrix = new float[16];
        float[] orientationValues = new float[3];

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
//        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix);
//        SensorManager.getOrientation(adjustedRotationMatrix, orientationValues);
        if (orientation){
//            if (gravityValues[1] < 0){
//                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, adjustedRotationMatrix);
//            }
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix);
        } else{
            if (gravityValues[2] <0 ){
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Y, adjustedRotationMatrix);
            } else {
                adjustedRotationMatrix = rotationMatrix.clone();
            }
        }
        SensorManager.getOrientation(adjustedRotationMatrix, orientationValues);
//        Log.d("gravity", Arrays.toString(gravityValues));




        float azimuthInRadians = orientationValues[0];
        float pitchInRadians = orientationValues[1];
        float rollInRadians = orientationValues[2];

//        if(Math.abs(Math.toDegrees(event.values[1])) > 45){
//            if (Math.toDegrees(event.values[1]) < 0){
//                event.values[1] += 45;
//            }
//        }

        // Ensure the azimuth is within the range of 0-360 degrees
//        if (azimuthInDegrees < 0) {
//            azimuthInDegrees += 360;
//        }
        float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
//        prevAzimuth = azimuthInDegrees;
        return azimuthInDegrees;
    }

    private void updateUI(float azimuth) {
        int newAzimuth = heuristicHeading(azimuth);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String heading = calculateCompassHeading(azimuth);
                directionTextView.setText(heading);
                if (phoneData[0] != null){
                    headingTextView.setText(phoneData[0] + "m\n");
                    Log.d("Message", String.valueOf(phoneData[0]));
                }
//

//        Log.d("newAz", String.valueOf(newAzimuth));
                // Update your compass UI element (e.g., rotate compassImageView). Not used right now
//         compassImageView.setRotation(newAzimuth); // Negative to make the arrow point in the correct direction.

                compassImageView.setPerspectiveRotation(-newAzimuth);
            }
        });
    }

    private int heuristicHeading(float azimuth) {
        int relativeHeading = 0;
        if (phoneData[2] != null) {
            relativeHeading = phoneData[2] - (int) azimuth;
            if (relativeHeading < 0) {
                relativeHeading += 360;
            }

        } else { //no data, act as compass for debugging
            relativeHeading = (int) azimuth;
        }
        return relativeHeading;
    }




    private String calculateCompassHeading(float azimuth) {
        String[] compassDirections = getResources().getStringArray(R.array.compass_heading);
        if (azimuth < 0) {
            azimuth += 360;
        }
        int directionIndex = Math.round(azimuth/ 45);
//        directionIndex = directionIndex % 8;

        directionIndex %= compassDirections.length;

        return compassDirections[directionIndex];
    }


    protected float[] lowPass( float[] input, float[] output ) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onPhoneDataReceived(String data) {
        if (data.replaceAll("\\s+","") == ""){
            return;
        }
        String[] type = data.replaceAll("\\s+","").split(":");
        Log.d("SPLIT",type[1]);
        String[] arr = type[1].split(",");
        if(type[0].equals("0")) {
            int i = 0;
            for (String s : arr) {
                phoneData[i] = Integer.valueOf(s);
                i++;
            }
            if(flashSequenceStarted){
                stopFlashingSequence();
            }
        }else if (type[0].equals("1")){
            weather = arr[0];
            if (!flashSequenceStarted){
                startFlashingSequence();
            }
        }

    }

//    private void unpackData(String data){
//        if (data.replaceAll("\\s+","") == ""){
//            return;
//        }
//        String[] type = data.replaceAll("\\s+","").split(":");
//        Log.d("SPLIT",type[1]);
//        String[] arr = type[1].split(",");
//        if(type[0].equals("0")) {
//            int i = 0;
//            for (String s : arr) {
//                phoneData[i] = Integer.valueOf(s);
//                i++;
//            }
//            if(flashSequenceStarted){
//                stopFlashingSequence();
//            }
//        }else if (type[0].equals("1")){
//            weather = arr[0];
//            if (!flashSequenceStarted){
//                startFlashingSequence();
//            }
//        }
//
//    }


    private void stopFlashingSequence() {
        // Use runOnUiThread to update the UI on the main thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Remove any pending callbacks from the handler
                handler.removeCallbacksAndMessages(null);

                // Hide the icon by setting its visibility to invisible
                cloudImageView.setVisibility(View.INVISIBLE);

                // Reset the flash count
                flashCount = 0;
            }
        });
        flashSequenceStarted = false;
    }


    // Method to start the flashing sequence
    private void startFlashingSequence() {
        flashSequenceStarted = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Toggle visibility
                        if (cloudImageView.getVisibility() == View.VISIBLE) {
                            cloudImageView.setVisibility(View.INVISIBLE);
                        } else {
                            cloudImageView.setVisibility(View.VISIBLE);
                        }
                    }
                });
                flashCount++;

                // Check if 3 flashes have occurred
                if (flashCount < 3) {
                    // Schedule the next flash after a 1-second interval
                    handler.postDelayed(this, 1000);
                } else {
                    // Reset flash count
                    flashCount = 0;

                    // Schedule the next flashing sequence after a 5-second interval
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startFlashingSequence();
                        }
                    }, 5000);
                }
            }
        }, 1000); // Start the first flash after a 1-second delay

    }

}
