package com.example.swim;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HostActivity extends AppCompatActivity {

    // Define an interface for communication between activity and fragments
    public interface ServerDataListener {
        void onPhoneDataReceived(String phoneData);

    }

    private ServerDataListener serverDataListener;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Socket socket = null;
    private volatile boolean destroy = false;

    private int currentFragmentIndex = 0;
    private final Fragment[] fragments = new Fragment[2]; // Adjust size as needed for more fragments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Check that the activity is using the layout version with the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state, then we don't need to do anything
            // and should return or else we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            fragments[0] = new CompassFragment(); // Index 0 for CompassFragment
            fragments[1] = new StopwatchFragment(); // Index 1 for StopwatchFragment, make sure you create this class

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragments[0]).commit();
        }

        connectToServer();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("key", String.valueOf(keyCode));
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                cycleFragment(true);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                cycleFragment(false);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void cycleFragment(boolean forward) {
        if (forward) {
            currentFragmentIndex = (currentFragmentIndex + 1) % fragments.length;
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.scale_up_slide_in_right, // Updated for incoming fragment
                            R.anim.scale_down_slide_out_left,
                            R.anim.scale_up_slide_in_left, // For popBackStack
                            R.anim.scale_down_slide_out_right) // For popBackStack
                    .replace(R.id.fragment_container, fragments[currentFragmentIndex])
                    .commit();
        } else {
            currentFragmentIndex = (currentFragmentIndex - 1 + fragments.length) % fragments.length;
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.scale_up_slide_in_left, // Updated for incoming fragment
                            R.anim.scale_down_slide_out_right,
                            R.anim.scale_up_slide_in_right, // For popBackStack
                            R.anim.scale_down_slide_out_left) // For popBackStack
                    .replace(R.id.fragment_container, fragments[currentFragmentIndex])
                    .commit();
        }
    }



    public void setServerDataListener(ServerDataListener listener) {
        this.serverDataListener = listener;
    }


    private void connectToServer() {
        //String serverIP = "192.168.159.119";

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                boolean connected = false;
                int attempts = 0;
                int maxAttempts = 1000; // Max number of reconnection attempts
                int retryInterval = 100; // Time to wait before retrying (in milliseconds)

                while (!connected && attempts < maxAttempts && !destroy) {
                    Log.d("debug connected", String.valueOf(connected));
                    try {
                        String serverIP = getRouterIp();
                        int port = 12345;
                        InetAddress ip = InetAddress.getByName(serverIP);
                        socket = new Socket(ip, port);
                        connected = true;
                        Log.d("connetion", "conneted");
                        InputStream inputStream = socket.getInputStream();
                        byte[] buffer = new byte[20];
                        int bytes;
                        bytes = inputStream.read(buffer);
                        Log.d("byte size", String.valueOf(bytes));
                        while (bytes != -1) {
                            Log.d("bytes:", String.valueOf(bytes));
                            String receivedMessage = new String(buffer, 0, bytes);
                            Log.d("received", receivedMessage);
//                            unpackData(receivedMessage);

                            if (serverDataListener != null) {
                                serverDataListener.onPhoneDataReceived(receivedMessage);
                            }
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (phoneData[0] != null){
//                                        headingTextView.setText(phoneData[0] + "m\n");
//                                        Log.d("Message", String.valueOf(phoneData[0]));
//                                    }
//
//                                }
//                            });
                            bytes = inputStream.read(buffer);
                        }

                        connected = false; //if reached this line, this means server
                        inputStream.close();
                    } catch (IOException e) {
//                        e.printStackTrace();
                        attempts++;
                        connected = false;
                        Log.d("connections",String.valueOf(destroy));
                        Log.d("connection", "disconnecteds");
                        try {
                            Thread.sleep(retryInterval); // Wait before retrying
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                if (!connected) {
                    // Handle the scenario when all reconnection attempts fail
                    // Update UI accordingly
                    Log.d("Connection", "max attempts reached");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroy = true;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdownNow();
    }

    private String getRouterIp() {
        final WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final DhcpInfo dhcp = manager.getDhcpInfo();
        final String address = Formatter.formatIpAddress(dhcp.gateway);
        Log.d("router: ", address);
        return address;
    }
}

