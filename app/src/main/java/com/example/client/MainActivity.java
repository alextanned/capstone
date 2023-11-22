package com.example.client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText editTextServerIP, editTextPort;
    private Button buttonConnect;
    private TextView textViewMessages;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Socket socket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        editTextServerIP = findViewById(R.id.editTextServerIP);
        editTextPort = findViewById(R.id.editTextPort);
        buttonConnect = findViewById(R.id.buttonConnect);
        textViewMessages = findViewById(R.id.textViewMessages);

//        buttonConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                connectToServer();
//            }
//        });
        connectToServer();
    }

    private void connectToServer() {
//        String serverIP = editTextServerIP.getText().toString();
//        int port = Integer.parseInt(editTextPort.getText().toString());
        String serverIP = "192.168.43.183";
        int port = 12345;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress ip =  InetAddress.getByName(serverIP);
                    socket = new Socket(ip, port);
                    InputStream inputStream = socket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytes;
                    while ((bytes = inputStream.read(buffer)) != -1) {
                        String receivedMessage = new String(buffer, 0, bytes);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewMessages.append(receivedMessage + "\n");
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow(); // Shut down the executor service immediately
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close(); // Close the socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}