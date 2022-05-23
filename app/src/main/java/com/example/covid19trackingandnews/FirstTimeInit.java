package com.example.covid19trackingandnews;

import android.app.Application;

public class FirstTimeInit extends Application {
    public static boolean isInit;
    @Override
    public void onCreate() {
        super.onCreate();
        isInit=false;
    }
}
