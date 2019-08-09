package com.example.preferenceroom_compiler_kotlin

import com.example.preferenceroom_kotlin.annotation.KeyName
import com.example.preferenceroom_kotlin.annotation.TypeConverter
import com.google.common.base.Strings
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

class PreferenceKeyField(variableElement: VariableElement, elementUtils: Elements) {
    val stringTypeName = ClassName("kotlin", "String")
    val variableElement: VariableElement
    val packageName: String?
    val type: TypeMirror
    var typeName: TypeName
    val clazzName: String
    lateinit var typeStringName: String
    var keyName: String
    var value: Any

    lateinit var converter: String
    lateinit var converterPackage: String
    lateinit var converterInstanceName: String
    var isConverterField = false
    var isReactiveField: Boolean = false

    init {
        val annotation_keyName = variableElement.getAnnotation(KeyName::class.java)
        this.variableElement = variableElement
        val packageElement = elementUtils.getPackageOf(variableElement)
        this.packageName = if (packageElement.isUnnamed) null else packageElement.qualifiedName.toString()
        this.typeName = variableElement.asType().asTypeName()
        this.clazzName = variableElement.simpleName.toString()
        this.value = variableElement.constantValue
        this.type = variableElement.asType()
        setTypeStringName()
        isReactiveField = annotation_keyName.reactive

        if (annotation_keyName != null)
            this.keyName = if (Strings.isNullOrEmpty(annotation_keyName.value))
                this.clazzName
            else
                annotation_keyName.value
        else
            this.keyName = this.clazzName

        if (this.isConverterField) {
            variableElement
                    .annotationMirrors
                    .stream()
                    .filter { annotationMirror -> annotationMirror.annotationType.asTypeName() == TypeConverter::class.asTypeName() }
                    .forEach { annotationMirror ->
                        annotationMirror
                                .elementValues
                                .forEach { (_, value) ->
                                    val split = value.value.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                    val builder = StringBuilder()
                                    for (i in 0 until split.size - 1)
                                        builder.append(split[i] + ".")
                                    this.converterPackage = builder.toString().substring(0, builder.toString().length - 1)
                                    this.converter = split[split.size - 1]
                                    this.converterInstanceName = StringUtils.toLowerCamelCase(this.converter)
                                }
                    }
        }

//        if (variableElement.modifiers.contains(Modifier.PRIVATE)) {
//            throw IllegalAccessException(
//                    String.format("Field \'%s\' should not be private.", variableElement.simpleName))
//        } else if (!this.isConverterField && !variableElement.modifiers.contains(Modifier.FINAL)) {
//            throw IllegalAccessException(
//                    String.format("Field \'%s\' should be final.", variableElement.simpleName))
//        }
    }

    @Throws(IllegalAccessException::class)
    private fun setTypeStringName() {
        if (this.typeName == Boolean::class.asTypeName())
            this.typeStringName = "Boolean"
        else if (this.typeName == Int::class.asTypeName())
            this.typeStringName = "Int"
        else if (this.typeName == Float::class.asTypeName())
            this.typeStringName = "Float"
        else if (this.typeName == Long::class.asTypeName())
            this.typeStringName = "Long"
        else if (isStringType(this.typeName) && variableElement.getAnnotation(TypeConverter::class.java) != null) {
            this.typeStringName = "String"
            this.isConverterField = true
            this.typeName = stringTypeName
        } else if (isStringType(this.typeName)) {
            this.typeStringName = "String"
            this.typeName = stringTypeName
        } else if (variableElement.getAnnotation(TypeConverter::class.java) == null)
            throw IllegalAccessException(
                    String.format(
                            "Field \'%s\' can not use %s type. \nObjects should be annotated with '@TypeConverter'.",
                            variableElement.simpleName, this.typeName.toString()))
        else {
            this.typeStringName = "String"
            this.isConverterField = true
            this.typeName = stringTypeName
        }
    }

    private fun isStringType(type: TypeName): Boolean {
        return type.toString().contains("String")
    }
}