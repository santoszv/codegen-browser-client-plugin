package mx.com.inftel.codegen.browser_client

import io.github.classgraph.ClassInfo
import io.github.classgraph.FieldInfo
import io.github.classgraph.MethodInfo
import java.io.BufferedWriter
import java.io.File
import java.lang.reflect.Modifier

class EnumerationModel(private val classInfo: ClassInfo) {

    val packageName: String by lazy {
        classInfo.packageName
    }

    val simpleName: String by lazy {
        classInfo.simpleName
    }

    val values: List<String> by lazy {
        val fields = classInfo.fieldInfo.filter { fieldInfo ->
            Modifier.isPublic(fieldInfo.modifiers) && Modifier.isStatic(fieldInfo.modifiers)
        }
        fields.map { fieldInfo ->
            fieldInfo.name
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
        writer.write("enum class $simpleName {")
        writer.newLine()
        for ((i, value) in values.withIndex()) {
            if (i != 0) {
                writer.write(",")
            }
            writer.newLine()
            writer.write("    $value")
        }
        writer.newLine()
        writer.newLine()
        writer.write("}")
    }
}