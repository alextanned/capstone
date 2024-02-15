package com.example.swim;

import androidx.lifecycle.ViewModel;

public class StopwatchViewModel extends ViewModel {
    private boolean running = false;
    private long startTime = 0;
    private long elapsedTime = 0;

    // Getters and setters
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
