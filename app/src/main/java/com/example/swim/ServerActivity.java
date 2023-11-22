package com.example.swim;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.service.controls.ControlsProviderService.TAG;

public class ServerActivity extends AppCompatActivity {

    private TextView textViewServerIP;
    private EditText editTextMessage;
    private Button buttonSend;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final int PORT = 12345; // Example port number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        textViewServerIP = findViewById(R.id.textViewServerIP);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Display server IP address (You'll need to implement a method to get the actual IP)
        //textViewServerIP.setText("Server IP: " + getServerIP());

        startServer();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                sendMessage(message);
            }
        });
    }

    private void startServer() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.bind(new InetSocketAddress(getServerIP(), PORT));
                    textViewServerIP.setText(serverSocket.getInetAddress().toString());
                    clientSocket = serverSocket.accept(); // Accepts a connection

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendMessage(String message) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    try {
                        OutputStream outputStream = clientSocket.getOutputStream();
                        outputStream.write(message.getBytes());
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
    protected void onDestroy() {
        super.onDestroy();
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
    }
}
