group = "mx.com.inftel.codegen"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("browserClient") {
            id = "mx.com.inftel.codegen.browser-client"
            implementationClass = "mx.com.inftel.codegen.browser_client.BrowserClientPlugin"
        }
    }
}

dependencies {
    // ClassGraph
    implementation("io.github.classgraph:classgraph:4.8.90")

    // Gradle
    compileOnly(gradleApi())

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        javaParameters = true
        jvmTarget = "1.8"
    }
}

publishing {
    repositories {
        maven {
            url = if (project.version.toString().endsWith("-SNAPSHOT")) {
                uri("https://nexus.inftelapps.com/repository/maven-snapshots/")
            } else {
                uri("https://nexus.inftelapps.com/repository/maven-releases/")
            }
            credentials {
                username = properties["inftel.nexus.username"]?.toString()
                password = properties["inftel.nexus.password"]?.toString()
            }
        }
    }
}