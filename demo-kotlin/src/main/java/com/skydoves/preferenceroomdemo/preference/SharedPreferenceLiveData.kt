package com.skydoves.preferenceroomdemo.preference

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedPreferenceLiveData<T> constructor(private val preferences: SharedPreferences,
                                              private val key: String,
                                              private val readValueFromPreferences:suspend() -> T) : MutableLiveData<T>() {

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == this.key) {
            CoroutineScope(Dispatchers.IO).launch {
            postValue(readValueFromPreferences())
            }
        }
    }

    override fun onActive() {
        super.onActive()
        CoroutineScope(Dispatchers.IO).launch {
            postValue(readValueFromPreferences())
        }
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }
}