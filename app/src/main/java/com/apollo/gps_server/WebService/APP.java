package com.apollo.gps_server.WebService;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;
import android.provider.Settings;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by cjw on 16/6/10.
 */
public class APP extends Application {


    private static  APP app = null;

    public String androidId;

    @Override
    public void onCreate() {
        super.onCreate();
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        SDKInitializer.initialize(getApplicationContext());
        app = this;
    }


    public static APP getAPP()
    {
        return app;
    }

}
