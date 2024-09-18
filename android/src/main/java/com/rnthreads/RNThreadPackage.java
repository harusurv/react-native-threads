package com.rnthreads;

import com.facebook.react.ReactPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rnthreads.RNFileReaderPackage;

public class RNThreadPackage implements ReactPackage {

    private ReactNativeHost reactNativeHost;
    private ReactPackage additionalThreadPackages[];

    public RNThreadPackage(ReactNativeHost pReactNativeHost, ReactPackage ...pAdditionalThreadPackages) {
        this.reactNativeHost = pReactNativeHost;

        // You can add more packages here if you find that using some
        // vanilla RN functionality leaves the background thread unable
        // to mount properly.
        ReactPackage[] extraPackages = {
            new RNFileReaderPackage() /* https://github.com/facebook/react-native/blob/7ea7d946c643f076c29bcf11b927f7569e3c516f/Libraries/Core/setUpXHR.js#L31 */
            // ...
        };

        // Create an array large enough to acommodate for both
        // the user-defined `pAdditionalThreadPackages` and the
        // `extraPackages`:
        this.additionalThreadPackages = new ReactPackage[pAdditionalThreadPackages.length + extraPackages.length];

        // i.e. [...pAdditionalThreadPackages, ...extraPackages];
        System.arraycopy(pAdditionalThreadPackages, 0, this.additionalThreadPackages, 0, pAdditionalThreadPackages.length);
        System.arraycopy(extraPackages, 0, this.additionalThreadPackages, pAdditionalThreadPackages.length, extraPackages.length);
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(
                new RNThreadModule(reactContext, reactNativeHost, additionalThreadPackages)
        );
    }
}