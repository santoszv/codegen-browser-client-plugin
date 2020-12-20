@file:Suppress("DuplicatedCode")

package mx.com.inftel.codegen.browser_client

import io.github.classgraph.*
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.beans.Introspector
import java.io.BufferedWriter
import java.io.File
import java.lang.reflect.Modifier

open class GenerateBrowserClientTask : DefaultTask() {

    @get:Internal
    val classpath: Property<String> = project.objects.property(String::class.java)

    @get:Internal
    val output: Property<String> = project.objects.property(String::class.java)

    private val representations = mutableMapOf<String, RepresentationModel>()

    init {
        group = groupName
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun execute() {
        ClassGraph()
            .overrideClassLoaders(ClassLoader.getPlatformClassLoader())
            .overrideClasspath(classpath.get())
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()
            .use { result ->
                val outputDir = project.file(output.get())
                val classInfoList = result.getClassesWithAnnotation("mx.com.inftel.codegen.rest.BrowserClient")
                for (classInfo in classInfoList) {
                    if (Modifier.isPublic(classInfo.modifiers)
                        && !Modifier.isStatic(classInfo.modifiers)
                        && !Modifier.isAbstract(classInfo.modifiers)
                    ) {
                        val resourceModel = ResourceModel().also { it.fillResourceModel(classInfo) }
                        val simpleName = classInfo.simpleName
                        val packageName = classInfo.packageName
                        val packageDir = packageName.replace('.', '/')
                        val outputFile = if (packageDir.isBlank()) {
                            File(outputDir, "${simpleName}.kt")
                        } else {
                            File(outputDir, "${packageDir}/${simpleName}.kt")
                        }
                        outputFile.parentFile.mkdirs()
                        outputFile.bufferedWriter().use { writer ->
                            generateResourceCode(writer, resourceModel)
                        }
                        for (method in resourceModel.methods.values) {
                            val resultType = method.resultType
                            if (resultType.isList) {
                                val listTypeParameter = resultType.listTypeParameter
                                representations.putIfAbsent(
                                    listTypeParameter.fullyQualifiedClassName,
                                    RepresentationModel().also { it.fillRepresentationModel(listTypeParameter.classInfo) })
                            } else if (resultType is ClassRefTypeSignature) {
                                representations.putIfAbsent(
                                    resultType.fullyQualifiedClassName,
                                    RepresentationModel().also { it.fillRepresentationModel(resultType.classInfo) })
                            }
                            for (parameter in method.parameters.values) {
                                if (parameter.bodyEntity) {
                                    val parameterType = parameter.parameterType
                                    if (parameterType.isList) {
                                        val listTypeParameter = parameterType.listTypeParameter
                                        representations.putIfAbsent(
                                            listTypeParameter.fullyQualifiedClassName,
                                            RepresentationModel().also { it.fillRepresentationModel(listTypeParameter.classInfo) })
                                    } else if (parameterType is ClassRefTypeSignature) {
                                        representations.putIfAbsent(
                                            parameterType.fullyQualifiedClassName,
                                            RepresentationModel().also { it.fillRepresentationModel(parameterType.classInfo) })
                                    }
                                }
                            }
                        }
                    }
                }
                for (representation in representations.values) {
                    val simpleName = representation.representationSimpleName
                    val packageName = representation.representationPackageName
                    val packageDir = packageName.replace('.', '/')
                    val outputFile = if (packageDir.isBlank()) {
                        File(outputDir, "${simpleName}.kt")
                    } else {
                        File(outputDir, "${packageDir}/${simpleName}.kt")
                    }
                    outputFile.parentFile.mkdirs()
                    outputFile.bufferedWriter().use { writer ->
                        generateRepresentationCode(writer, representation)
                    }
                }
            }
    }

    private fun generateResourceCode(writer: BufferedWriter, resourceModel: ResourceModel) {
        if (resourceModel.resourcePackageName.isNotBlank()) {
            writer.write("package ${resourceModel.resourcePackageName}")
        }
        writer.newLine()
        writer.newLine()
        writer.write("import kotlinx.serialization.builtins.serializer as _codegen_serializer")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.ACCEPT_HEADER as _codegen_ACCEPT_HEADER")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.ACCEPT_LANGUAGE_HEADER as _codegen_ACCEPT_LANGUAGE_HEADER")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.ANTI_REPLAY_TOKEN_HEADER as _codegen_ANTI_REPLAY_TOKEN_HEADER")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.AUTHENTICATION_TOKEN_HEADER as _codegen_AUTHENTICATION_TOKEN_HEADER")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.CONTENT_TYPE_HEADER as _codegen_CONTENT_TYPE_HEADER")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.NULL_ANTI_REPLAY_TOKEN as _codegen_NULL_ANTI_REPLAY_TOKEN")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.APPLICATION_JSON_TYPE as _codegen_APPLICATION_JSON_TYPE")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.browser.aSend as _codegen_aSend")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.browser.encodeURIComponent as _codegen_encodeURIComponent")
        writer.newLine()
        writer.write("import mx.com.inftel.codegen.rest.XHRException as _codegen_XHRException")
        writer.newLine()
        writer.newLine()
        writer.write("class ${resourceModel.resourceSimpleName}(private val apiUrl: kotlin.String, private val authenticationToken: kotlin.String, private val acceptLanguage: kotlin.String) {")
        writer.newLine()
        writer.newLine()
        writer.write("    private var antiReplayToken = \"\"")
        generateCheckAntiReplayToken(writer)
        for (methodModel in resourceModel.methods.values) {
            if (methodModel.getMethod) {
                generateGET(writer, resourceModel, methodModel)
            }
            if (methodModel.postMethod) {
                generatePOST(writer, resourceModel, methodModel)
            }
            if (methodModel.putMethod) {
                generatePUT(writer, resourceModel, methodModel)
            }
            if (methodModel.deleteMethod) {
                generateDELETE(writer, resourceModel, methodModel)
            }
        }
        writer.newLine()
        writer.write("}")
    }

    private fun generateCheckAntiReplayToken(writer: BufferedWriter) {
        writer.newLine()
        writer.newLine()
        writer.write("    private suspend fun _checkAntiReplayToken() {")
        writer.newLine()
        writer.write("        if (this.antiReplayToken.isNotBlank()) {")
        writer.newLine()
        writer.write("            return")
        writer.newLine()
        writer.write("        }")
        writer.newLine()
        writer.write("        val _xhr = org.w3c.xhr.XMLHttpRequest()")
        writer.newLine()
        writer.write("        _xhr.open(\"HEAD\", this.apiUrl)")
        writer.newLine()
        writer.write("        _xhr.setRequestHeader(_codegen_ANTI_REPLAY_TOKEN_HEADER, _codegen_NULL_ANTI_REPLAY_TOKEN)")
        writer.newLine()
        writer.write("        _xhr._codegen_aSend(kotlin.Unit)")
        writer.newLine()
        writer.write("        this.antiReplayToken = _xhr.getResponseHeader(_codegen_ANTI_REPLAY_TOKEN_HEADER)")
        writer.newLine()
        writer.write("                ?: \"\"")
        writer.newLine()
        writer.write("        val _status = _xhr.status.toInt()")
        writer.newLine()
        writer.write("        if (_status !in 200..299) {")
        writer.newLine()
        writer.write("            throw _codegen_XHRException(_xhr)")
        writer.newLine()
        writer.write("        }")
        writer.newLine()
        writer.write("        if (this.antiReplayToken.isBlank()) {")
        writer.newLine()
        writer.write("            throw kotlin.IllegalStateException()")
        writer.newLine()
        writer.write("        }")
        writer.newLine()
        writer.write("    }")
    }

    private fun generateGET(writer: BufferedWriter, resourceModel: ResourceModel, methodModel: MethodModel) {
        writer.newLine()
        writer.newLine()
        writer.write("    suspend fun ${methodModel.methodName}(${methodModel.parametersAsString}): ${methodModel.resultType.asString} {")
        writer.newLine()
        writer.write("        _checkAntiReplayToken()")
        generatePathLogic(writer, resourceModel, methodModel)
        generateQueryLogic(writer, methodModel)
        writer.newLine()
        writer.write("        val _body: Any = kotlin.Unit")
        generateSendLogic(writer, "GET")
        generateResultLogic(writer, methodModel)
        writer.newLine()
        writer.write("    }")
    }

    private fun generatePOST(writer: BufferedWriter, resourceModel: ResourceModel, methodModel: MethodModel) {
        writer.newLine()
        writer.newLine()
        writer.write("    suspend fun ${methodModel.methodName}(${methodModel.parametersAsString}): ${methodModel.resultType.asString} {")
        writer.newLine()
        writer.write("        _checkAntiReplayToken()")
        generatePathLogic(writer, resourceModel, methodModel)
        generateQueryLogic(writer, methodModel)
        generateBodyLogic(writer, methodModel)
        generateSendLogic(writer, "POST")
        generateResultLogic(writer, methodModel)
        writer.newLine()
        writer.write("    }")
    }

    private fun generatePUT(writer: BufferedWriter, resourceModel: ResourceModel, methodModel: MethodModel) {
        writer.newLine()
        writer.newLine()
        writer.write("    suspend fun ${methodModel.methodName}(${methodModel.parametersAsString}): ${methodModel.resultType.asString} {")
        writer.newLine()
        writer.write("        _checkAntiReplayToken()")
        generatePathLogic(writer, resourceModel, methodModel)
        generateQueryLogic(writer, methodModel)
        generateBodyLogic(writer, methodModel)
        generateSendLogic(writer, "PUT")
        generateResultLogic(writer, methodModel)
        writer.newLine()
        writer.write("    }")
    }

    private fun generateDELETE(writer: BufferedWriter, resourceModel: ResourceModel, methodModel: MethodModel) {
        writer.newLine()
        writer.newLine()
        writer.write("    suspend fun ${methodModel.methodName}(${methodModel.parametersAsString}): ${methodModel.resultType.asString} {")
        writer.newLine()
        writer.write("        _checkAntiReplayToken()")
        generatePathLogic(writer, resourceModel, methodModel)
        generateQueryLogic(writer, methodModel)
        writer.newLine()
        writer.write("        val _body: Any = kotlin.Unit")
        generateSendLogic(writer, "DELETE")
        generateResultLogic(writer, methodModel)
        writer.newLine()
        writer.write("    }")
    }

    private fun generatePathLogic(writer: BufferedWriter, resourceModel: ResourceModel, methodModel: MethodModel) {
        writer.newLine()
        writer.write("        val _path = run {")
        writer.newLine()
        writer.write("            val _components = kotlin.collections.ArrayList<String>()")
        writer.newLine()
        writer.write("            _components.add(this.apiUrl)")
        if (resourceModel.path.isNotEmpty()) {
            writer.newLine()
            writer.write("            _components.add(\"${resourceModel.path}\")")
        }
        if (methodModel.path.isNotEmpty()) {
            writer.newLine()
            writer.write("            _components.add(\"${methodModel.path}\")")
        }
        writer.newLine()
        writer.write("            val _path = _components.joinToString(\"/\")")
        for (parameterModel in methodModel.parameters.values.filter { it.pathParam }) {
            writer.newLine()
            writer.write("                    .replace(\"{${parameterModel.pathParamName}}\", _codegen_encodeURIComponent(${parameterModel.parameterName}.toString()))")
        }
        writer.newLine()
        writer.write("            _path")
        writer.newLine()
        writer.write("        }")
    }

    private fun generateQueryLogic(writer: BufferedWriter, methodModel: MethodModel) {
        writer.newLine()
        writer.write("""        val _query = run {""")
        writer.newLine()
        writer.write("""            val _components = kotlin.collections.ArrayList<kotlin.Pair<kotlin.String, kotlin.String>>()""")
        for (parameterModel in methodModel.parameters.values.filter { it.queryParam }) {
            if (parameterModel.parameterType.isList) {
                writer.newLine()
                writer.write("            ${parameterModel.parameterName}.forEach {")
                writer.newLine()
                writer.write("                _components.add(kotlin.Pair(\"${parameterModel.queryParamName}\", it.toString()))")
                writer.newLine()
                writer.write("            }")
            } else {
                writer.newLine()
                writer.write("            _components.add(kotlin.Pair(\"${parameterModel.queryParamName}\", ${parameterModel.parameterName}.toString()))")
            }
        }
        writer.newLine()
        writer.write("            val _query = _components.joinToString(\"&\") {")
        writer.newLine()
        writer.write("                \"\${it.first}=\${_codegen_encodeURIComponent(it.second)}\"")
        writer.newLine()
        writer.write("            }")
        writer.newLine()
        writer.write("            _query")
        writer.newLine()
        writer.write("        }")
    }

    private fun generateBodyLogic(writer: BufferedWriter, methodModel: MethodModel) {
        val bodyEntity = methodModel.parameters.values.firstOrNull { it.bodyEntity }
        if (bodyEntity == null) {
            writer.newLine()
            writer.write("        val _body: Any = kotlin.Unit")
        } else {
            writer.newLine()
            writer.write("        val _body: Any = kotlinx.serialization.json.Json.Default.encodeToString(${bodyEntity.parameterType.asSerializer}, ${bodyEntity.parameterName})")
        }
    }

    private fun generateSendLogic(writer: BufferedWriter, verb: String) {
        writer.newLine()
        writer.write("        val _url = if (_query.isBlank()) _path else \"\${_path}?\${_query}\"")
        writer.newLine()
        writer.write("        val _xhr = org.w3c.xhr.XMLHttpRequest()")
        writer.newLine()
        writer.write("        _xhr.open(\"${verb}\", _url)")
        writer.newLine()
        writer.write("        _xhr.setRequestHeader(_codegen_ACCEPT_HEADER, _codegen_APPLICATION_JSON_TYPE)")
        writer.newLine()
        writer.write("        _xhr.setRequestHeader(_codegen_ACCEPT_LANGUAGE_HEADER, this.acceptLanguage)")
        writer.newLine()
        writer.write("        _xhr.setRequestHeader(_codegen_ANTI_REPLAY_TOKEN_HEADER, this.antiReplayToken)")
        writer.newLine()
        writer.write("        _xhr.setRequestHeader(_codegen_AUTHENTICATION_TOKEN_HEADER, this.authenticationToken)")
        writer.newLine()
        writer.write("        _xhr.setRequestHeader(_codegen_CONTENT_TYPE_HEADER, _codegen_APPLICATION_JSON_TYPE)")
        writer.newLine()
        writer.write("        _xhr._codegen_aSend(_body)")
        writer.newLine()
        writer.write("        this.antiReplayToken = _xhr.getResponseHeader(_codegen_ANTI_REPLAY_TOKEN_HEADER)")
        writer.newLine()
        writer.write("                ?: \"\"")
        writer.newLine()
        writer.write("        val _status = _xhr.status.toInt()")
        writer.newLine()
        writer.write("        if (_status !in 200..299) {")
        writer.newLine()
        writer.write("            throw _codegen_XHRException(_xhr)")
        writer.newLine()
        writer.write("        }")
    }

    private fun generateResultLogic(writer: BufferedWriter, methodModel: MethodModel) {
        val resultType = methodModel.resultType
        if (resultType.asString != "kotlin.Unit") {
            writer.newLine()
            writer.write("        return kotlinx.serialization.json.Json.Default.decodeFromString(${resultType.asSerializer}, _xhr.responseText)")
        }
    }

    private fun generateRepresentationCode(writer: BufferedWriter, representationModel: RepresentationModel) {
        if (representationModel.representationPackageName.isNotBlank()) {
            writer.write("package ${representationModel.representationPackageName}")
        }
        writer.newLine()
        writer.newLine()
        writer.write("@kotlinx.serialization.Serializable")
        writer.newLine()
        writer.write("class ${representationModel.representationSimpleName} {")
        //
        for (propertyModel in representationModel.properties.values) {
            if (propertyModel.isGeneratedCode) {
                if (propertyModel.isNotNull) {
                    when (propertyModel.propertyGetter.typeSignatureOrTypeDescriptor.resultType.asString) {
                        "kotlin.Byte" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var ${propertyModel.propertyName}: kotlin.Byte = 0")
                        }
                        "kotlin.Char" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var ${propertyModel.propertyName}: kotlin.Char = ' '")
                        }
                        "kotlin.Double" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var ${propertyModel.propertyName}: kotlin.Double = 0.0")
                        }
                        "kotlin.Float" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var ${propertyModel.propertyName}: kotlin.Float = 0.0f")
                        }
                        "kotlin.Int" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var ${propertyModel.propertyName}: kotlin.Int = 0")
                        }
                        "kotlin.Long" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var ${propertyModel.propertyName}: kotlin.Long = 0L")
                        }
                        "kotlin.Short" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var ${propertyModel.propertyName}: kotlin.Short = 0")
                        }
                        "kotlin.Boolean" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var is${propertyModel.propertyName.capitalize()}: kotlin.Boolean = false")
                        }
                        "kotlin.String" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var ${propertyModel.propertyName}: kotlin.String = \"\"")
                        }
                        else -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    lateinit var ${propertyModel.propertyName}: ${propertyModel.propertyGetter.typeSignatureOrTypeDescriptor.resultType.asString}")
                        }
                    }
                } else {
                    when (val resultTypeAsString = propertyModel.propertyGetter.typeSignatureOrTypeDescriptor.resultType.asString) {
                        "kotlin.Boolean" -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var is${propertyModel.propertyName.capitalize()}: ${resultTypeAsString}? = null")
                        }
                        else -> {
                            writer.newLine()
                            writer.newLine()
                            writer.write("    var ${propertyModel.propertyName}: ${resultTypeAsString}? = null")
                        }
                    }
                }
            }
        }
        //
        writer.newLine()
        writer.newLine()
        writer.write("}")
    }

    private fun ResourceModel.fillResourceModel(classInfo: ClassInfo) {
        resourceQualifiedName = classInfo.name
        val pathAnnotationInfo = classInfo.getAnnotationInfo("javax.ws.rs.Path")
        if (pathAnnotationInfo != null) {
            path = pathAnnotationInfo.parameterValues.first { it.name == "value" }.value as String
        }
        for (methodInfo in classInfo.methodInfo) {
            if (Modifier.isPublic(methodInfo.modifiers)
                && !Modifier.isStatic(methodInfo.modifiers)
                && !Modifier.isAbstract(methodInfo.modifiers)
            ) {
                methods[methodInfo.name] = MethodModel().also { it.fillMethodModel(methodInfo) }
            }
        }
    }

    private fun MethodModel.fillMethodModel(methodInfo: MethodInfo) {
        methodName = methodInfo.name
        resultType = methodInfo.typeSignatureOrTypeDescriptor.resultType
        //
        val pathAnnotationInfo = methodInfo.getAnnotationInfo("javax.ws.rs.Path")
        if (pathAnnotationInfo != null) {
            path = pathAnnotationInfo.parameterValues.first { it.name == "value" }.value as String
        }
        //
        getMethod = methodInfo.hasAnnotation("javax.ws.rs.GET")
        postMethod = methodInfo.hasAnnotation("javax.ws.rs.POST")
        putMethod = methodInfo.hasAnnotation("javax.ws.rs.PUT")
        deleteMethod = methodInfo.hasAnnotation("javax.ws.rs.DELETE")
        //
        for (methodParameterInfo in methodInfo.parameterInfo) {
            parameters[methodParameterInfo.name] = ParameterModel().also { it.fillParameterModel(methodParameterInfo) }
        }
    }

    private fun ParameterModel.fillParameterModel(methodParameterInfo: MethodParameterInfo) {
        parameterName = methodParameterInfo.name
        parameterType = methodParameterInfo.typeSignatureOrTypeDescriptor
        //
        val headerAnnotation = methodParameterInfo.getAnnotationInfo("javax.ws.rs.HeaderParam")
        if (headerAnnotation != null) {
            headerParam = true
            headerParamName = headerAnnotation.parameterValues.first { it.name == "value" }.value as String
        }
        //
        val matrixAnnotation = methodParameterInfo.getAnnotationInfo("javax.ws.rs.MatrixParam")
        if (matrixAnnotation != null) {
            matrixParam = true
            matrixParamName = matrixAnnotation.parameterValues.first { it.name == "value" }.value as String
        }
        //
        val queryAnnotation = methodParameterInfo.getAnnotationInfo("javax.ws.rs.QueryParam")
        if (queryAnnotation != null) {
            queryParam = true
            queryParamName = queryAnnotation.parameterValues.first { it.name == "value" }.value as String
        }
        //
        val pathAnnotation = methodParameterInfo.getAnnotationInfo("javax.ws.rs.PathParam")
        if (pathAnnotation != null) {
            pathParam = true
            pathParamName = pathAnnotation.parameterValues.first { it.name == "value" }.value as String
        }
    }

    private fun RepresentationModel.fillRepresentationModel(classInfo: ClassInfo) {
        representationQualifiedName = classInfo.name
        for (methodInfo in classInfo.methodInfo) {
            if (methodInfo.isGetter) {
                properties.getOrPut(methodInfo.propertyName) { PropertyModel() }.fillPropertyModel(methodInfo)
            }
            if (methodInfo.isSetter) {
                properties.getOrPut(methodInfo.propertyName) { PropertyModel() }.propertySetter = methodInfo
            }
        }
    }

    private fun PropertyModel.fillPropertyModel(getter: MethodInfo) {
        if (!this.isGetterInitialized) {
            this.propertyGetter = getter
            this.propertyName = getter.propertyName
            this.capitalizedName = getter.capitalizedName
        }
        //
        isNotNull = getter.hasAnnotation("org.jetbrains.annotations.NotNull")
                || getter.hasAnnotation("javax.validation.constraints.NotNull")
                || getter.hasAnnotation("javax.validation.constraints.NotBlank")
    }

    private val MethodModel.parametersAsString: String
        get() {
            return parameters.values.joinToString { parameter ->
                "${parameter.parameterName}: ${parameter.parameterType.asString}"
            }
        }

    private val MethodInfo.isGetter: Boolean
        get() {
            return Modifier.isPublic(modifiers)
                    && !Modifier.isStatic(modifiers)
                    && !Modifier.isAbstract(modifiers)
                    && (name.startsWith("get") || name.startsWith("is"))
        }

    private val MethodInfo.isSetter: Boolean
        get() {
            return Modifier.isPublic(modifiers)
                    && !Modifier.isStatic(modifiers)
                    && !Modifier.isAbstract(modifiers)
                    && name.startsWith("set")
        }

    private val MethodInfo.capitalizedName: String
        get() {
            return when {
                name.startsWith("get") -> name.substring(3)
                name.startsWith("set") -> name.substring(3)
                name.startsWith("is") -> name.substring(2)
                else -> throw RuntimeException()
            }
        }

    private val MethodInfo.propertyName: String
        get() {
            return Introspector.decapitalize(capitalizedName)
        }

    private val TypeSignature.isList: Boolean
        get() {
            return (this as? ClassRefTypeSignature)?.fullyQualifiedClassName == "java.util.List"
        }

    private val TypeSignature.listTypeParameter: ClassRefTypeSignature
        get() {
            return (this as ClassRefTypeSignature).typeArguments[0].typeSignature as ClassRefTypeSignature
        }

    private val TypeSignature.isMap: Boolean
        get() {
            return (this as? ClassRefTypeSignature)?.fullyQualifiedClassName == "java.util.Map"
        }

    private val TypeSignature.mapTypeParameters: Pair<ClassRefTypeSignature, ClassRefTypeSignature>
        get() {
            val mapRefTypeSignature = this as ClassRefTypeSignature
            val keyRefTypeSignature = mapRefTypeSignature.typeArguments[0].typeSignature as ClassRefTypeSignature
            val valueRefTypeSignature = mapRefTypeSignature.typeArguments[1].typeSignature as ClassRefTypeSignature
            return Pair(keyRefTypeSignature, valueRefTypeSignature)
        }

    private val TypeSignature.asSerializer: String
        get() {
            return when (this) {
                is BaseTypeSignature -> when (this.type) {
                    Byte::class.javaPrimitiveType -> "kotlin.Byte._codegen_serializer()"
                    Char::class.javaPrimitiveType -> "kotlin.Char._codegen_serializer()"
                    Double::class.javaPrimitiveType -> "kotlin.Double._codegen_serializer()"
                    Float::class.javaPrimitiveType -> "kotlin.Float._codegen_serializer()"
                    Int::class.javaPrimitiveType -> "kotlin.Int._codegen_serializer()"
                    Long::class.javaPrimitiveType -> "kotlin.Long._codegen_serializer()"
                    Short::class.javaPrimitiveType -> "kotlin.Short._codegen_serializer()"
                    Boolean::class.javaPrimitiveType -> "kotlin.Boolean._codegen_serializer()"
                    Void::class.javaPrimitiveType -> "kotlin.Unit._codegen_serializer()"
                    else -> throw RuntimeException()
                }
                is ClassRefTypeSignature -> when (this.fullyQualifiedClassName) {
                    "java.lang.Byte" -> "kotlin.Byte._codegen_serializer()"
                    "java.lang.Char" -> "kotlin.Char._codegen_serializer()"
                    "java.lang.Double" -> "kotlin.Double._codegen_serializer()"
                    "java.lang.Float" -> "kotlin.Float._codegen_serializer()"
                    "java.lang.Integer" -> "kotlin.Int._codegen_serializer()"
                    "java.lang.Long" -> "kotlin.Long._codegen_serializer()"
                    "java.lang.Short" -> "kotlin.Short._codegen_serializer()"
                    "java.lang.Boolean" -> "kotlin.Boolean._codegen_serializer()"
                    "java.lang.Void" -> "kotlin.Unit._codegen_serializer()"
                    "java.lang.String" -> "kotlin.String._codegen_serializer()"
                    "java.util.List" -> "kotlinx.serialization.builtins.ListSerializer(${listTypeParameter.asSerializer})"
                    "java.util.Map" -> {
                        val (key, value) = mapTypeParameters
                        "kotlinx.serialization.builtins.MapSerializer(${key.asSerializer}, ${value.asSerializer})"
                    }
                    else -> "${asString}.serializer()"
                }
                else -> throw RuntimeException()
            }
        }

    private val TypeSignature.asString: String
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
                    "java.util.List" -> "kotlin.collections.List<${this.listTypeParameter.asString}>"
                    "java.util.Map" -> {
                        val (key, value) = this.mapTypeParameters
                        "kotlin.collections.Map<${key.asString}, ${value.asString}>"
                    }
                    else -> fullyQualifiedClassName
                }
                else -> throw RuntimeException()
            }
        }
}