plugins {
    id("java")
    id("me.champeau.jmh") version "0.7.3"
    id("maven-publish")
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

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name = "Fission"
                description = "A high-performance, zero-dependency library designed for blazing-fast file reading and character stream processing. Fission outperforms standard Java I/O by 20-100x in parsing operations while providing a clean, exception-free API."
                url = "https://github.com/groundbreakingmc/Fission"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://github.com/groundbreakingmc/Fission?tab=Apache-2.0-1-ov-file"
                    }
                }

                developers {
                    developer {
                        id = "GroundbreakingMC"
                        name = "Victor"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/groundbreakingmc/Fission.git"
                    developerConnection = "scm:git:ssh://git@github.com:groundbreakingmc/Fission.git"
                    url = "https://github.com/groundbreakingmc/Fission"
                }
            }
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.release = 17
    }
    withType<Jar> {
        manifest {
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = project.version
        }
    }
    withType<Javadoc> {
        options {
            encoding = "UTF-8"
            (this as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }
    }
}
