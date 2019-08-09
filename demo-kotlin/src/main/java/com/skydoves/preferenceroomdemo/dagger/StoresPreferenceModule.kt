package com.skydoves.preferenceroomdemo.dagger

import android.content.Context
import com.skydoves.preferenceroomdemo.entities.Preference_StoresConfig
import dagger.Module
import dagger.Provides

@Module
open class StoresPreferenceModule(private val context: Context) {

    @Provides
    open fun provideContext(): Context {
        return context
    }

    @Provides
    open fun provideStoresConfig()= Preference_StoresConfig()
}