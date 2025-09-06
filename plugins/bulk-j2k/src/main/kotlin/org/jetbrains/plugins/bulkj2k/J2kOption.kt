package org.jetbrains.plugins.bulkj2k

import org.jetbrains.kotlin.j2k.ConverterSettings
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

/**
 * Represents a configurable J2K option. Each enum value is mapped to a property in [ConverterSettings].
 * Only options whose backing property exists in the current Kotlin plugin are presented to the user.
 */
enum class J2kOption(
    val propertyName: String,
    val title: String,
    val description: String,
    val defaultValue: Boolean
) {
    BASIC_MODE(
        propertyName = "basicMode",
        title = "Basic conversion mode",
        description = "Use the basic, less idiomatic conversion mode.",
        defaultValue = true
    ),
    CONVERT_PROPERTIES(
        propertyName = "convertGetterSetterToProperty",
        title = "Convert getters/setters to properties",
        description = "Replace getX()/setX() pairs with Kotlin properties.",
        defaultValue = true
    ),
    SAM_CONVERSIONS(
        propertyName = "samConversion",
        title = "Convert SAM interfaces to lambdas",
        description = "Convert anonymous classes implementing functional interfaces into lambdas.",
        defaultValue = true
    ),
    NULL_ANNOTATIONS(
        propertyName = "useNullabilityAnnotations",
        title = "Use nullability annotations",
        description = "Derive Kotlin nullability from @Nullable/@NotNull annotations.",
        defaultValue = true
    ),
    KOTLIN_COLLECTIONS(
        propertyName = "ktCollections",
        title = "Use Kotlin collection types",
        description = "Map Java collections to Kotlin collection interfaces.",
        defaultValue = true
    ),
    DATA_CLASSES(
        propertyName = "pojoToDataClass",
        title = "Convert POJOs to data classes",
        description = "Turn simple Java beans into Kotlin data classes.",
        defaultValue = false
    ),
    LATEINIT(
        propertyName = "lateinit",
        title = "Prefer lateinit fields",
        description = "Use lateinit var instead of nullable types for non-null fields.",
        defaultValue = false
    ),
    KEEP_COMMENTS(
        propertyName = "keepComments",
        title = "Keep comments and formatting",
        description = "Preserve comments and basic formatting from the original sources.",
        defaultValue = true
    );

    companion object {
        /** Returns only the options that are available in the current Kotlin plugin runtime. */
        fun available(): List<J2kOption> {
            val properties = ConverterSettings.defaultSettings::class.memberProperties.map { it.name }.toSet()
            return values().filter { it.propertyName in properties }
        }

        /** Applies the selected [options] to the given [settings] using reflection. */
        fun apply(settings: ConverterSettings, options: Map<J2kOption, Boolean>) {
            val kClass = settings::class
            for ((option, value) in options) {
                val property = kClass.memberProperties.firstOrNull { it.name == option.propertyName }
                @Suppress("UNCHECKED_CAST")
                (property as? KMutableProperty1<ConverterSettings, Boolean>)?.set(settings, value)
            }
        }
    }
}
