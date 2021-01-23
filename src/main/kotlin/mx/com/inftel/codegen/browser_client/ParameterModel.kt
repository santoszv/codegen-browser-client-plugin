@file:Suppress("DuplicatedCode")

package mx.com.inftel.codegen.browser_client

import io.github.classgraph.MethodParameterInfo
import io.github.classgraph.TypeSignature

class ParameterModel(private val methodParameterInfo: MethodParameterInfo) {

    val name: String by lazy {
        methodParameterInfo.name
    }

    val type: TypeSignature by lazy {
        methodParameterInfo.typeSignatureOrTypeDescriptor
    }

    val isHeaderParam: Boolean by lazy {
        methodParameterInfo.hasAnnotation("javax.ws.rs.HeaderParam")
    }

    val isMatrixParam: Boolean by lazy {
        methodParameterInfo.hasAnnotation("javax.ws.rs.MatrixParam")
    }

    val isQueryParam: Boolean by lazy {
        methodParameterInfo.hasAnnotation("javax.ws.rs.QueryParam")
    }

    val isPathParam: Boolean by lazy {
        methodParameterInfo.hasAnnotation("javax.ws.rs.PathParam")
    }

    val isBodyParam: Boolean by lazy {
        !(isHeaderParam || isMatrixParam || isQueryParam || isPathParam)
    }

    val headerParamName: String by lazy {
        val pathAnnotationInfo = methodParameterInfo.getAnnotationInfo("javax.ws.rs.HeaderParam")
        if (pathAnnotationInfo != null) {
            pathAnnotationInfo.parameterValues.first { it.name == "value" }.value as String
        } else {
            ""
        }
    }

    val matrixParamName: String by lazy {
        val pathAnnotationInfo = methodParameterInfo.getAnnotationInfo("javax.ws.rs.MatrixParam")
        if (pathAnnotationInfo != null) {
            pathAnnotationInfo.parameterValues.first { it.name == "value" }.value as String
        } else {
            ""
        }
    }

    val queryParamName: String by lazy {
        val pathAnnotationInfo = methodParameterInfo.getAnnotationInfo("javax.ws.rs.QueryParam")
        if (pathAnnotationInfo != null) {
            pathAnnotationInfo.parameterValues.first { it.name == "value" }.value as String
        } else {
            ""
        }
    }

    val pathParamName: String by lazy {
        val pathAnnotationInfo = methodParameterInfo.getAnnotationInfo("javax.ws.rs.PathParam")
        if (pathAnnotationInfo != null) {
            pathAnnotationInfo.parameterValues.first { it.name == "value" }.value as String
        } else {
            ""
        }
    }
}