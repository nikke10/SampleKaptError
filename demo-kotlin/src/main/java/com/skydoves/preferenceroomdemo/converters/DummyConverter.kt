package com.skydoves.preferenceroomdemo.converters

import com.example.preferenceroom_kotlin.PreferenceTypeConverter
import javax.inject.Inject
import kotlin.reflect.KClass

class DummyConverter @Inject constructor() : PreferenceTypeConverter<String> {
    override fun convertObjectToString(obj: String, keyName: String): String {
        return obj
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun convertStringToObject(value: String?, keyName: String, clazz: Class<String>): String {
        return value?:""
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}