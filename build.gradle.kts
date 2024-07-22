import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.10.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.coolrequest.tool.views.coder"
version = "v1.0.2"

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public/")
    mavenCentral()
}
intellij {
    version.set("2022.1")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.java","org.jetbrains.plugins.yaml"))
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(fileTree("/libs"))
    // https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2
    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
        options.encoding = "UTF-8"
    }

//    withType<Jar> {
//        from(configurations.runtimeClasspath.get().filter {
//            it.name.contains("log4j")
//        }.map {
//            if (it.isDirectory) it else zipTree(it)
//        })
//    }
    withType<ShadowJar>{
        relocate("com.alibaba.fastjson2","dev.coolrequest.shadow.com.alibaba.fastjson2")
        dependencies {
            exclude(dependency("com.jetbrains.*:.*:.*"))
            exclude(dependency("org.jetbrains.*:.*:.*"))
        }
    }

    withType<JavaExec> {
        jvmArgs("-Dfile.encoding=UTF-8")
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

