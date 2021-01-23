@file:Suppress("DuplicatedCode")

package mx.com.inftel.codegen.browser_client

import io.github.classgraph.BaseTypeSignature
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.MethodInfo
import io.github.classgraph.TypeSignature
import java.beans.Introspector

val List<ParameterModel>.asParametersString: String
    get() {
        return joinToString { parameter ->
            "${parameter.name}: ${parameter.type.asTypeString}"
        }
    }

val TypeSignature.asSerializerString: String
    get() {
        return when (this) {
            is BaseTypeSignature -> when (this.type) {
                Byte::class.javaPrimitiveType -> "kotlin.Byte.serializer()"
                Char::class.javaPrimitiveType -> "kotlin.Char.serializer()"
                Double::class.javaPrimitiveType -> "kotlin.Double.serializer()"
                Float::class.javaPrimitiveType -> "kotlin.Float.serializer()"
                Int::class.javaPrimitiveType -> "kotlin.Int.serializer()"
                Long::class.javaPrimitiveType -> "kotlin.Long.serializer()"
                Short::class.javaPrimitiveType -> "kotlin.Short.serializer()"
                Boolean::class.javaPrimitiveType -> "kotlin.Boolean.serializer()"
                Void::class.javaPrimitiveType -> "kotlin.Unit.serializer()"
                else -> throw RuntimeException()
            }
            is ClassRefTypeSignature -> when (this.fullyQualifiedClassName) {
                "java.lang.Byte" -> "kotlin.Byte.serializer()"
                "java.lang.Char" -> "kotlin.Char.serializer()"
                "java.lang.Double" -> "kotlin.Double.serializer()"
                "java.lang.Float" -> "kotlin.Float.serializer()"
                "java.lang.Integer" -> "kotlin.Int.serializer()"
                "java.lang.Long" -> "kotlin.Long.serializer()"
                "java.lang.Short" -> "kotlin.Short.serializer()"
                "java.lang.Boolean" -> "kotlin.Boolean.serializer()"
                "java.lang.Void" -> "kotlin.Unit.serializer()"
                "java.lang.String" -> "kotlin.String.serializer()"
                "java.util.List" -> "kotlinx.serialization.builtins.ListSerializer(${listTypeParameter.asSerializerString})"
                "java.util.Map" -> {
                    val (key, value) = mapTypeParameters
                    "kotlinx.serialization.builtins.MapSerializer(${key.asSerializerString}, ${value.asSerializerString})"
                }
                else -> "$asTypeString.serializer()"
            }
            else -> throw RuntimeException()
        }
    }

val TypeSignature.asTypeString: String
    get() {
        return when (this) {
            is BaseTypeSignature -> when (this.type) {
                Byte::class.javaPrimitiveType -> "kotlin.Byte"
                Char::class.javaPrimitiveType -> "kotlin.Char"
                Double::class.javaPrimitiveType -> "kotlin.Double"
                Float::class.javaPrimitiveType -> "kotlin.Float"
                Int::class.javaPrimitiveType -> "kotlin.Int"
                Long::class.javaPrimitiveType -> "kotlin.Long"
                Short::class.javaPrimitiveType -> "kotlin.Short"
                Boolean::class.javaPrimitiveType -> "kotlin.Boolean"
                Void::class.javaPrimitiveType -> "kotlin.Unit"
                else -> throw RuntimeException()
            }
            is ClassRefTypeSignature -> when (this.fullyQualifiedClassName) {
                "java.lang.Byte" -> "kotlin.Byte"
                "java.lang.Char" -> "kotlin.Char"
                "java.lang.Double" -> "kotlin.Double"
                "java.lang.Float" -> "kotlin.Float"
                "java.lang.Integer" -> "kotlin.Int"
                "java.lang.Long" -> "kotlin.Long"
                "java.lang.Short" -> "kotlin.Short"
                "java.lang.Boolean" -> "kotlin.Boolean"
                "java.lang.Void" -> "kotlin.Unit"
                "java.lang.String" -> "kotlin.String"
                "java.util.List" -> "kotlin.collections.List<${this.listTypeParameter.asTypeString}>"
                "java.util.Map" -> {
                    val (key, value) = this.mapTypeParameters
                    "kotlin.collections.Map<${key.asTypeString}, ${value.asTypeString}>"
                }
                else -> fullyQualifiedClassName
            }
            else -> throw RuntimeException()
        }
    }

val TypeSignature.isList: Boolean
    get() {
        return (this as? ClassRefTypeSignature)?.fullyQualifiedClassName == "java.util.List"
    }

val TypeSignature.isMap: Boolean
    get() {
        return (this as? ClassRefTypeSignature)?.fullyQualifiedClassName == "java.util.Map"
    }

val TypeSignature.listTypeParameter: ClassRefTypeSignature
    get() {
        return (this as ClassRefTypeSignature).typeArguments[0].typeSignature as ClassRefTypeSignature
    }

val TypeSignature.mapTypeParameters: Pair<ClassRefTypeSignature, ClassRefTypeSignature>
    get() {
        val mapRefTypeSignature = this as ClassRefTypeSignature
        val keyRefTypeSignature = mapRefTypeSignature.typeArguments[0].typeSignature as ClassRefTypeSignature
        val valueRefTypeSignature = mapRefTypeSignature.typeArguments[1].typeSignature as ClassRefTypeSignature
        return Pair(keyRefTypeSignature, valueRefTypeSignature)
    }

val MethodInfo.capitalizedName: String
    get() {
        return when {
            name.startsWith("get") -> name.substring(3)
            name.startsWith("set") -> name.substring(3)
            name.startsWith("is") -> name.substring(2)
            else -> throw RuntimeException()
        }
    }

val MethodInfo.propertyName: String
    get() {
        return Introspector.decapitalize(capitalizedName)
    }