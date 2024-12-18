package com.rnthreads;

import android.content.Context;
import android.net.Uri;

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

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static com.facebook.react.modules.systeminfo.AndroidInfoHelpers.getFriendlyDeviceName;

public class ReactContextBuilder {

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
        JavaScriptExecutor jsExecutor = getJSExecutorFactory().create();

        if (devSupportManager != null) {
            reactContext.setNativeModuleCallExceptionHandler(devSupportManager);
        }

        // load native modules
        NativeModuleRegistryBuilder nativeRegistryBuilder = new NativeModuleRegistryBuilder(reactContext, this.instanceManager);
        addNativeModules(nativeRegistryBuilder);

        CatalystInstanceImpl.Builder catalystInstanceBuilder = new CatalystInstanceImpl.Builder()
                .setReactQueueConfigurationSpec(ReactQueueConfigurationSpec.createDefault())
                .setJSExecutor(jsExecutor)
                .setRegistry(nativeRegistryBuilder.build())
                .setJSBundleLoader(jsBundleLoader)
                .setNativeModuleCallExceptionHandler(devSupportManager != null ? devSupportManager : createNativeModuleExceptionHandler());

        final CatalystInstance catalystInstance = catalystInstanceBuilder.build();

        reactContext.initializeWithInstance(catalystInstance);
        catalystInstance.runJSBundle();

        return reactContext;
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