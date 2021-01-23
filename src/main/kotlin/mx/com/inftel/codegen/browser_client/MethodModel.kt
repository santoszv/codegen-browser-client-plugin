@file:Suppress("DuplicatedCode")

package mx.com.inftel.codegen.browser_client

import io.github.classgraph.MethodInfo
import io.github.classgraph.TypeSignature
import java.io.BufferedWriter

class MethodModel(private val methodInfo: MethodInfo) {

    val name: String by lazy {
        methodInfo.name
    }

    val resultType: TypeSignature by lazy {
        methodInfo.typeSignatureOrTypeDescriptor.resultType
    }

    val path: String by lazy {
        val pathAnnotationInfo = methodInfo.getAnnotationInfo("javax.ws.rs.Path")
        if (pathAnnotationInfo != null) {
            pathAnnotationInfo.parameterValues.first { it.name == "value" }.value as String
        } else {
            ""
        }
    }

    val isGet: Boolean by lazy {
        methodInfo.hasAnnotation("javax.ws.rs.GET")
    }

    val isPost: Boolean by lazy {
        methodInfo.hasAnnotation("javax.ws.rs.POST")
    }

    val isPut: Boolean by lazy {
        methodInfo.hasAnnotation("javax.ws.rs.PUT")
    }

    val isDelete: Boolean by lazy {
        methodInfo.hasAnnotation("javax.ws.rs.DELETE")
    }

    val parameters: List<ParameterModel> by lazy {
        methodInfo.parameterInfo.map { methodParameterInfo ->
            ParameterModel(methodParameterInfo)
        }
    }

    fun generateGet(writer: BufferedWriter, resourceModel: ResourceModel) {
        writer.newLine()
        writer.newLine()
        writer.write("    suspend fun ${name}(${parameters.asParametersString}): ${resultType.asTypeString} {")
        writer.newLine()
        writer.write("        if (antiReplayToken.isBlank()) {")
        writer.newLine()
        writer.write("            throw kotlin.IllegalStateException(\"'antiReplayToken' is blank\")")
        writer.newLine()
        writer.write("        }")
        generatePathLogic(writer, resourceModel)
        generateQueryLogic(writer)
        generateBodyLogic(writer)
        generateSendLogic(writer, "GET")
        generateResultLogic(writer)
        writer.newLine()
        writer.write("    }")
    }

    fun generatePost(writer: BufferedWriter, resourceModel: ResourceModel) {
        writer.newLine()
        writer.newLine()
        writer.write("    suspend fun ${name}(${parameters.asParametersString}): ${resultType.asTypeString} {")
        writer.newLine()
        writer.write("        if (antiReplayToken.isBlank()) {")
        writer.newLine()
        writer.write("            throw kotlin.IllegalStateException(\"'antiReplayToken' is blank\")")
        writer.newLine()
        writer.write("        }")
        generatePathLogic(writer, resourceModel)
        generateQueryLogic(writer)
        generateBodyLogic(writer)
        generateSendLogic(writer, "POST")
        generateResultLogic(writer)
        writer.newLine()
        writer.write("    }")
    }

    fun generatePut(writer: BufferedWriter, resourceModel: ResourceModel) {
        writer.newLine()
        writer.newLine()
        writer.write("    suspend fun ${name}(${parameters.asParametersString}): ${resultType.asTypeString} {")
        writer.newLine()
        writer.write("        if (antiReplayToken.isBlank()) {")
        writer.newLine()
        writer.write("            throw kotlin.IllegalStateException(\"'antiReplayToken' is blank\")")
        writer.newLine()
        writer.write("        }")
        generatePathLogic(writer, resourceModel)
        generateQueryLogic(writer)
        generateBodyLogic(writer)
        generateSendLogic(writer, "PUT")
        generateResultLogic(writer)
        writer.newLine()
        writer.write("    }")
    }

    fun generateDelete(writer: BufferedWriter, resourceModel: ResourceModel) {
        writer.newLine()
        writer.newLine()
        writer.write("    suspend fun ${name}(${parameters.asParametersString}): ${resultType.asTypeString} {")
        writer.newLine()
        writer.write("        if (antiReplayToken.isBlank()) {")
        writer.newLine()
        writer.write("            throw kotlin.IllegalStateException(\"'antiReplayToken' is blank\")")
        writer.newLine()
        writer.write("        }")
        generatePathLogic(writer, resourceModel)
        generateQueryLogic(writer)
        generateBodyLogic(writer)
        generateSendLogic(writer, "DELETE")
        generateResultLogic(writer)
        writer.newLine()
        writer.write("    }")
    }

    private fun generatePathLogic(writer: BufferedWriter, resourceModel: ResourceModel) {
        writer.newLine()
        writer.write("        val path = run {")
        writer.newLine()
        writer.write("            val components = kotlin.collections.ArrayList<String>()")
        writer.newLine()
        writer.write("            components.add(baseUrl)")
        if (resourceModel.path.isNotEmpty()) {
            writer.newLine()
            writer.write("            components.add(\"${resourceModel.path}\")")
        }
        if (path.isNotEmpty()) {
            writer.newLine()
            writer.write("            components.add(\"${path}\")")
        }
        writer.newLine()
        writer.write("            val path = components.joinToString(\"/\")")
        for (parameterModel in parameters.filter { it.isPathParam }) {
            writer.newLine()
            writer.write("                    .replace(\"{${parameterModel.pathParamName}}\", encodeURIComponent(${parameterModel.name}.toString()))")
        }
        writer.newLine()
        writer.write("            path")
        writer.newLine()
        writer.write("        }")
    }

    private fun generateQueryLogic(writer: BufferedWriter) {
        writer.newLine()
        writer.write("""        val query = run {""")
        writer.newLine()
        writer.write("""            val components = kotlin.collections.ArrayList<kotlin.Pair<kotlin.String, kotlin.String>>()""")
        for (parameterModel in parameters.filter { it.isQueryParam }) {
            if (parameterModel.type.isList) {
                writer.newLine()
                writer.write("            ${parameterModel.name}.forEach {")
                writer.newLine()
                writer.write("                components.add(kotlin.Pair(\"${parameterModel.queryParamName}\", it.toString()))")
                writer.newLine()
                writer.write("            }")
            } else {
                writer.newLine()
                writer.write("            components.add(kotlin.Pair(\"${parameterModel.queryParamName}\", ${parameterModel.name}.toString()))")
            }
        }
        writer.newLine()
        writer.write("            val query = components.joinToString(\"&\") {")
        writer.newLine()
        writer.write("                \"\${encodeURIComponent(it.first)}=\${encodeURIComponent(it.second)}\"")
        writer.newLine()
        writer.write("            }")
        writer.newLine()
        writer.write("            query")
        writer.newLine()
        writer.write("        }")
    }

    private fun generateBodyLogic(writer: BufferedWriter) {
        val parameterModel = parameters.firstOrNull { it.isBodyParam }
        if (parameterModel == null) {
            writer.newLine()
            writer.write("        val body: Any = kotlin.Unit")
        } else {
            writer.newLine()
            writer.write("        val body: Any = json.encodeToString(${parameterModel.type.asSerializerString}, ${parameterModel.name})")
        }
    }

    private fun generateSendLogic(writer: BufferedWriter, verb: String) {
        writer.newLine()
        writer.write("        val url = if (query.isBlank()) path else \"\${path}?\${query}\"")
        writer.newLine()
        writer.write("        val xhr = org.w3c.xhr.XMLHttpRequest()")
        writer.newLine()
        writer.write("        xhr.open(\"${verb}\", url)")
        writer.newLine()
        writer.write("        xhr.setRequestHeader(mx.com.inftel.codegen.ACCEPT_HEADER, mx.com.inftel.codegen.APPLICATION_JSON_TYPE)")
        writer.newLine()
        writer.write("        xhr.setRequestHeader(mx.com.inftel.codegen.ACCEPT_LANGUAGE_HEADER, language)")
        writer.newLine()
        writer.write("        xhr.setRequestHeader(mx.com.inftel.codegen.ANTI_REPLAY_TOKEN_HEADER, antiReplayToken)")
        writer.newLine()
        writer.write("        xhr.setRequestHeader(mx.com.inftel.codegen.AUTHENTICATION_TOKEN_HEADER, authenticationToken)")
        writer.newLine()
        writer.write("        xhr.setRequestHeader(mx.com.inftel.codegen.CONTENT_TYPE_HEADER, mx.com.inftel.codegen.APPLICATION_JSON_TYPE)")
        writer.newLine()
        writer.write("        xhr.aSend(body)")
        writer.newLine()
        writer.write("        antiReplayToken = xhr.getResponseHeader(mx.com.inftel.codegen.ANTI_REPLAY_TOKEN_HEADER)")
        writer.newLine()
        writer.write("                ?: \"\"")
        writer.newLine()
        writer.write("        val status = xhr.status.toInt()")
        writer.newLine()
        writer.write("        if (status !in 200..299) {")
        writer.newLine()
        writer.write("            throw mx.com.inftel.codegen.rest.XHRException(xhr)")
        writer.newLine()
        writer.write("        }")
    }

    private fun generateResultLogic(writer: BufferedWriter) {
        if (resultType.asTypeString != "kotlin.Unit") {
            writer.newLine()
            writer.write("        return this.json.decodeFromString(${resultType.asSerializerString}, xhr.responseText)")
        }
    }
}