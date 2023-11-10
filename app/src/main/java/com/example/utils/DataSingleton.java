package com.example.utils;

public class DataSingleton {
    private static DataSingleton instance;
    private double sharedData = 0;

    private DataSingleton() {
        // private constructor to prevent instantiation
    }

    public static synchronized DataSingleton getInstance() {
        if (instance == null) {
            instance = new DataSingleton();
        }
        return instance;
    }

    public double getSharedData() {
        return sharedData;
    }

    public void setSharedData(double data) {
        this.sharedData = data;
    }
}
