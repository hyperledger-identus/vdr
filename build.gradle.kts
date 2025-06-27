val publishedId: String = "org.hyperledger.identus.vdr"

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    kotlin("jvm") version "1.9.23"
    `java-library`
    `maven-publish`
    signing
}

group = publishedId
version = "0.1.0"

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
    jvmToolchain(21)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.withType<MavenPublication> {
        groupId = publishedId
        artifactId = project.name
        version = project.version.toString()
        pom {
            name.set("Identus VDR")
            description.set("VDR framework implementation for Identus")
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
                connection.set("scm:git:git://git@github.com/hyperledger-identus/vdr.git")
                developerConnection.set("scm:git:ssh://git@github.com/hyperledger-identus/vdr.git")
                url.set("https://github.com/hyperledger-identus/vdr")
            }
        }

        signing {
            useInMemoryPgpKeys(
                project.findProperty("signing.signingSecretKey") as String? ?: System.getenv("OSSRH_GPG_SECRET_KEY"),
                project.findProperty("signing.signingSecretKeyPassword") as String? ?: System.getenv("OSSRH_GPG_SECRET_KEY_PASSWORD")
            )
            sign(this@withType)
        }
    }

    repositories {
        mavenLocal()

        maven {
            name = "ossrh"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USER")
                password = findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASS")
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/releases/"))
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
        }
    }
}
