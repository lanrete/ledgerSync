import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
}

group = "me.lanrete"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jcenter.bintray.com/")
    }
}

dependencies {
    testImplementation(kotlin("test-junit"));
    implementation(group = "khttp", name = "khttp", version = "1.0.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

// https://mvnrepository.com/artifact/khttp/khttp
