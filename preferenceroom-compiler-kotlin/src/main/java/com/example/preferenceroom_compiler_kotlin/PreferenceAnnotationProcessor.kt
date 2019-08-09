package com.example.preferenceroom_compiler_kotlin

import com.example.preferenceroom_kotlin.annotation.KeyName
import com.example.preferenceroom_kotlin.annotation.PreferenceEntity
import com.google.auto.service.AutoService
import com.google.common.base.VerifyException
import com.squareup.kotlinpoet.*
import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.HashSet
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.reflect.KClass

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("kapt.kotlin.generated")
@AutoService(Processor::class)
class PreferenceAnnotationProcessor : AbstractProcessor(){
    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private lateinit var annotatedEntityNameMap: MutableMap<String, String>
    private lateinit var annotatedEntityMap: MutableMap<String, PreferenceEntityAnnotatedClass>
    private lateinit var messager: Messager

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {

        // Return the supported annotations by this processor
        val supportedAnnotations = HashSet<String>()
        supportedAnnotations.add(PreferenceEntity::class.java.canonicalName)
        supportedAnnotations.add(KeyName::class.java.canonicalName)
        return supportedAnnotations
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(mutableSet: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {

        val preferenceEntitySet = roundEnvironment?.getElementsAnnotatedWith(PreferenceEntity::class.java)
        preferenceEntitySet?.stream()
                ?.map {annotatedType -> annotatedType as TypeElement}
                ?.forEach { annotatedType ->
                    try {
                        checkValidEntityType(annotatedType)
                        processEntity(annotatedType)
                    } catch (e: IllegalAccessException) {
                        showErrorLog(e.localizedMessage, annotatedType)
                    }
                }
        return true
    }

    @Throws(IllegalAccessException::class)
    private fun checkValidEntityType(annotatedType: TypeElement) {
        if (!annotatedType.kind.isClass) {
            throw IllegalAccessException("Only classes can be annotated with @PreferenceEntity")
        } else if (annotatedType.modifiers.contains(Modifier.FINAL)) {
            showErrorLog("class modifier should not be final", annotatedType)
        } else if (annotatedType.modifiers.contains(Modifier.PRIVATE)) {
            showErrorLog("class modifier should not be final", annotatedType)
        }
    }

    @Throws(VerifyException::class)
    private fun processEntity(annotatedType: TypeElement) {
        try {
            val annotatedClazz = PreferenceEntityAnnotatedClass(annotatedType, processingEnv.elementUtils)
            checkDuplicatedPreferenceEntity(annotatedClazz)
            generateProcessEntity(annotatedClazz)
        } catch (e: VerifyException) {
            showErrorLog(e.localizedMessage, annotatedType)
            e.printStackTrace()
        }

    }

    private fun generateProcessEntity(annotatedClass: PreferenceEntityAnnotatedClass) {
        try {
            val annotatedClazz = PreferenceEntityGenerator(annotatedClass, processingEnv.elementUtils)
                    .generate()
            val fileSpec = FileSpec.get(annotatedClass.packageName, annotatedClazz)
            val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
            fileSpec.writeTo(File(kaptKotlinGeneratedDir, ""))
        } catch (e: IOException) {
            // ignore
        }

    }

    @Throws(VerifyException::class)
    private fun checkDuplicatedPreferenceEntity(annotatedClazz: PreferenceEntityAnnotatedClass) {
        initMaps()
        if (annotatedEntityMap.containsKey(annotatedClazz.entityName)) {
            throw VerifyException("@PreferenceEntity name value is duplicated.")
        } else {
            annotatedEntityMap[annotatedClazz.entityName]= annotatedClazz
            annotatedEntityNameMap["${annotatedClazz.typeName} + .class"] = annotatedClazz.entityName
        }
    }

    private fun initMaps() {
        if (!this::annotatedEntityMap.isInitialized) {
            annotatedEntityMap = HashMap()
        }
        if (!this::annotatedEntityNameMap.isInitialized) {
            annotatedEntityNameMap = HashMap()
        }
    }


    private fun showErrorLog(message: String, element: Element) {
        messager.printMessage(Diagnostic.Kind.ERROR, StringUtils.getErrorMessagePrefix() + message, element)
    }
}