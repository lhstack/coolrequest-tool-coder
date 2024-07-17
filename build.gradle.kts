plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "dev.coolrequest.tool.coder"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
intellij {
    version.set("2022.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("org.jetbrains.idea.maven","org.jetbrains.plugins.gradle"))
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(files("D:\\project\\java\\coolrequest-tool\\coolrequest-tool\\build\\libs\\coolrequest-tool.jar"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
    }

    patchPluginXml {
        sinceBuild.set("203")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

