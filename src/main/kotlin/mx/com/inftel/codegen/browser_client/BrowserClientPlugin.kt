package mx.com.inftel.codegen.browser_client

import org.gradle.api.Plugin
import org.gradle.api.Project

open class BrowserClientPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks.register(taskGenerateName, GenerateBrowserClientTask::class.java)
    }
}