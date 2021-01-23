@file:Suppress("DuplicatedCode")

package mx.com.inftel.codegen.browser_client

import io.github.classgraph.ClassInfo
import java.io.BufferedWriter
import java.io.File
import java.lang.reflect.Modifier

class ResourceModel(private val classInfo: ClassInfo) {

    val packageName: String by lazy {
        classInfo.packageName
    }

    val simpleName: String by lazy {
        classInfo.simpleName
    }

    val path: String by lazy {
        val pathAnnotationInfo = classInfo.getAnnotationInfo("javax.ws.rs.Path")
        if (pathAnnotationInfo != null) {
            pathAnnotationInfo.parameterValues.first { it.name == "value" }.value as String
        } else {
            ""
        }
    }

    val methods: List<MethodModel> by lazy {
        classInfo.methodInfo.filter { methodInfo ->
            Modifier.isPublic(methodInfo.modifiers)
                    && !Modifier.isStatic(methodInfo.modifiers)
                    && !Modifier.isAbstract(methodInfo.modifiers)
        }.filter { methodInfo ->
            methodInfo.hasAnnotation("javax.ws.rs.GET")
                    || methodInfo.hasAnnotation("javax.ws.rs.POST")
                    || methodInfo.hasAnnotation("javax.ws.rs.PUT")
                    || methodInfo.hasAnnotation("javax.ws.rs.DELETE")
        }.map { methodInfo ->
            MethodModel(methodInfo)
        }
    }

    fun generateCode(outputDir: File) {
        val packageDir = packageName.replace('.', '/')
        val outputFile = if (packageDir.isBlank()) {
            File(outputDir, "${simpleName}.kt")
        } else {
            File(outputDir, "${packageDir}/${simpleName}.kt")
        }
        outputFile.parentFile.mkdirs()
        outputFile.bufferedWriter().use { writer ->
            generateCode(writer)
        }
    }

    private fun generateCode(writer: BufferedWriter) {
        if (packageName.isNotBlank()) {
            writer.write("package $packageName")
        }
        writer.newLine()
        writer.newLine()
        writer.write("import kotlinx.serialization.builtins.serializer")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.rest.aSend")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.rest.encodeURIComponent")
        writer.newLine()
        writer.newLine()
        writer.write("class ${simpleName}(var baseUrl: kotlin.String, var authenticationToken: kotlin.String, var language: kotlin.String) {")
        writer.newLine()
        writer.newLine()
        writer.write("    var antiReplayToken = \"\"")
        writer.newLine()
        writer.newLine()
        writer.write("    var json = kotlinx.serialization.json.Json { encodeDefaults = true }")
        generateInit(writer)
        for (method in methods) {
            if (method.isGet) {
                method.generateGet(writer, this)
            }
            if (method.isPost) {
                method.generatePost(writer, this)
            }
            if (method.isPut) {
                method.generatePut(writer, this)
            }
            if (method.isDelete) {
                method.generateDelete(writer, this)
            }
        }
        writer.newLine()
        writer.write("}")
    }

    private fun generateInit(writer: BufferedWriter) {
        writer.newLine()
        writer.newLine()
        writer.write("    private suspend fun init() {")
        writer.newLine()
        writer.write("        val xhr = org.w3c.xhr.XMLHttpRequest()")
        writer.newLine()
        writer.write("        xhr.open(\"HEAD\", baseUrl)")
        writer.newLine()
        writer.write("        xhr.setRequestHeader(mx.com.inftel.codegen.ANTI_REPLAY_TOKEN_HEADER, mx.com.inftel.codegen.NULL_ANTI_REPLAY_TOKEN)")
        writer.newLine()
        writer.write("        xhr.aSend(kotlin.Unit)")
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
        writer.newLine()
        writer.write("        if (antiReplayToken.isBlank()) {")
        writer.newLine()
        writer.write("            throw kotlin.IllegalStateException(\"'antiReplayToken' is blank\")")
        writer.newLine()
        writer.write("        }")
        writer.newLine()
        writer.write("    }")
    }
}