package com.encryption;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by cy002 on 2016/10/7.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
    }
}
