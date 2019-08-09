package com.example.preferenceroom_compiler_kotlin

import androidx.annotation.Nullable
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.lang.reflect.Type
import java.util.ArrayList
import kotlin.reflect.KClass

class DynamicFieldMethodGenerator(preference: String) {
    private val sharedPreference: String = preference
    private val HAS_PREFIX = "containsKey"
    private val REMOVE_PREFIX = "removeKey"
    private val KEY = "key"
    private val VALUE = "value"
    private val DEFAULT_VALUE = "defaultValue"
    private val EDIT_METHOD = "edit()"
    private val APPLY_METHOD = "apply()"
    private val set = ClassName("kotlin.collections", "Set")

    fun getDynamicFieldMethods(): List<FunSpec> {
        val methodSpecs = ArrayList<FunSpec>(15)
        methodSpecs.addAll(generateGetters())
        methodSpecs.addAll(generateSetters())
        methodSpecs.add(generateContainsSpec())
        methodSpecs.add(generateRemoveSpec())
        return methodSpecs
    }

    /**
     * Generates the list of all getters for each key type
     * @return
     */
    private fun generateGetters(): List<FunSpec> {
        val methodSpecs = ArrayList<FunSpec>(6)
        methodSpecs.add(generateGetter("getStringByKey", String::class.asTypeName(), "getString"))
        methodSpecs.add(generateGetter("getIntegerByKey", Int::class.asTypeName(), "getInt"))
        methodSpecs.add(generateGetter("getFloatByKey", Float::class.asTypeName(), "getFloat"))
        methodSpecs.add(generateGetter("getBooleanByKey", Boolean::class.asTypeName(), "getBoolean"))
        methodSpecs.add(generateGetter("getLongByKey", Long::class.asTypeName(), "getLong"))

        val builder = FunSpec.builder("getStringSetByKey")
                .addModifiers(KModifier.PUBLIC)
                .addParameter(KEY, String::class)
                .addParameter(DEFAULT_VALUE, getSet())
                .addAnnotation(Nullable::class)
        builder.addStatement(
                "return %N.%N(%N, %N)", sharedPreference, "getStringSet", KEY, DEFAULT_VALUE)
        builder.returns(getSet().copy(nullable = true))
        methodSpecs.add(builder.build())

        return methodSpecs
    }

    private fun getSet(): ParameterizedTypeName {
        return set.parameterizedBy(String::class.asTypeName())
    }

    private fun generateGetter(getterName: String, type: TypeName, getStatement: String): FunSpec {
        val builder = FunSpec.builder(getterName)
                .addModifiers(KModifier.PUBLIC)
                .addParameter(KEY, String::class)
                .addParameter(DEFAULT_VALUE, type)
                .addAnnotation(Nullable::class)
        builder.addStatement(
                "return %N.%N(%N, %N)", sharedPreference, getStatement, KEY, DEFAULT_VALUE)
        builder.returns(type.copy(nullable = true))
        return builder.build()
    }

    /**
     * Generates the list of all getters for each key type
     * @return
     */
    private fun generateSetters(): List<FunSpec> {
        val methodSpecs = ArrayList<FunSpec>(6)
        methodSpecs.add(generateSetter("putStringByKey", String::class.asTypeName(), "putString"))
        methodSpecs.add(generateSetter("putIntegerByKey", Int::class.asTypeName(), "putInt"))
        methodSpecs.add(generateSetter("putFloatByKey", Float::class.asTypeName(), "putFloat"))
        methodSpecs.add(generateSetter("putBooleanByKey", Boolean::class.asTypeName(), "putBoolean"))
        methodSpecs.add(generateSetter("putLongByKey", Long::class.asTypeName(), "putLong"))

        val builder = FunSpec.builder("putStringSetByKey")
                .addModifiers(KModifier.PUBLIC)
                .addParameter(KEY, String::class)
                .addParameter(VALUE, getSet())
        builder.addStatement(
                "%N.%N.%N(%N, %N).%N", sharedPreference,
                EDIT_METHOD,
                "putStringSet",
                KEY,
                VALUE,
                APPLY_METHOD)
        methodSpecs.add(builder.build())
        return methodSpecs
    }

    private fun generateSetter(setterName: String, type: TypeName, putStatement: String): FunSpec {
        val builder = FunSpec.builder(setterName)
                .addModifiers(KModifier.PUBLIC)
                .addParameter(KEY, String::class)
                .addParameter(VALUE, type)
        builder.addStatement(
                "%N.%N.%N(%N, %N).%N", sharedPreference,
                EDIT_METHOD,
                putStatement,
                KEY,
                VALUE,
                APPLY_METHOD)
        return builder.build()
    }

    /**
     * Generates the contains method specification for a provided key name
     * @return
     */
    private fun generateContainsSpec(): FunSpec {
        return FunSpec.builder(HAS_PREFIX)
                .addModifiers(KModifier.PUBLIC)
                .addParameter(KEY, String::class)
                .addStatement("return %N.contains(%N)", sharedPreference, KEY)
                .returns(Boolean::class)
                .build()
    }

    /**
     * Generates the remove method specification for a provided key name
     * @return
     */
    private fun generateRemoveSpec(): FunSpec {
        return FunSpec.builder(REMOVE_PREFIX)
                .addModifiers(KModifier.PUBLIC)
                .addParameter(KEY, String::class)
                .addStatement(
                        "%N.%N.remove(%N).%N", sharedPreference, EDIT_METHOD, KEY, APPLY_METHOD)
                .build()
    }
}