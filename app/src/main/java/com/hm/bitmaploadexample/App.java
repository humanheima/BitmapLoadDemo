package com.hm.bitmaploadexample;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 2017/1/5.
 */
public class App extends Application {

    private static Application context;

    public static Context getInstance() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
