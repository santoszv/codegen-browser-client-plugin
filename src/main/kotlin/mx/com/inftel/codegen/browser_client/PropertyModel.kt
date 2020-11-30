package mx.com.inftel.codegen.browser_client

import io.github.classgraph.MethodInfo

class PropertyModel {

    var propertyName = ""
    var capitalizedName = ""

    lateinit var propertyGetter: MethodInfo
    lateinit var propertySetter: MethodInfo

    var isNotNull = true

    val isGetterInitialized: Boolean
        get() = this::propertyGetter.isInitialized

    val isGeneratedCode: Boolean
        get() = this::propertyGetter.isInitialized && this::propertySetter.isInitialized
}