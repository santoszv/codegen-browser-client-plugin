group = "mx.com.inftel.codegen"
version = "1.0.0"

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.3.72"
    `maven-publish`
    signing
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

val fakeJavadoc by tasks.registering(Jar::class) {
    archiveBaseName.set("${project.name}-fake")
    archiveClassifier.set("javadoc")
    from(file("$projectDir/files/README"))
}

afterEvaluate {
    publishing {
        publications.withType<MavenPublication> {
            pom {
                name.set("${project.group}:${project.name}")
                description.set("Codegen Browser Client Plugin")
                url.set("https://github.com/santoszv/codegen-browser-client-plugin")
                inceptionYear.set("2021")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("santoszv")
                        name.set("Santos Zatarain Vera")
                        email.set("santoszv@inftel.com.mx")
                        url.set("https://www.inftel.com.mx")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/santoszv/codegen-browser-client-plugin")
                    developerConnection.set("scm:git:https://github.com/santoszv/codegen-browser-client-plugin")
                    url.set("https://github.com/santoszv/codegen-browser-client-plugin")
                }
            }

            signing.sign(this)
        }

        publications.named<MavenPublication>("pluginMaven") {
            artifact(fakeJavadoc)
        }

        repositories {
            maven {
                setUrl(file("$projectDir/build/repo"))
            }
        }
    }
}

signing {
    useGpgCmd()
}