package com.sc.easyloginapp;

import android.app.Application;

import com.sc.easyaccessmodule.FingerPrintManager;

/**
 * Created by Peter on 25.04.2017.
 */

public class App extends Application {
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FingerPrintManager.init(instance);
    }

    public static App getInstance(){
        return instance;
    }
}
