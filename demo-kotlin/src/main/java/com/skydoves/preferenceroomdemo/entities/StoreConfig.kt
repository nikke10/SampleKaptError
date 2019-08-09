package com.skydoves.preferenceroomdemo.entities

import com.example.preferenceroom_kotlin.annotation.*
import com.skydoves.preferenceroomdemo.converters.DummyConverter

@PreferenceEntity("stores_config", true)
open class StoreConfig {

    @KeyName(value = "base_image_url")
    private val imageUrl = "https://docstore/images"

    @KeyName(value = "list_page_size", reactive = false)
    private val listPageSize = 50

    @KeyName(value = "age")
    private val age = 10

    @KeyName("search_throttle")
    private val searchThrottle = 400L

    @KeyName("map_zoom_level")
    private val mmiMapZoomLevel = 14f

    @KeyName("nick_name")
    @TypeConverter(DummyConverter::class)
    private val nickName= ""

    @KeyName("rating_max_stars")
    private val ratingMaxStars = 5

    @PreferenceFunction("base_image_url")
    fun putDummyString(value : String) : String {
        return value
    }
}