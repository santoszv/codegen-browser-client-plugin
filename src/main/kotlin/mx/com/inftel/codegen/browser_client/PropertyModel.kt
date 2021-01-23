@file:Suppress("DuplicatedCode")

package mx.com.inftel.codegen.browser_client

import io.github.classgraph.MethodInfo
import io.github.classgraph.TypeSignature

class PropertyModel(val getter: MethodInfo) {

    val propertyName: String by lazy {
        getter.propertyName
    }

    val capitalizedName: String by lazy {
        getter.capitalizedName
    }

    val type: TypeSignature by lazy {
        getter.typeSignatureOrTypeDescriptor.resultType
    }

    val isNotNull: Boolean by lazy {
        getter.hasAnnotation("org.jetbrains.annotations.NotNull")
                || getter.hasAnnotation("javax.validation.constraints.NotNull")
                || getter.hasAnnotation("javax.validation.constraints.NotBlank")
    }
}