plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.wtx"
version = "1.1.2"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/Java"))
        }
        resources {
            setSrcDirs(listOf("src/main/resources"))
        }
    }
}

dependencies {
    implementation("com.googlecode.juniversalchardet:juniversalchardet:1.0.3")
    implementation("cn.hutool:hutool-all:5.8.40")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.34")
    intellijPlatform {
        create("IC", "2025.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginVerification {
        ides {
            ide("IC", "2025.1")
            ide("IC", "2024.3")
            ide("IU", "2025.1")
            ide("IU", "2024.3")
            ide("IU", "2023.1")

        }
    }

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }

        changeNotes = """
      Initial version
    """.trimIndent()
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    buildSearchableOptions {
        enabled = false
    }

    named("prepareJarSearchableOptions") {
        enabled = false
    }
}
