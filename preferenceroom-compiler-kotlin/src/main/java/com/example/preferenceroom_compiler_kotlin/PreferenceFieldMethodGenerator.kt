package com.example.preferenceroom_compiler_kotlin

import androidx.annotation.Nullable
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.ArrayList

class PreferenceFieldMethodGenerator(
        val keyField : PreferenceKeyField,
        val annotatedEntityClazz : PreferenceEntityAnnotatedClass,
        val preference : String) {

  private val SETTER_PREFIX = "put"
  private val GETTER_PREFIX = "get"
  private val KEYNAME_POSTFIX = "KeyName"
  private val JAVA_SYNC_POSTFIX = "SyncJava"
  private val JAVA_ASYNC_POSTFIX = "AsyncJava"
  private val HAS_PREFIX = "contains"
  private val REMOVE_PREFIX = "remove"
    private val preferenceResultContract = ClassName("com.example.preferenceroom_kotlin", "PreferenceResultContract")


    private val runBlocking = ClassName("kotlinx.coroutines", "runBlocking")
    private val coroutinescope = ClassName("kotlinx.coroutines", "CoroutineScope")
    private val dispatcher = ClassName("kotlinx.coroutines", "Dispatchers")
    private val launch = ClassName("kotlinx.coroutines", "launch")
  private val EDIT_METHOD = "edit()"
  private val APPLY_METHOD = "apply()"

   fun getFieldMethods():List<FunSpec> {
       val methodSpecs = ArrayList<FunSpec>()

       if (!keyField.isConverterField) {
           methodSpecs.add(generateGetter())
           methodSpecs.add(generateSetter())
       } else {
           methodSpecs.add(generateObjectGetter())
           methodSpecs.add(generateObjectSetter())
       }
//       methodSpecs.add(generateJavaSyncGetter())
//       methodSpecs.add(generateJavaAsyncGetter())

       if (keyField.isReactiveField) {
           methodSpecs.add(getLiveDataMethodSpec(PreferenceEntityGenerator.FIELD_PREFERENCE))
       }

       methodSpecs.add(generateContainsSpec())
       methodSpecs.add(generateRemoveSpec())
       return methodSpecs
   }

    private fun generateGetter(): FunSpec {
        val builder = FunSpec.builder(getSuspendGetterPrefixName())
                .addAnnotation(Nullable::class)
        builder.addStatement(
                "return " + getGetterStatement(), preference, keyField.keyName, keyField.value)
        builder.returns(keyField.typeName.copy(nullable = true))
        return builder.build()
    }

    private fun generateSetter(): FunSpec {
        val builder = FunSpec.builder(getSetterPrefixName())
                .addParameter(StringUtils.toLowerCamelCase(keyField.keyName), keyField.typeName)
        builder.addStatement(
                getSetterStatement(),
                preference,
                EDIT_METHOD,
                keyField.keyName,
                StringUtils.toLowerCamelCase(keyField.keyName),
                APPLY_METHOD)
        return builder.build()
    }

    private fun generateObjectGetter(): FunSpec {
        val converterClazz = ClassName(keyField.converterPackage, keyField.converter)
        var typeName = keyField.typeName.toString()
        if (typeName.contains("<")) typeName = typeName.substring(0, typeName.indexOf("<"))
        val builder = FunSpec.builder(getSuspendGetterPrefixName())
                .addAnnotation(Nullable::class)
                .addModifiers(KModifier.SUSPEND)
        builder.addStatement(
                "return " + getObjectGetterStatement(),
                keyField.converterInstanceName,
                preference,
                keyField.keyName,
                keyField.value,
                keyField.keyName,
                typeName)
        builder.returns(keyField.typeName.copy(nullable = true))
        return builder.build()
    }

    private fun generateObjectSetter(): FunSpec {
        val converterClazz = ClassName(keyField.converterPackage, keyField.converter)
        var typeName = keyField.typeName.toString()
        if (typeName.contains("<")) typeName = typeName.substring(0, typeName.indexOf("<"))
        val builder = FunSpec.builder(getSetterPrefixName())
                .addParameter(StringUtils.toLowerCamelCase(keyField.keyName), keyField.typeName)
        builder.addStatement(
                getSetterStatement(),
                preference,
                EDIT_METHOD,
                keyField.keyName,
                keyField.converterInstanceName + ".convertObjectToString(" + StringUtils.toLowerCamelCase(keyField.keyName) + ", \"" + keyField.keyName + "\")",
                APPLY_METHOD)
        return builder.build()
    }

    private fun generateJavaSyncGetter(): FunSpec {
        return FunSpec.builder(getSuspendGetterPrefixName())
                .addStatement("return ${getJavaSyncGetterPrefixName()}()")
                .returns(keyField.typeName.copy(nullable = true))
                .build()
    }

    private fun generateJavaAsyncGetter(): FunSpec {
        return FunSpec.builder(getJavaAsyncGetterPrefixName())
                .addParameter("callback", preferenceResultContract.parameterizedBy(keyField.typeName))
                .addStatement("%T(%T.IO).%T{ callback.onKeyAvailable(%N()) }", coroutinescope, dispatcher, launch, getJavaSyncGetterPrefixName())
                .build()
    }

    private fun generateContainsSpec(): FunSpec {
        return FunSpec.builder(getContainsPrefixName())
                .addStatement("return %N.contains(%S)", preference, keyField.keyName)
                .returns(Boolean::class)
                .build()
    }

    private fun generateRemoveSpec(): FunSpec {
        return FunSpec.builder(getRemovePrefixName())
                .addStatement(
                        "%N.%N.remove(%S).%N", preference, EDIT_METHOD, keyField.keyName, APPLY_METHOD)
                .build()
    }

    private fun getLiveDataMethodSpec(preferenceFieldName: String): FunSpec {
        val spLiveDataClass = ClassName(PreferenceEntityGenerator.PACKAGE_SHARED_PREFERENCE_LIVE_DATA,
                PreferenceEntityGenerator.SHARED_PREFERENCE_LIVE_DATA_CLASS_NAME)
        return FunSpec.builder(getLiveDataMethodName())

                .addStatement("return if(this::${getLiveDataFieldName()}.isInitialized) ${getLiveDataFieldName()} " +
                        "else ${PreferenceEntityGenerator.SHARED_PREFERENCE_LIVE_DATA_CLASS_NAME}(%N, %N(), this::%N).also{${getLiveDataFieldName()} = it}"
                        ,preferenceFieldName, getKeyNamePostfixName(), getSuspendGetterPrefixName())
                .returns(spLiveDataClass.parameterizedBy(keyField.typeName.copy(true)))
                .build()
    }

    private fun getLiveDataMethodName(): String {
        return GETTER_PREFIX + StringUtils.toUpperCamel(this.keyField.keyName) + PreferenceEntityGenerator.LIVE_DATA_POSTFIX
    }

    private fun getLiveDataFieldName(): String {
        return StringUtils.toLowerCamelCase(keyField.keyName) + PreferenceEntityGenerator.LIVE_DATA_POSTFIX
    }


    private fun getJavaSyncGetterPrefixName(): String {
        return GETTER_PREFIX + StringUtils.toUpperCamel(this.keyField.keyName) + JAVA_SYNC_POSTFIX
    }

    private fun getJavaAsyncGetterPrefixName(): String {
        return GETTER_PREFIX + StringUtils.toUpperCamel(this.keyField.keyName) + JAVA_ASYNC_POSTFIX
    }

    private fun getSuspendGetterPrefixName(): String {
        return GETTER_PREFIX + StringUtils.toUpperCamel(this.keyField.keyName)
    }

    private fun getSetterPrefixName(): String {
        return SETTER_PREFIX + StringUtils.toUpperCamel(this.keyField.keyName)
    }

    private fun getKeyNamePostfixName(): String {
        return GETTER_PREFIX + StringUtils.toUpperCamel(this.keyField.keyName) + KEYNAME_POSTFIX
    }

    private fun getContainsPrefixName(): String {
        return HAS_PREFIX + StringUtils.toUpperCamel(this.keyField.keyName)
    }

    private fun getRemovePrefixName(): String {
        return REMOVE_PREFIX + StringUtils.toUpperCamel(this.keyField.keyName)
    }

    private fun getGetterTypeMethodName(): String {
        return GETTER_PREFIX + StringUtils.toUpperCamel(this.keyField.typeStringName)
    }

    private fun getSetterTypeMethodName(): String {
        return SETTER_PREFIX + StringUtils.toUpperCamel(this.keyField.typeStringName)
    }

    private fun getGetterStatement(): String {
        if (annotatedEntityClazz.getterFunctionsList.containsKey(keyField.keyName)) {
            val superMethodName = annotatedEntityClazz.getterFunctionsList[keyField.keyName]?.simpleName.toString()
            return if (keyField.value is String)
                "super.$superMethodName(%N.getString(%S, %S))"
            else if (keyField.value is Float)
                "super.$superMethodName(%N.${getGetterTypeMethodName()}(%S, %Lf))"
            else
                "super.$superMethodName(%N..${getGetterTypeMethodName()}(%S, %L))"
        } else {
            return if (keyField.value is String)
                "%N.getString(%S, %S)"
            else if (keyField.value is Float)
                "%N.${getGetterTypeMethodName()}(%S, %Lf)"
            else
                "%N.${getGetterTypeMethodName()}(%S, %L)"
        }
    }

    private fun getObjectGetterStatement(): String {
        if (annotatedEntityClazz.getterFunctionsList.containsKey(keyField.keyName)) {
            val superMethodName = annotatedEntityClazz.getterFunctionsList[keyField.keyName]?.simpleName.toString()
            return if (keyField.value is String)
                "super.$superMethodName%N.convertStringToObject(%N.getString(%S, %S), %S, %N::class.java))"
            else if (keyField.value is Float)
                "super.$superMethodName%N.convertStringToObject(%N.${getGetterTypeMethodName()}(%S, %Lf), %S, %N::class.java))"
            else
                "super.$superMethodName%N.convertStringToObject(%N.${getGetterTypeMethodName()}(%S, %L), %S, %N::class.java)"
        } else {
            return if (keyField.value is String)
                "%N.convertStringToObject(%N.getString(%S, %S), %S, %N::class.java)"
            else if (keyField.value is Float)
                "%N.convertStringToObject(%N.${getGetterTypeMethodName()}(%S, %Lf), %S, %N::class.java)"
            else
                "%N.convertStringToObject(%N.${getGetterTypeMethodName()}(%S, %L), %S, %N::class.java)"
        }
    }

    private fun getSetterStatement(): String {
        return if (annotatedEntityClazz.setterFunctionsList.containsKey(keyField.keyName)) {
            "%N.%N." + getSetterTypeMethodName() + "(%S, super.${annotatedEntityClazz.setterFunctionsList[keyField.keyName]?.simpleName}(%N)).%N"
        } else
            "%N.%N.${getSetterTypeMethodName()}(%S, %N).%N"
    }

    private fun wrapperClassFormatting(statement: String): String {
        if (keyField.value is Boolean) {
            return String.format("Boolean.valueOf(%s).booleanValue()", statement)
        } else if (keyField.value is Int) {
            return String.format("Integer.valueOf(%s).intValue()", statement)
        } else if (keyField.value is Float) {
            return String.format("Float.valueOf(%s).floatValue()", statement)
        }
        return statement
    }

}