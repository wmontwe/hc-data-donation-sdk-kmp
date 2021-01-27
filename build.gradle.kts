buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath(Dependency.GradlePlugin.kotlin)
        classpath(Dependency.GradlePlugin.android)
        classpath(Dependency.GradlePlugin.sqlDelight)
        classpath(Dependency.GradlePlugin.cocoapodsext)
        classpath(Dependency.kotlin.serialization)
        classpath(kotlin("gradle-plugin", Version.kotlin))
    }
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version Version.kotlinGradlePlugin apply false
    id("org.jetbrains.kotlin.plugin.serialization") version Version.kotlinGradlePlugin apply false

    // https://github.com/ben-manes/gradle-versions-plugin
    id("com.github.ben-manes.versions") version "0.28.0"
}

allprojects {
    repositories {
        mavenCentral()
        google()
        jcenter()

        maven("https://kotlin.bintray.com/kotlin")
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://jitpack.io")
        maven(url = "https://dl.bintray.com/touchlabpublic/kotlin")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://dl.bintray.com/korlibs/korlibs")
        maven("https://raw.github.com/d4l-data4life/maven-repository/main/features")
        maven("https://raw.github.com/d4l-data4life/maven-repository/main/snapshots")
        maven("https://raw.github.com/d4l-data4life/maven-repository/main/releases")
    }
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "6.5"
    distributionType = Wrapper.DistributionType.ALL
}
