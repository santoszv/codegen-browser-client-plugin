package mx.com.inftel.codegen.browser_client

import io.github.classgraph.TypeSignature

class MethodModel {

    var methodName = ""

    lateinit var resultType: TypeSignature

    var path = ""

    var getMethod = false
    var postMethod = false
    var putMethod = false
    var deleteMethod = false

    val generateCode: Boolean
        get() = getMethod || postMethod || putMethod || deleteMethod

    val parameters = mutableMapOf<String, ParameterModel>()
}