package com.example.preferenceroom_compiler_kotlin

import androidx.annotation.NonNull
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.ArrayList
import java.util.HashMap
import javax.inject.Inject
import javax.lang.model.util.Elements

class PreferenceEntityGenerator(annotatedClass: PreferenceEntityAnnotatedClass, elementUtils: Elements) {
    private var annotatedClazz: PreferenceEntityAnnotatedClass = annotatedClass
    private var annotatedElementUtils: Elements = elementUtils

    companion object {
        const val FIELD_PREFERENCE = "preference"
        const val GET_PREFERENCE = "getPreference"
        const val METHOD_GET_PREFERENCE = "$GET_PREFERENCE()"
        const val PACKAGE_SHARED_PREFERENCE_LIVE_DATA = "com.skydoves.preferenceroomdemo.preference"
        const val SHARED_PREFERENCE_LIVE_DATA_CLASS_NAME = "SharedPreferenceLiveData"
        const val LIVE_DATA_POSTFIX = "LiveData"
    }

    private val CLAZZ_PREFIX = "Preference_"
    private val KEYNAME_POSTFIX = "KeyName"
    private val FIELD_INSTANCE = "instance"
    private val FIELD_LOCK = "lock"
    private val CONSTRUCTOR_CONTEXT = "context"
    private val KEY_NAME_LIST = "keyNameList"

    private val EDIT_METHOD = "edit()"
    private val CLEAR_METHOD = "clear()"
    private val APPLY_METHOD = "apply()"
    private val GETTER_PREFIX = "get"
    private val list = ClassName("kotlin.collections", "List")
    private val any = ClassName("kotlin", "Any")
    private val arrayList = ClassName("kotlin.collections", "ArrayList")

    private val PACKAGE_CONTEXT = "android.content.Context"
    //TODO : Should find a better way of doing it.
    private val PACKAGE_SHAREDPREFERENCE = "android.content.SharedPreferences"

    fun generate(): TypeSpec {
        val builder = TypeSpec.classBuilder(getClazzName())
                //TODO
//                .addAnnotation(Singleton::class.java!!)
                .superclass(annotatedClazz.annotatedElement.asClassName())
                .addProperties(getFieldSpecs())
                .addProperties(getLiveDataFieldSpecs())

        builder.primaryConstructor(getConstructorSpec())
        builder.addType(getCompanionSpec())

        builder.addFunctions(getFieldMethodSpecs())
                .addFunction(getClearMethodSpec())
                .addFunction(getPreferenceSpec())
                .addFunction(getLoadPreferenceSpec())

        return builder.build()
    }

    private fun getFieldSpecs(): List<PropertySpec> {
        val fieldSpecs = ArrayList<PropertySpec>()

        fieldSpecs.add(
                PropertySpec.builder(
                        FIELD_PREFERENCE,
                        getSharedPreferencesPackageType(),
                        KModifier.PRIVATE,
                        KModifier.LATEINIT).mutable(true).build())

        fieldSpecs.add(PropertySpec.builder(CONSTRUCTOR_CONTEXT, getContextPackageType(),KModifier.PUBLIC, KModifier.LATEINIT)
                .mutable(true)
                .build())

        val checkList = HashMap<String, String>()
        annotatedClazz.keyFields.forEach { keyField ->
            if (keyField.isConverterField && !checkList.containsKey(keyField.converter)) {
                checkList[keyField.converter] = keyField.converter
                val converterClazz = ClassName(keyField.converterPackage, keyField.converter)
                fieldSpecs.add(PropertySpec.builder(keyField.converterInstanceName, converterClazz, KModifier.PRIVATE, KModifier.LATEINIT)
                        .mutable(true)
                        //TODO
//                        .addAnnotation(Inject::class.java!!)
                        .build())
            }
        }
        return fieldSpecs
    }

    private fun getConstructorSpec(): FunSpec {
        return FunSpec.constructorBuilder()
                .addAnnotation(Inject::class.java)
                .build()
    }

//    private fun getInstanceSpec(): FunSpec {
//        return FunSpec.builder("getInstance")
//                .addModifiers(PUBLIC, STATIC)
//                .addParameter(
//                        ParameterSpec.builder(getContextPackageType(), CONSTRUCTOR_CONTEXT)
//                                .addAnnotation(NonNull::class.java)
//                                .build())
//                .addStatement("if(\$N != null) return \$N", FIELD_INSTANCE, FIELD_INSTANCE)
//                .addCode("synchronized(\$N) {\n", FIELD_LOCK)
//                .addCode("if(\$N == null) {\n", FIELD_INSTANCE)
//                .addStatement("\$N = new \$N(\$N)", FIELD_INSTANCE, getClazzName(), CONSTRUCTOR_CONTEXT)
//                .addCode("}\n")
//                .addStatement("return \$N", FIELD_INSTANCE)
//                .addCode("}\n")
//                .returns(getClassType())
//                .build()
//    }

    private fun getCompanionSpec() : TypeSpec {
        return TypeSpec.companionObjectBuilder()
                .addFunction(getPreferenceNameMethodSpec())
                .addFunctions(getKeyNamesSpecs())
                .build()
    }

    private fun getFieldMethodSpecs(): List<FunSpec> {
        val methodSpecs = ArrayList<FunSpec>()
        this.annotatedClazz.keyFields.forEach { annotatedFields ->
            val methodGenerator = PreferenceFieldMethodGenerator(annotatedFields, annotatedClazz, METHOD_GET_PREFERENCE)
            methodSpecs.addAll(methodGenerator.getFieldMethods())
        }
        if (annotatedClazz.isDynamicKeysRequired) {
            methodSpecs.addAll(DynamicFieldMethodGenerator(METHOD_GET_PREFERENCE).getDynamicFieldMethods())
        }
        return methodSpecs
    }

    private fun getLiveDataFieldSpecs(): List<PropertySpec> {
        val fieldSpecs = ArrayList<PropertySpec>()
        this.annotatedClazz.keyFields
                .stream()
                .filter { preferenceKeyField -> preferenceKeyField.isReactiveField }
                .forEach { annotatedField -> fieldSpecs.add(generateLiveDataField(annotatedField)) }
        return fieldSpecs
    }

    private fun generateLiveDataField(keyField: PreferenceKeyField): PropertySpec {
        val spLiveDataClass = ClassName(PACKAGE_SHARED_PREFERENCE_LIVE_DATA, SHARED_PREFERENCE_LIVE_DATA_CLASS_NAME)
        return PropertySpec.builder(
                getLiveDataFieldName(keyField),
                spLiveDataClass.parameterizedBy(keyField.typeName.copy(true)),
                KModifier.PRIVATE, KModifier.LATEINIT)
                .mutable(true)
                .build()
    }

    private fun getClearMethodSpec(): FunSpec {
        return FunSpec.builder("clear")
                .addStatement("%N.%N.%N.%N", METHOD_GET_PREFERENCE, EDIT_METHOD, CLEAR_METHOD, APPLY_METHOD)
                .build()
    }

    private fun getPreferenceSpec() : FunSpec {
        return FunSpec.builder(GET_PREFERENCE)
                .addModifiers(KModifier.PRIVATE)
                .addStatement("return if(this::%N.isInitialized) %N \nelse %N.getSharedPreferences(%S, Context.MODE_PRIVATE).also {%N = it}",
                        FIELD_PREFERENCE,
                        FIELD_PREFERENCE,
                        CONSTRUCTOR_CONTEXT,
                        annotatedClazz.entityName,
                        FIELD_PREFERENCE)
                .returns(getSharedPreferencesPackageType())
                .build()
    }

    private fun getLoadPreferenceSpec() : FunSpec {
        return FunSpec.builder("loadPreference")
                .addStatement("$METHOD_GET_PREFERENCE")
                .addModifiers(KModifier.SUSPEND)
                .build()
    }

    private fun getKeyNamesSpecs(): List<FunSpec> {
        val keyNamesFunSpecs = ArrayList<FunSpec>()
        this.annotatedClazz.keyFields
                .stream()
                .forEach { annotatedField -> keyNamesFunSpecs.add(generateObjectKeyNameSpec(annotatedField)) }
        return keyNamesFunSpecs
    }

    private fun getKeyNameListMethodSpec(): FunSpec {
        val builder = FunSpec.builder("get$KEY_NAME_LIST")
                .addModifiers(KModifier.PUBLIC)
                .returns(list.parameterizedBy(any))
                .addStatement("List<String> %N = new %T<>()", KEY_NAME_LIST, arrayList)

        this.annotatedClazz.keyNameFields.forEach { keyName -> builder.addStatement("%N.add(%S)", KEY_NAME_LIST, keyName) }

        builder.addStatement("return %N", KEY_NAME_LIST)
        return builder.build()
    }

    private fun getPreferenceNameMethodSpec(): FunSpec {
        return FunSpec.builder("getPreferenceName")
                .addModifiers(KModifier.PUBLIC)
                .returns(String::class)
                .addStatement("return %S", annotatedClazz.entityName)
                .build()
    }

    private fun generateObjectKeyNameSpec(keyField: PreferenceKeyField): FunSpec {
        return FunSpec.builder(getKeyNamePostfixName(keyField))
                .addModifiers(KModifier.PUBLIC)
                .returns(String::class)
                .addStatement("return %S", keyField.keyName)
                .build()
    }

    private fun getKeyNamePostfixName(keyField: PreferenceKeyField): String {
        return GETTER_PREFIX + StringUtils.toUpperCamel(keyField.keyName) + KEYNAME_POSTFIX
    }

    private fun getLiveDataFieldName(keyField: PreferenceKeyField): String {
        return StringUtils.toLowerCamelCase(keyField.keyName) + LIVE_DATA_POSTFIX
    }

    private fun getClassType(): ClassName {
        return ClassName(annotatedClazz.packageName, getClazzName())
    }

    private fun getClazzName(): String {
        return CLAZZ_PREFIX + StringUtils.toUpperCamel(annotatedClazz.entityName)
    }

    private fun getContextPackageType(): TypeName {
        return annotatedElementUtils.getTypeElement(PACKAGE_CONTEXT).asClassName()
    }

    private fun getSharedPreferencesPackageType(): TypeName {
        return annotatedElementUtils.getTypeElement(PACKAGE_SHAREDPREFERENCE).asClassName()
    }

}