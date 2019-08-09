package com.example.preferenceroom_compiler_kotlin

import com.example.preferenceroom_kotlin.annotation.KeyName
import com.example.preferenceroom_kotlin.annotation.PreferenceEntity
import com.example.preferenceroom_kotlin.annotation.PreferenceFunction
import com.google.common.base.Strings
import com.google.common.base.VerifyException
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import java.util.ArrayList
import java.util.HashMap
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

class PreferenceEntityAnnotatedClass(annotatedElement: TypeElement, elementUtils: Elements) {
    val packageName: String
    val annotatedElement: TypeElement
    val typeName: TypeName
    val clazzName: String
    val entityName: String
    val keyFields: MutableList<PreferenceKeyField>
    var isDynamicKeysRequired = false

    val keyNameFields: MutableList<String>
    val keyFieldMap: MutableMap<String, PreferenceKeyField>
    val setterFunctionsList: MutableMap<String, Element> = HashMap()
    val getterFunctionsList: MutableMap<String, Element> = HashMap()

    private val SETTER_PREFIX = "put"
    private val GETTER_PREFIX = "get"
    private val HAS_PREFIX = "contains"
    private val REMOVE_PREFIX = "remove"

    init {
        val preferenceEntity = annotatedElement.getAnnotation(PreferenceEntity::class.java)
        val packageElement = elementUtils.getPackageOf(annotatedElement)
        this.packageName = packageElement.qualifiedName.toString()
        this.annotatedElement = annotatedElement
        this.typeName = annotatedElement.asType().asTypeName()
        this.clazzName = annotatedElement.simpleName.toString()
        this.entityName = if (Strings.isNullOrEmpty(preferenceEntity.value))
            StringUtils.toUpperCamel(this.clazzName)
        else
            preferenceEntity.value
        this.keyFields = ArrayList()
        this.keyNameFields = ArrayList()
        this.keyFieldMap = HashMap()

        if (preferenceEntity.hasDynamicKeys) {
            isDynamicKeysRequired = true
        }

        val checkMap = HashMap<String, String>()

        annotatedElement
                .enclosedElements
                .stream()
                .filter { element ->
                    (element.kind.isField
                            && element.getAnnotation(KeyName::class.java) != null)
                }
                .map { element -> element as VariableElement }
                .forEach { variable ->
                    try {
                        val keyField = PreferenceKeyField(variable, elementUtils)

                        if (checkMap[keyField.keyName] != null) {
                            throw VerifyException(
                                    String.format("\'%s\' key is already used in class.", keyField.keyName))
                        }

                        checkMap[keyField.keyName] = keyField.clazzName
                        keyFields.add(keyField)
                        keyNameFields.add(keyField.keyName)
                        keyFieldMap[keyField.keyName] = keyField
                    } catch (e: IllegalAccessException) {
                        throw VerifyException(e.message)
                    }
                }

        checkOverrideMethods()

        annotatedElement
                .enclosedElements
                .stream()
                .filter { function ->
                    (!function.kind.isField
                            && function.modifiers.contains(Modifier.PUBLIC)
                            && function.getAnnotation(PreferenceFunction::class.java) != null)
                }
                .forEach { function ->
                    val annotation = function.getAnnotation(PreferenceFunction::class.java)
                    val keyName = annotation.value
                    if (keyNameFields.contains(keyName)) {
                        if (function.simpleName.toString().startsWith(SETTER_PREFIX)) {
                            setterFunctionsList[keyName] = function
                        } else if (function.simpleName.toString().startsWith(GETTER_PREFIX)) {
                            getterFunctionsList[keyName] = function
                        } else {
                            throw VerifyException(
                                    String.format(
                                            "PreferenceFunction's prefix should startWith 'get' or 'put' : %s",
                                            function.simpleName))
                        }
                    } else {
                        throw VerifyException(
                                String.format("keyName '%s' is not exist in entity.", keyName))
                    }

                    val methodSpec = function as ExecutableElement
                    if (methodSpec.parameters.size > 1 || methodSpec.parameters.size == 0) {
                        throw VerifyException("PreferenceFunction should has one parameter")
                    } else if (methodSpec
                                    .parameters[0]
                                    .asType() != keyFieldMap[keyName]!!.type) {
                        throw VerifyException(
                                String.format(
                                        "parameter '%s''s type should be %s.",
                                        methodSpec.parameters[0].simpleName, keyFieldMap[keyName]!!.type))
                    } else if (methodSpec.returnType != keyFieldMap[keyName]!!.type) {
                        throw VerifyException(
                                String.format(
                                        "method '%s''s return type should be %s.",
                                        methodSpec.simpleName, keyFieldMap[keyName]!!.type))
                    }
                }
    }

    private fun checkOverrideMethods() {
        annotatedElement
                .enclosedElements
                .stream()
                .filter { element -> element is ExecutableElement }
                .map { element -> element as ExecutableElement }
                .forEach { method ->
                    if (keyNameFields.contains(
                                    method.simpleName.toString().replace(SETTER_PREFIX, "")))
                        throw VerifyException(
                                getMethodNameVerifyErrorMessage(method.simpleName.toString()))
                    else if (keyNameFields.contains(
                                    method.simpleName.toString().replace(GETTER_PREFIX, "")))
                        throw VerifyException(
                                getMethodNameVerifyErrorMessage(method.simpleName.toString()))
                    else if (keyNameFields.contains(
                                    method.simpleName.toString().replace(HAS_PREFIX, "")))
                        throw VerifyException(
                                getMethodNameVerifyErrorMessage(method.simpleName.toString()))
                    else if (keyNameFields.contains(
                                    method.simpleName.toString().replace(REMOVE_PREFIX, "")))
                        throw VerifyException(
                                getMethodNameVerifyErrorMessage(method.simpleName.toString()))
                }
    }

    private fun getMethodNameVerifyErrorMessage(methodName: String): String {
        return String.format("can not use method value '%s'. Use an another one.", methodName)
    }
}