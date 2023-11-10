package com.example.utils;

public class DataSingleton {
    private static DataSingleton instance;
    private int sharedData = 0;

    private DataSingleton() {
        // private constructor to prevent instantiation
    }

    public static synchronized DataSingleton getInstance() {
        if (instance == null) {
            instance = new DataSingleton();
        }
        return instance;
    }

    public int getSharedData() {
        return sharedData;
    }

    public void setSharedData(int data) {
        this.sharedData = data;
    }
}
