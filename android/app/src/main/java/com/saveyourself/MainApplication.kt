package com.saveyourself

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeApplicationEntryPoint.loadReactNative
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.ReactInstanceManager // Import ReactInstanceManager
import com.facebook.react.bridge.ReactContext // Import ReactContext

class MainApplication : Application(), ReactApplication {

    // Make ReactInstanceManager accessible statically
    companion object {
        @JvmStatic
        var reactInstanceManager: ReactInstanceManager? = null
    }

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages(): List<ReactPackage> =
                PackageList(this).packages.apply {
                    add(AppPackage())
                }

            override fun getJSMainModuleName(): String = "index"

            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

            override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        loadReactNative(this) // This initializes the React Native environment
        reactInstanceManager = reactNativeHost.reactInstanceManager

        // This is crucial: Ensure the ReactContext is loaded and ready
        // The reactNativeHost.reactInstanceManager already handles the creation of the ReactContext
        // when loadReactNative(this) is called. We just need to make sure it's accessible
        // and its catalyst instance is active when the service needs it.
        // The retry logic in AppMonitorService will now wait for this to be truly ready.
    }

    // Optional: A helper to get the active ReactContext
    fun getActiveReactContext(): ReactContext? {
        return reactInstanceManager?.currentReactContext
    }
}
