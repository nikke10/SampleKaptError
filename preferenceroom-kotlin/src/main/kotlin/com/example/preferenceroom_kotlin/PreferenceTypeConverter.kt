package com.example.preferenceroom_kotlin

public interface PreferenceTypeConverter<T> {
    /**
     * converts an object to string value for saving.
     * Called before calling actual SharedPreferences put method
     *
     * @param obj an object for saving.
     * @param keyName the name of the key with which it will be saved
     * @return converted string value.
     */
    abstract fun convertObjectToString(obj: T, keyName: String): String?

    /**
     * converts a saved string value and recovers the original object.
     * Called after calling actual SharedPreferences get method
     *
     * @param value saved string value.
     * @param keyName the name of the key with which it was saved
     * @return recovered original object.
     */
    abstract fun convertStringToObject(value: String?, keyName: String, clazz: Class<T>): T?
}