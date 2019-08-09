package com.example.preferenceroom_kotlin.annotation

import com.example.preferenceroom_kotlin.PreferenceTypeConverter
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class TypeConverter(val value: KClass<out PreferenceTypeConverter<*>>)