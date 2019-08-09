package com.skydoves.preferenceroomdemo.dagger

import android.content.Context
import com.skydoves.preferenceroomdemo.LoginActivity
import com.skydoves.preferenceroomdemo.MainActivity
import com.skydoves.preferenceroomdemo.entities.Preference_StoresConfig
import dagger.Component
import javax.inject.Singleton

@Component(modules = [StoresPreferenceModule::class])
@Singleton
interface StoresPreferenceComponent {

    fun provideStoresConfig(): Preference_StoresConfig

    fun inject(provider : LoginActivity)

    class Initializer {
        companion object {
            private lateinit var storesPreferenceComponent: StoresPreferenceComponent
            private val lock = Any()

            fun init(context: Context): StoresPreferenceComponent {
                if (!this::storesPreferenceComponent.isInitialized) {
                    synchronized(lock) {
                        if (!this::storesPreferenceComponent.isInitialized) {
                            storesPreferenceComponent = DaggerStoresPreferenceComponent.builder()
                                    .storesPreferenceModule(StoresPreferenceModule(context.applicationContext))
                                    .build()
                        }
                    }
                }
                return storesPreferenceComponent
            }
        }
    }
}