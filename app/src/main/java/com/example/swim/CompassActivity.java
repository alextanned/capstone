package com.example.swim;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.format.Formatter;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CompassActivity extends AppCompatActivity {

    private ImageView compassImageView;
    private TextView headingTextView;
    private TextView directionTextView;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;
    private Sensor rotationVectorSensor;
    private Sensor accelerometer;
    private float[] magneticFieldValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private Integer[] phoneData = new Integer[3]; //distance, delta bearing, bearing latlng

    private float headingDegrees = 0f;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Socket socket = null;
    static final float ALPHA = 0.25f;
    boolean higher = true;
    private float prevAzimuth = -1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        // Initialize UI elements
        compassImageView = findViewById(R.id.compassImageView);

        headingTextView = findViewById(R.id.headingTextView);
        directionTextView = findViewById(R.id.directionTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorManager.registerListener(sensorEventListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        connectToServer();

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the activity is resumed
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(sensorEventListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelerometer != null){
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener when the activity is paused
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        executorService.shutdownNow();
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close(); // Close the socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectToServer() {
        //String serverIP = "192.168.159.119";
        String serverIP = getRouterIp();
        int port = 12345;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress ip =  InetAddress.getByName(serverIP);
                    socket = new Socket(ip, port);
                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[20];
                    int bytes;
                    while ((bytes = inputStream.read(buffer)) != -1) {
                        String receivedMessage = new String(buffer, 0, bytes);
                        unpackData(receivedMessage);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                headingTextView.setText(String.valueOf(phoneData[0]) + "m\n");
                                Log.d("Message", String.valueOf(phoneData[0]));
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor== accelerometer){
                accelerometerValues = lowPass(event.values.clone(), accelerometerValues);
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
        float[] rotationMatrix = new float[16];
        float[] orientationValues = new float[3];
        float[] mappedRotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        if (accelerometerValues[2]<0){
            if(higher) {
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Y, mappedRotationMatrix);
            }else{
                higher = true;
                return prevAzimuth;
            }
        }else{
            if(!higher || prevAzimuth == -1000) {
                mappedRotationMatrix = rotationMatrix;
            }else{
                higher = false;
                return prevAzimuth;
            }
        }


//        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(mappedRotationMatrix, orientationValues);

        // Calculate the azimuth angle (in radians) and convert it to degrees
//        float azimuth = orientationValues[0]; // azimuth (yaw) in radians
//        float pitch = orientationValues[1];   // pitch in radians
//        float roll = orientationValues[2];    // roll in radians
        float azimuthInRadians = orientationValues[0];

        float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

//        if(Math.abs(Math.toDegrees(event.values[1])) > 45){
//            if (Math.toDegrees(event.values[1]) < 0){
//                event.values[1] += 45;
//            }
//        }

        // Ensure the azimuth is within the range of 0-360 degrees
//        if (azimuthInDegrees < 0) {
//            azimuthInDegrees += 360;
//        }
        prevAzimuth = azimuthInDegrees;
        return azimuthInDegrees;
    }

    private void updateUI(float azimuth) {

        String heading = calculateCompassHeading(azimuth);
        directionTextView.setText(heading);
//
        int newAzimuth = heuristicHeading(azimuth);
        Log.d("newAz", String.valueOf(newAzimuth));
        // Update your compass UI element (e.g., rotate compassImageView). Not used right now
         compassImageView.setRotation(newAzimuth); // Negative to make the arrow point in the correct direction.
    }

    private int heuristicHeading(float azimuth) {
        int relativeHeading = 0;
        if (phoneData[2] != null) {
            relativeHeading = phoneData[2] - (int) azimuth;
            if (relativeHeading < 0) {
                relativeHeading += 360;
            }

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

    private void unpackData(String data){
        String[] arr = data.replaceAll("\\s+","").split(",");
        int i = 0;
        for (String s : arr){
            phoneData[i] = Integer.valueOf(s);
            i++;
        }

    }

    private String getRouterIp() {
        final WifiManager manager = (WifiManager) super.getApplicationContext().getSystemService(WIFI_SERVICE);
        final DhcpInfo dhcp = manager.getDhcpInfo();
        final String address = Formatter.formatIpAddress(dhcp.gateway);
        return address;
    }

}
