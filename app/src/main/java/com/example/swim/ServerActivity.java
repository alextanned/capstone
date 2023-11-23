package com.example.swim;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetSocketAddress;

import static android.service.controls.ControlsProviderService.TAG;

public class ServerActivity extends Service {
    private static final String TAG = "MyServerService";
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private static final String NOTIFICATION_CHANNEL_ID = "ServerActivityChannel";// for foreground activity

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final int PORT = 12345; // Example port number
    private static ServerActivity instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startServer();
        startForegroundService();

    }
    public static ServerActivity getInstance() {
        return instance;
    }


    private void startServer() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.bind(new InetSocketAddress(getServerIP(), PORT));
                    Log.d(TAG,"host ip is " + getServerIP());
                    clientSocket = serverSocket.accept(); // Accepts a connection
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendLocationData(String distance, String bearing, String absoluteBearing) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    try {
                        String dataToSend = distance + "," + bearing + "," + absoluteBearing;
                        // Define the fixed string length
                        int fixedLength = 20;
                        // Use String.format to pad the string with spaces
                        String paddedString = String.format("%-" + fixedLength + "s", dataToSend);
                        OutputStream outputStream = clientSocket.getOutputStream();
                        outputStream.write(paddedString.getBytes());
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String getServerIP() {
        // Implement logic to retrieve the device's IP address
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                            .hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
                            Log.d(TAG, inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        executorService.shutdownNow(); // Shut down the executor service immediately
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Your Service is Running")
                .setContentText("App is still running")
                .setSmallIcon(R.drawable.up_arrow)  // Replace with your notification icon
                .build();

        startForeground(1, notification);
    }
}
