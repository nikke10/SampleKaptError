package com.example.preferenceroom_kotlin

import com.example.preferenceroom_kotlin.annotation.KeyName
import com.example.preferenceroom_kotlin.annotation.PreferenceEntity
import com.example.preferenceroom_kotlin.annotation.TypeConverter

@PreferenceEntity("dummy", true)
class Dummy {

    @KeyName("dummyKey", true)
//    @TypeConverter(DummyConverter::class)
    lateinit var key: String
}