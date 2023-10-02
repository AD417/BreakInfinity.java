plugins {
    id("java-library")
    id("maven-publish")
}

group = "io.github.ad417"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Define your dependencies here
    implementation("org.jetbrains:annotations:24.0.0")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "io.github.ad417"
            artifactId = "BreakInfinity"
            version = "0.1.0"
        }
    }
    repositories {
        maven {
            name = "MyRepo" //  optional target repository name
            url = uri("http://my.org.server/repo/url")
            credentials {
                // TODO
            }
        }
    }
}