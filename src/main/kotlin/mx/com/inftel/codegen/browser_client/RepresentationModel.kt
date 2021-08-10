/*
 *    Copyright 2021 Santos Zatarain Vera
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@file:Suppress("DuplicatedCode")

package mx.com.inftel.codegen.browser_client

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import java.io.BufferedWriter
import java.io.File

class RepresentationModel(private val classInfo: ClassInfo) {

    val packageName: String by lazy {
        classInfo.packageName
    }

    val simpleName: String by lazy {
        classInfo.simpleName
    }

    val properties: List<PropertyModel> by lazy {
        val getters = classInfo.methodInfo.filter { methodInfo: MethodInfo ->
            methodInfo.name.startsWith("get") || methodInfo.name.startsWith("is")
        }
        getters.mapNotNull { getter ->
            val setter = classInfo.methodInfo.firstOrNull { methodInfo ->
                methodInfo.name == "set${getter.capitalizedName}"
            }
            if (setter != null) {
                PropertyModel(getter)
            } else {
                null
            }
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
        writer.write("@kotlinx.serialization.Serializable")
        writer.newLine()
        writer.write("class $simpleName {")
        for (property in properties) {
            if (property.isNotNull) {
                when (val type = property.type.asTypeString) {
                    "kotlin.Byte" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var ${property.propertyName}: kotlin.Byte = 0")
                    }
                    "kotlin.Char" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var ${property.propertyName}: kotlin.Char = ' '")
                    }
                    "kotlin.Double" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var ${property.propertyName}: kotlin.Double = 0.0")
                    }
                    "kotlin.Float" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var ${property.propertyName}: kotlin.Float = 0.0f")
                    }
                    "kotlin.Int" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var ${property.propertyName}: kotlin.Int = 0")
                    }
                    "kotlin.Long" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var ${property.propertyName}: kotlin.Long = 0L")
                    }
                    "kotlin.Short" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var ${property.propertyName}: kotlin.Short = 0")
                    }
                    "kotlin.Boolean" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var is${property.capitalizedName}: kotlin.Boolean = false")
                    }
                    "kotlin.String" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var ${property.propertyName}: kotlin.String = \"\"")
                    }
                    else -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    lateinit var ${property.propertyName}: $type")
                    }
                }
            } else {
                when (val type = property.type.asTypeString) {
                    "kotlin.Boolean" -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var is${property.capitalizedName}: ${type}? = null")
                    }
                    else -> {
                        writer.newLine()
                        writer.newLine()
                        writer.write("    @kotlinx.serialization.SerialName(\"${property.propertyName}\")")
                        writer.newLine()
                        writer.write("    var ${property.propertyName}: ${type}? = null")
                    }
                }
            }
        }
        writer.newLine()
        writer.newLine()
        writer.write("}")
    }
}