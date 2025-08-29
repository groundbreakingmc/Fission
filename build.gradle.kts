plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.3"
}

group = "com.github.groundbreakingmc"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {

    // ANNOTATIONS

    // https://github.com/JetBrains/java-annotations
    implementation("org.jetbrains:annotations:26.0.2")

    // TESTS

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-core
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    // https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-generator-annprocess
    testImplementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.test {
    useJUnitPlatform()
}