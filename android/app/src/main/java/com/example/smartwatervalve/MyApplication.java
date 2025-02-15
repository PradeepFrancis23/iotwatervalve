package com.example.smartwatervalve;  

import android.app.Application;
import android.util.Log;
import com.thingclips.smart.home.sdk.ThingHomeSdk;


public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();

        // Initialize Tuya SDK
        ThingHomeSdk.init(this);
        Log.d("Tuya", "✅ Tuya SDK Initialized");
    }
}
