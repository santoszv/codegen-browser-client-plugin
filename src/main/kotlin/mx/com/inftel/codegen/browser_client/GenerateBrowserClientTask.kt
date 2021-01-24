@file:Suppress("DuplicatedCode")

package mx.com.inftel.codegen.browser_client

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassRefTypeSignature
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.lang.reflect.Modifier

open class GenerateBrowserClientTask : DefaultTask() {

    @get:Internal
    val classpath: Property<String> = project.objects.property(String::class.java)

    @get:Internal
    val output: Property<String> = project.objects.property(String::class.java)

    init {
        group = groupName
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun execute() {
        ClassGraph()
            .overrideClasspath(classpath.get())
            .enableClassInfo()
            .enableFieldInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()
            .use { result ->
                val outputDir = project.file(output.get())
                val resources = result.getClassesWithAnnotation("mx.com.inftel.codegen.rest.BrowserClient").filter { classInfo ->
                    Modifier.isPublic(classInfo.modifiers)
                            && !Modifier.isStatic(classInfo.modifiers)
                            && !Modifier.isAbstract(classInfo.modifiers)
                }.map { classInfo ->
                    ResourceModel(classInfo)
                }
                for (resource in resources) {
                    resource.generateCode(outputDir)
                }
                //
                val representationsName = mutableSetOf<String>()
                for (resource in resources) {
                    for (method in resource.methods) {
                        if (method.resultType.isList) {
                            val classInfo = method.resultType.listTypeParameter.classInfo
                            if (classInfo != null) {
                                representationsName.add(classInfo.name)
                            }
                        } else if (method.resultType.isMap) {
                            val (key, value) = method.resultType.mapTypeParameters
                            val keyClassInfo = key.classInfo
                            val valueClassInfo = value.classInfo
                            if (keyClassInfo != null) {
                                representationsName.add(keyClassInfo.name)
                            }
                            if (valueClassInfo != null) {
                                representationsName.add(valueClassInfo.name)
                            }
                        } else if (method.resultType is ClassRefTypeSignature) {
                            val classInfo = (method.resultType as ClassRefTypeSignature).classInfo
                            if (classInfo != null) {
                                representationsName.add(classInfo.name)
                            }
                        }
                    }
                }
                for (resource in resources) {
                    for (method in resource.methods) {
                        for (parameter in method.parameters) {
                            if (parameter.type.isList) {
                                val classInfo = parameter.type.listTypeParameter.classInfo
                                if (classInfo != null) {
                                    representationsName.add(classInfo.name)
                                }
                            } else if (parameter.type is ClassRefTypeSignature) {
                                val classInfo = (parameter.type as ClassRefTypeSignature).classInfo
                                if (classInfo != null) {
                                    representationsName.add(classInfo.name)
                                }
                            }
                        }
                    }
                }
                //
                val representations = representationsName.mapNotNull { name ->
                    val classInfo = result.getClassInfo(name)
                    if (classInfo != null) {
                        RepresentationModel(classInfo)
                    } else {
                        null
                    }
                }
                for (representation in representations) {
                    representation.generateCode(outputDir)
                }
                //
                val enumerationsName = mutableSetOf<String>()
                for (resource in resources) {
                    for (method in resource.methods) {
                        if (method.resultType.isEnum) {
                            val classInfo = (method.resultType as ClassRefTypeSignature).classInfo
                            if (classInfo != null) {
                                enumerationsName.add(classInfo.name)
                            }
                        }
                    }
                }
                for (resource in resources) {
                    for (method in resource.methods) {
                        for (parameter in method.parameters) {
                            if (parameter.type.isEnum) {
                                val classInfo = (parameter.type as ClassRefTypeSignature).classInfo
                                if (classInfo != null) {
                                    enumerationsName.add(classInfo.name)
                                }
                            }
                        }
                    }
                }
                for (representation in representations) {
                    for (property in representation.properties) {
                        if (property.type.isEnum) {
                            val classInfo = (property.type as ClassRefTypeSignature).classInfo
                            if (classInfo != null) {
                                enumerationsName.add(classInfo.name)
                            }
                        }
                    }
                }
                //
                val enumerations = enumerationsName.mapNotNull { name ->
                    val classInfo = result.getClassInfo(name)
                    if (classInfo != null) {
                        EnumerationModel(classInfo)
                    } else {
                        null
                    }
                }
                for (enumeration in enumerations) {
                    enumeration.generateCode(outputDir)
                }
            }
    }
}