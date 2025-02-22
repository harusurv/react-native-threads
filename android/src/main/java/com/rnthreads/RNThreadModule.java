package com.rnthreads;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JSBundleLoader;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.devsupport.interfaces.DevSupportManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Okio;
import okio.Sink;


public class RNThreadModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

  private String TAG = "ThreadManager";
  private HashMap<Integer, JSThread> threads;
  private int nextThreadId = 1;

  private ReactApplicationContext reactApplicationContext;

  private ReactNativeHost reactNativeHost;

  private ReactPackage additionalThreadPackages[];

  public RNThreadModule(final ReactApplicationContext reactContext, ReactNativeHost reactNativehost, ReactPackage additionalThreadPackages[]) {
    super(reactContext);
    this.reactApplicationContext = reactContext;
    threads = new HashMap<>();
    this.reactNativeHost = reactNativehost;
    this.additionalThreadPackages = additionalThreadPackages;
    reactContext.addLifecycleEventListener(this);
  }

  // https://github.com/harusurv/template-wallet/issues/106#issuecomment-2322604973
  @ReactMethod public void addListener(String eventName) {}
  @ReactMethod public void removeListeners(Integer count) {}

  @Override
  public String getName() {
    return "ThreadManager";
  }

  @ReactMethod
  public void startThread(final String jsFileName, final Promise promise) {
    Log.d(TAG, "Starting web thread - " + jsFileName);

    // When we create the absolute file path later, a "./" will break it.
    // Remove the leading "./" if it exists.
    String jsFileSlug = jsFileName.contains("./")
      ? jsFileName.replace("./", "")
      : jsFileName;

    JSBundleLoader bundleLoader = getDevSupportManager().getDevSupportEnabled()
            ? createDevBundleLoader(jsFileName, jsFileSlug)
            : createReleaseBundleLoader(jsFileName, jsFileSlug);

    try {
      ArrayList<ReactPackage> threadPackages = new ArrayList<ReactPackage>();
      // react-native-threads
      threadPackages.add(new ThreadBaseReactPackage(getReactInstanceManager()));
      // react native runtime
      threadPackages.addAll(getReactInstanceManager().getPackages());
      // thread-specific native modules
      threadPackages.addAll(Arrays.asList(additionalThreadPackages));

      ReactContextBuilder threadContextBuilder = new ReactContextBuilder(getReactApplicationContext())
              .setJSBundleLoader(bundleLoader)
              .setDevSupportManager(getDevSupportManager())
              .setReactInstanceManager(getReactInstanceManager())
              .setReactPackages(threadPackages);

      int id = nextThreadId++;
      JSThread thread = new JSThread(jsFileSlug, id);
      thread.runFromContext(
              getReactApplicationContext(),
              threadContextBuilder
      );
      threads.put(id, thread);
      promise.resolve(id);
    } catch (Exception e) {
      promise.reject(e);
      getDevSupportManager().handleException(e);
    }
  }

  @ReactMethod
  public void stopThread(final int threadId) {
    final JSThread thread = threads.get(threadId);
    if (thread == null) {
      Log.d(TAG, "Cannot stop thread - thread is null for id " + threadId);
      WritableMap params = Arguments.createMap();
      params.putString("threadId", String.valueOf(threadId));
      sendEventToMainInstance("ThreadIsTerminated", params);
      return;
    }

    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        thread.terminate();
        threads.remove(threadId);
      }
    });
  }

  @ReactMethod
  public void postThreadMessage(int threadId, String message) {
    JSThread thread = threads.get(threadId);
    if (thread == null) {
      Log.d(TAG, "Cannot post message to thread - thread is null for id " + threadId);
      WritableMap params = Arguments.createMap();
      params.putString("threadId", String.valueOf(threadId));
      sendEventToMainInstance("ThreadIsTerminated", params);
      return;
    }

    thread.postMessage(message);
  }

  private void sendEventToMainInstance(String eventName, WritableMap params) {
    reactApplicationContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  @Override
  public void onHostResume() {
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        for (int threadId : threads.keySet()) {
          threads.get(threadId).onHostResume();
        }
      }
    });
  }

  @Override
  public void onHostPause() {
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        for (int threadId : threads.keySet()) {
          threads.get(threadId).onHostPause();
        }
      }
    });
  }

  @Override
  public void onHostDestroy() {
    Log.d(TAG, "onHostDestroy - Clean JS Threads");

    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        for (int threadId : threads.keySet()) {
          threads.get(threadId).terminate();
        }
      }
    });
  }

    /*
     *  Helper methods
     */

  private JSBundleLoader createDevBundleLoader(String jsFileName, String jsFileSlug) {
    String bundleUrl = bundleUrlForFile(jsFileName);
    // nested file directory will not exist in the files dir during development,
    // so remove any leading directory paths to simply download a flat file into
    // the root of the files directory.
    String[] splitFileSlug = jsFileSlug.split("/");
    String bundleOut = getReactApplicationContext().getFilesDir().getAbsolutePath() + "/" + splitFileSlug[splitFileSlug.length - 1];

    Log.d(TAG, "createDevBundleLoader - download web thread to - " + bundleOut);
    downloadScriptToFileSync(bundleUrl, bundleOut);

    return JSBundleLoader.createCachedBundleFromNetworkLoader(bundleUrl, bundleOut);
  }

  private JSBundleLoader createReleaseBundleLoader(String jsFileName, String jsFileSlug) {
    Log.d(TAG, "createReleaseBundleLoader - reading file from assets");
    return JSBundleLoader.createAssetLoader(reactApplicationContext, "assets://" + jsFileSlug + ".jsbundle", false);
  }

  private ReactInstanceManager getReactInstanceManager() {
    return reactNativeHost.getReactInstanceManager();
  }

  private DevSupportManager getDevSupportManager() {
    return getReactInstanceManager().getDevSupportManager();
  }

  private String bundleUrlForFile(final String fileName) {
    // http://localhost:8081/index.android.bundle?platform=android&dev=true&hot=false&minify=false
    String sourceUrl = getDevSupportManager().getSourceUrl().replace("http://", "");
    return  "http://"
            + sourceUrl.split("/")[0]
            + "/"
            + fileName
            + ".bundle?platform=android&dev=true&hot=false&minify=false";
  }

  private void downloadScriptToFileSync(String bundleUrl, String bundleOut) {

    // Here we increase the read timeout to avoid crashes on startup
    // when bundling from an empty metro cache:
    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    final File out = new File(bundleOut);

    Request request = new Request.Builder()
            .url(bundleUrl)
            .build();

    try {
      Response response = client.newCall(request).execute();
      if (!response.isSuccessful()) {
        throw new RuntimeException("Error downloading thread script - " + response.toString());
      }

      Sink output = Okio.sink(out);
      Okio.buffer(response.body().source()).readAll(output);
    } catch (IOException e) {
      throw new RuntimeException("Exception downloading thread script to file", e);
    }
  }
}
