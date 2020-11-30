package mx.com.inftel.codegen.browser_client

class ResourceModel {

    var resourceQualifiedName = ""
    val resourcePackageName: String
        get() = resourceQualifiedName.substringBeforeLast('.', "")
    val resourceSimpleName: String
        get() = resourceQualifiedName.substringAfterLast('.')

    var path = ""
    val methods = mutableMapOf<String, MethodModel>()
}