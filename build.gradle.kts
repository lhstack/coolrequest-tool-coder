plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.10.1"
}

group = "dev.coolrequest.tool.coder"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}
intellij {
    version.set("2022.1")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.java","org.intellij.groovy"))
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(fileTree("/libs"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }

    withType<JavaExec>{
        jvmArgs("-Dfile.encoding=UTF-8")
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

