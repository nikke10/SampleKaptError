package com.example.preferenceroom_kotlin.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class KeyName(val value: String = "", val reactive: Boolean = false)