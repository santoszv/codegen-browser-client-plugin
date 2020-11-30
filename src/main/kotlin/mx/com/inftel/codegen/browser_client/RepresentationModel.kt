package mx.com.inftel.codegen.browser_client

class RepresentationModel {

    var representationQualifiedName = ""
    val representationPackageName: String
        get() = representationQualifiedName.substringBeforeLast('.', "")
    val representationSimpleName: String
        get() = representationQualifiedName.substringAfterLast('.')

    val properties = mutableMapOf<String, PropertyModel>()
}