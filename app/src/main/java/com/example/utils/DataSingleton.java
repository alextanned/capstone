package com.example.utils;
import java.util.HashMap;

public class DataSingleton {
    private static DataSingleton instance;
    private HashMap<String, Integer> sharedData = new HashMap<String, Integer>();

    private DataSingleton() {
        // private constructor to prevent instantiation
    }

    public static synchronized DataSingleton getInstance() {
        if (instance == null) {
            instance = new DataSingleton();
        }
        return instance;
    }

    public Integer getSharedData(String key) {
        return sharedData.get(key);
    }

    public void setSharedData(String key, int data) {
        this.sharedData.put(key, data);
    }
}
