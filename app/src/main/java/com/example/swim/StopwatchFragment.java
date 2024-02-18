package com.example.swim;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class StopwatchFragment extends Fragment {

    private TextView stopwatchTextView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int pressCounter = 0;
    private long lastPressTime = 0;
    private final long MAX_CONSECUTIVE_PRESS_INTERVAL = 300; // 0.3 seconds allowed between presses

    private StopwatchViewModel viewModel;
    private TextView instructionsTextView;

    private Runnable updateTimeRunnable = new Runnable() {
        public void run() {
            if (viewModel.isRunning()) {
                long currentElapsedTime = System.currentTimeMillis() - viewModel.getStartTime() + viewModel.getElapsedTime();
                updateStopwatchText(currentElapsedTime);
                handler.postDelayed(this, 50); // Update the text view every 50 milliseconds
            }
        }
    };
    private void updateStopwatchText(long elapsedTime) {
        int minutes = (int) (elapsedTime / (1000 * 60)) % 60;
        int seconds = (int) (elapsedTime / 1000) % 60;
        int milliseconds = (int) (elapsedTime % 1000) / 10; // Displaying milliseconds as two digits
        stopwatchTextView.setText(String.format("%02d:%02d:%02d", minutes, seconds, milliseconds));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        stopwatchTextView = view.findViewById(R.id.stopwatchTextView);
        viewModel = new ViewModelProvider(requireActivity()).get(StopwatchViewModel.class);
        instructionsTextView = view.findViewById(R.id.instructionsTextView);

        String instructionText = "Press <b>SELECT</b> to Start/Stop  Double press <b>SELECT</b> to reset";
        instructionsTextView.setText(HtmlCompat.fromHtml(instructionText, HtmlCompat.FROM_HTML_MODE_LEGACY));

        updateStopwatchText(viewModel.getElapsedTime());
        if (viewModel.isRunning()) {
            handler.postDelayed(updateTimeRunnable, 0);
            instructionsTextView.setVisibility(View.INVISIBLE);
        }
        if (viewModel.getElapsedTime() != 0){
            instructionsTextView.setVisibility(View.INVISIBLE);
        }
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastPressTime < MAX_CONSECUTIVE_PRESS_INTERVAL) {
                        pressCounter++;
                    } else {
                        pressCounter = 1; // Reset counter if time between presses is too long
                    }
                    lastPressTime = currentTime;

                    if (pressCounter == 2) {
                        resetStopwatch();
                        pressCounter = 0; // Reset counter after resetting stopwatch
                        return true; // Indicates the event was handled
                    } else {
                        startStopStopwatch(); // Start/Stop the stopwatch for the first two presses
                    }
                    Log.d("Timer", String.valueOf(pressCounter));
                    return true; // Indicates the event was handled
                }
                return false; // Let other keys be handled normally
            }
        });

        return view;
    }

    public void startStopStopwatch() {
        instructionsTextView.setVisibility(View.INVISIBLE);
        if (viewModel.isRunning()) {
            handler.removeCallbacks(updateTimeRunnable);
            viewModel.setElapsedTime(viewModel.getElapsedTime() + (System.currentTimeMillis() -  viewModel.getStartTime()));
        } else {
            viewModel.setStartTime(System.currentTimeMillis());
            handler.postDelayed(updateTimeRunnable, 0);
        }
        viewModel.setRunning(!viewModel.isRunning());
    }

    public void resetStopwatch() {
        viewModel.setRunning(false);
//        startTime = System.currentTimeMillis();
        viewModel.setElapsedTime(0);
        handler.removeCallbacks(updateTimeRunnable);
        stopwatchTextView.setText("00:00.00");
        instructionsTextView.setVisibility(View.VISIBLE);
    }


}


