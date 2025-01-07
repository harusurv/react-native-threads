package com.rnthreads;
import android.util.Log;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Random;

public class JSThread {
    private int id;
    private String TAG = "JavascriptThread";

    private String jsSlugname;
    private ReactApplicationContext reactContext;

    public JSThread(String jsSlugname, int id) {
        Log.d(TAG, "Initializing thread with ID: " + id + " and slug: " + jsSlugname);
        this.id = id;
        this.jsSlugname = jsSlugname;
    }

    public int getThreadId() {
        return this.id;
    }

    public String getName() {
        return jsSlugname;
    }

    public void runFromContext(ReactApplicationContext context, ReactContextBuilder reactContextBuilder) throws Exception {
        Log.d(TAG, "Running thread from context");
        if (reactContext != null) {
            Log.d("ReactNativeThreads", "Thread already has a context");
            return;
        }

        reactContext = reactContextBuilder.build();

        ThreadSelfModule threadSelfModule = reactContext.getNativeModule(ThreadSelfModule.class);
        threadSelfModule.initialize(id, context);
    }

    public void postMessage(String message) {
        if (reactContext == null) {
            return;
        }

        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("ThreadMessage", message);
    }

    public void onHostResume() {
        if (reactContext == null) {
            Log.e(TAG, "Cannot resume: ReactContext is null");
            return;
        }

        reactContext.onHostResume(null);
    }

    public void onHostPause() {
        if (reactContext == null) {
            Log.e(TAG, "Cannot pause: ReactContext is null");
            return;
        }

        reactContext.onHostPause();
    }

    public void terminate() {
        if (reactContext == null) {
            Log.e(TAG, "Cannot terminare: ReactContext is null");
            return;
        }

        reactContext.onHostPause();
        reactContext.destroy();
        reactContext = null;
    }
}
