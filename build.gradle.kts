val publishedId: String = "org.hyperledger.identus"
val projectDescription =
    "Identus VDR - A framework implementation for Verifiable Data Registry (VDR) for Identus Platform"

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    kotlin("jvm") version "1.9.23"
    `java-library`
    `maven-publish`
    signing
}

group = publishedId
version = project.findProperty("releaseVersion") ?: "0.1.0"
description = projectDescription

repositories { mavenCentral() }

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.zaxxer:HikariCP:6.3.0")
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.postgresql:postgresql:42.7.3")
    testImplementation("org.testcontainers:postgresql:1.19.6")
    testImplementation("org.testcontainers:junit-jupiter:1.19.6")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.13")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = publishedId
            artifactId = project.name
            version = project.version.toString()
            pom {
                name.set("Identus VDR")
                description.set(projectDescription)
                url.set("https://github.com/hyperledger-identus/vdr")
                organization {
                    name.set("Hyperledger")
                    url.set("https://www.hyperledger.org/")
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/hyperledger-identus/vdr")
                }
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("amagyar-iohk")
                        name.set("Allain Magyar")
                        email.set("allain.magyar@iohk.io")
                        organization.set("IOG")
                        roles.add("qc")
                    }
                    developer {
                        id.set("goncalo-frade-iohk")
                        name.set("Gonçalo Frade")
                        email.set("goncalo.frade@iohk.io")
                        organization.set("IOG")
                        roles.add("developer")
                    }
                    developer {
                        id.set("yshyn-iohk")
                        name.set("Yurii Shynbuiev")
                        email.set("yurii.shynbuiev@iohk.io")
                        organization.set("IOG")
                        roles.add("developer")
                    }
                }
                scm {
                    connection.set("scm:git:https://git@github.com/hyperledger-identus/vdr.git")
                    developerConnection.set("scm:git:https://git@github.com/hyperledger-identus/vdr.git")
                    url.set("https://github.com/hyperledger-identus/vdr")
                }
            }
        }
    }
}

val signingKey = project.findProperty("signing.signingSecretKey") as String?
    ?: System.getenv("OSSRH_GPG_SECRET_KEY")
val signingPassword = project.findProperty("signing.signingSecretKeyPassword") as String?
    ?: System.getenv("OSSRH_GPG_SECRET_KEY_PASSWORD")

if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
    signing {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
} else {
    logger.lifecycle("Signing is skipped because OSSRH_GPG_SECRET_KEY or OSSRH_GPG_SECRET_KEY_PASSWORD is not set.")
}

nexusPublishing {
    repositories {
        // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
        }
    }
}