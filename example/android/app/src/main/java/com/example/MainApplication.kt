package com.example
import android.app.Application
import android.content.res.Configuration
import expo.modules.ApplicationLifecycleDispatcher
import expo.modules.ReactNativeHostWrapper
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.soloader.SoLoader
import com.rnthreads.RNThreadPackage

class MainApplication : Application(), ReactApplication {
    // Make a private instance of DefaultReactNativeHost to handle package addition properly
    private val myReactNativeHost = object : DefaultReactNativeHost(this) {
        override fun getPackages(): List<ReactPackage> {
            val packages = PackageList(this).packages
            // Use MainApplication.this when you need to reference the outer class instance
            packages.add(RNThreadPackage(this@MainApplication.reactNativeHost))
            return packages
        }

        override fun getJSMainModuleName(): String = "index"
        override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG
        override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
        override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
    }

    override val reactNativeHost: ReactNativeHost
        get() = ReactNativeHostWrapper(this, myReactNativeHost)

    override val reactHost: ReactHost
        get() = ReactNativeHostWrapper.createReactHost(applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            load()
        }
        ApplicationLifecycleDispatcher.onApplicationCreate(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ApplicationLifecycleDispatcher.onConfigurationChanged(this, newConfig)
    }
}
