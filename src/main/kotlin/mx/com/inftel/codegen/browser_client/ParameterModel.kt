package mx.com.inftel.codegen.browser_client

import io.github.classgraph.TypeSignature

class ParameterModel {

    var parameterName = ""

    lateinit var parameterType: TypeSignature

    var headerParam = false
    var headerParamName = "false"
    var matrixParam = false
    var matrixParamName = "false"
    var queryParam = false
    var queryParamName = "false"
    var pathParam = false
    var pathParamName = "false"

    val bodyEntity: Boolean
        get() = !(headerParam || matrixParam || queryParam || pathParam)
}