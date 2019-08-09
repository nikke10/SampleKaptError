package com.skydoves.preferenceroomdemo

import android.app.Application
import com.facebook.stetho.Stetho

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // initialize Stetho for debugging local data
        Stetho.initializeWithDefaults(this)

        /**
         * initialize instances of preference component and entities.
         * [com.skydoves.preferenceroomdemo.components.UserProfileComponent]
         */
//        PreferenceComponent_UserProfileComponent.init(this)
    }
}
