package com.example.admin.wobeassignment;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.clevertap.android.sdk.ActivityLifecycleCallback;

/**
 * Created by Admin on 21-09-2017.
 */

public class ApplicationLoader extends Application implements
        Application.ActivityLifecycleCallbacks,  ComponentCallbacks2 {

    public static final String TAG = ApplicationLoader.class.getSimpleName();
    private static ApplicationLoader mInstance;
    private static RequestQueue mRequestQueue;
    public static String stateOfLifeCycle = "";

    public static boolean wasInBackground = false;

    @Override
    public void onCreate() {
        ActivityLifecycleCallback.register(this);
        super.onCreate();
        mInstance = this;
        registerActivityLifecycleCallbacks(this);
    }

    public static synchronized ApplicationLoader getInstance() {
        return mInstance;
    }

    public static RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(ApplicationLoader.getInstance());
        }

        return mRequestQueue;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        wasInBackground = false;
        stateOfLifeCycle = "Create";
    }

    @Override
    public void onActivityStarted(Activity activity) {
        stateOfLifeCycle = "Start";
    }

    @Override
    public void onActivityResumed(Activity activity) {
        stateOfLifeCycle = "Resume";
    }

    @Override
    public void onActivityPaused(Activity activity) {
        stateOfLifeCycle = "Pause";
    }

    @Override
    public void onActivityStopped(Activity activity) {
        stateOfLifeCycle = "Stop";
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        wasInBackground = false;
        stateOfLifeCycle = "Destroy";
    }

    @Override
    public void onTrimMemory(int level) {
        if (stateOfLifeCycle.equals("Stop")) {
            wasInBackground = true;
        }
        super.onTrimMemory(level);
    }

    class ScreenOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            wasInBackground = true;
        }
    }
}
