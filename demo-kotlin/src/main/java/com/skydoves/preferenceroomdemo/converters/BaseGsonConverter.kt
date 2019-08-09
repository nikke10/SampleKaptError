package com.skydoves.preferenceroomdemo.converters

import com.example.preferenceroom_kotlin.PreferenceTypeConverter
import com.google.gson.Gson
import kotlin.reflect.KClass

class BaseGsonConverter<T> : PreferenceTypeConverter<T> {

    private val gson: Gson = Gson()

    override fun convertObjectToString(obj: T, keyName : String): String {
        return gson.toJson(obj)
    }

    override fun convertStringToObject(string: String?, keyName : String, clazz : Class<T>): T {
        return gson.fromJson(string, clazz)
    }
}
