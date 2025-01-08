package com.rnthreads;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.hermes.reactexecutor.HermesExecutorFactory;
import com.facebook.react.NativeModuleRegistryBuilder;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.CatalystInstanceImpl;
import com.facebook.react.bridge.JSBundleLoader;
import com.facebook.react.bridge.JSExceptionHandler;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.jscexecutor.JSCExecutorFactory;
import com.facebook.react.bridge.JavaScriptExecutor;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.queue.ReactQueueConfigurationSpec;
import com.facebook.react.devsupport.interfaces.DevSupportManager;
import com.facebook.soloader.SoLoader;
import com.facebook.react.bridge.NativeModuleRegistry;
import com.facebook.react.bridge.NativeModuleCallExceptionHandler;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import static com.facebook.react.modules.systeminfo.AndroidInfoHelpers.getFriendlyDeviceName;

public class ReactContextBuilder {
    private String TAG = "ReactContextBuilder";

    private Context parentContext;
    private JSBundleLoader jsBundleLoader;
    private DevSupportManager devSupportManager;
    private ReactInstanceManager instanceManager;
    private ArrayList<ReactPackage> reactPackages;

    public ReactContextBuilder(Context context) {
        this.parentContext = context;
        SoLoader.init(context, /* native exopackage */ false);
    }

    public ReactContextBuilder setJSBundleLoader(JSBundleLoader jsBundleLoader) {
        this.jsBundleLoader = jsBundleLoader;
        return this;
    }

    public ReactContextBuilder setDevSupportManager(DevSupportManager devSupportManager) {
        this.devSupportManager = devSupportManager;
        return this;
    }

    public ReactContextBuilder setReactInstanceManager(ReactInstanceManager manager) {
        this.instanceManager = manager;
        return this;
    }

    public ReactContextBuilder setReactPackages(ArrayList<ReactPackage> reactPackages) {
        this.reactPackages = reactPackages;
        return this;
    }

    private JavaScriptExecutorFactory getJSExecutorFactory() {
        try {
            String appName = Uri.encode(parentContext.getPackageName());
            String deviceName = Uri.encode(getFriendlyDeviceName());
            // If JSC is included, use it as normal
            SoLoader.loadLibrary("jscexecutor");
            return new JSCExecutorFactory(appName, deviceName);
        } catch (UnsatisfiedLinkError jscE) {
            // Otherwise use Hermes
            return new HermesExecutorFactory();
        }
    }

    public ReactApplicationContext build(ReactApplicationContext reactContext) throws Exception {

        try{
            Log.d(TAG, "Create javascript executor factory A");

            JavaScriptExecutor jsExecutor = getJSExecutorFactory().create();
            Log.d(TAG, "Create javascript executor factory B");

            // load native modules
            NativeModuleRegistryBuilder nativeRegistryBuilder = new NativeModuleRegistryBuilder(reactContext, this.instanceManager);
            addNativeModules(nativeRegistryBuilder);

            Log.d(TAG, "Create javascript executor factory C");

            // Logging to check if jsExecutor is null
            if (jsExecutor == null) {
                Log.d(TAG, "jsExecutor is null");
            } else {
                Log.d(TAG, "jsExecutor is not null");
            }

            // Check ReactQueueConfigurationSpec
            ReactQueueConfigurationSpec configSpec = ReactQueueConfigurationSpec.createDefault();
            if (configSpec == null) {
                Log.d(TAG, "ReactQueueConfigurationSpec is null");
            } else {
                Log.d(TAG, "ReactQueueConfigurationSpec is not null");
            }

            // Check if nativeRegistry is null
            NativeModuleRegistry nativeRegistry = nativeRegistryBuilder.build();
            if (nativeRegistry == null) {
                Log.d(TAG, "nativeRegistry is null");
            } else {
                Log.d(TAG, "nativeRegistry is not null");
            }

            // Check if jsBundleLoader is null
            if (jsBundleLoader == null) {
                Log.d(TAG, "jsBundleLoader is null");
            } else {
                Log.d(TAG, "jsBundleLoader is not null");
            }
            CatalystInstanceImpl.Builder catalystInstanceBuilder = new CatalystInstanceImpl.Builder()
                    .setReactQueueConfigurationSpec(configSpec)
                    .setJSExecutor(jsExecutor)
                    .setRegistry(nativeRegistry)
                    .setJSBundleLoader(jsBundleLoader);
                    .setNativeModuleCallExceptionHandler(new NativeModuleCallExceptionHandler() {
                        @Override
                        public void handleException(Exception e) {
                            // Handle the exception as needed
                            throw new RuntimeException(e);
                        }
                    });
            Log.d(TAG, "Create javascript executor factory D");

            final CatalystInstance catalystInstance = catalystInstanceBuilder.build();
            Log.d(TAG, "Create javascript executor factory E");

            reactContext.initializeWithInstance(catalystInstance);
            Log.d(TAG, "Create javascript executor factory F");

            catalystInstance.runJSBundle();
            Log.d(TAG, "Create javascript executor factory G");

            return reactContext;
        }
        catch (Exception e) {
            Log.e(TAG, "Error building React context A: ");
            throw e; // Re-throw to handle it further up if necessary
        }
        
    }


    private JSExceptionHandler createNativeModuleExceptionHandler() {
        return new JSExceptionHandler() {
            @Override
            public void handleException(Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void addNativeModules(NativeModuleRegistryBuilder nativeRegistryBuilder) {
        for (int i = 0; i < reactPackages.size(); i++) {
            ReactPackage reactPackage = reactPackages.get(i);
            nativeRegistryBuilder.processPackage(reactPackage);
        }
    }
}