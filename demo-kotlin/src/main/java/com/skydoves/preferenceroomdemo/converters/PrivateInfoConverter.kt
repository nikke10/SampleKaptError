package com.skydoves.preferenceroomdemo.converters

import com.skydoves.preferenceroomdemo.models.PrivateInfo

class PrivateInfoConverter  {
//    : PreferenceTypeConverter<PrivateInfo>() {
//
//    override fun convertObject(privateInfo: PrivateInfo, keyName : String): String {
//        return privateInfo.name + "," + privateInfo.age
//    }
//
//    override fun convertType(string: String?, keyName : String, clazz : Class<PrivateInfo>): PrivateInfo {
//        if (string == null) return PrivateInfo("null", 0)
//        val information = string.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        return PrivateInfo(information[0], Integer.parseInt(information[1]))
//    }
}
