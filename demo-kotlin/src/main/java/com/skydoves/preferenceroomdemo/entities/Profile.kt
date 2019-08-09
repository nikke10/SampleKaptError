package com.skydoves.preferenceroomdemo.entities

import com.skydoves.preferenceroomdemo.converters.PetConverter
import com.skydoves.preferenceroomdemo.converters.PrivateInfoConverter
import com.skydoves.preferenceroomdemo.models.Pet
import com.skydoves.preferenceroomdemo.models.PrivateInfo

//@EncryptEntity("1234567890ABCDFG")
//@PreferenceEntity("UserProfile")
open class Profile {
//    @KeyName("nickname")
//    @JvmField val userNickName = "skydoves"
//
//    /**
//     * key value will be 'Login'. (login's camel uppercase)
//     */
//    @JvmField val login = false
//
//    @KeyName("visits")
//    @JvmField val visitCount = 1
//
//    @KeyName("userinfo")
//    @TypeConverter(PrivateInfoConverter::class)
//    @JvmField val privateInfo: PrivateInfo? = null
//
//    /**
//     * value used with gson.
//     */
//    @KeyName("userPet")
//    @TypeConverter(PetConverter::class)
//    @JvmField val userPetInfo: Pet? = null
//
//    /**
//     * preference putter function about userNickName.
//     * @param nickname function in
//     * @return function out
//     */
//    @PreferenceFunction("nickname")
//    open fun putUserNickFunction(nickname: String): String {
//        return "Hello, $nickname"
//    }
//
//    /**
//     * preference getter function about userNickName.
//     * @param nickname function in
//     * @return function out
//     */
//    @PreferenceFunction("nickname")
//    open fun getUserNickFunction(nickname: String): String {
//        return "$nickname !!!"
//    }
//
//    /**
//     * preference putter function example about visitCount's auto increment.
//     * @param count function in
//     * @return function out
//     */
//    @PreferenceFunction("visits")
//    open fun putVisitCountFunction(count: Int): Int {
//        var count = count
//        return ++count
//    }
}
