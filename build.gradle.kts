val publishedId: String = "org.hyperledger.identus.vdr"

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    kotlin("jvm") version "2.1.21"
    `java-library`
    `maven-publish`
    signing
    application
    scala //https://docs.gradle.org/current/userguide/scala_plugin.html
}

repositories { mavenCentral() }

scala { scalaVersion =  "3.3.6" }
application { mainClass = "demo.App" }

group = publishedId
version = "0.1.0"


tasks.withType<ScalaCompile> {
    scalaCompileOptions.apply {        additionalParameters = listOf( "-feature" )}
}

// https://docs.gradle.org/current/samples/sample_building_scala_applications.html


//configurations {
//    create("scalaCompilerPlugin")
//}
//
//tasks.withType<ScalaCompile>().configureEach {
//    // Set compiler classpath to use Scala 3 compiler
//    val scalaCompilerCp = configurations["scalaCompilerPlugin"]
//    scalaClasspath = scalaCompilerCp
//
//    scalaCompileOptions.additionalParameters = listOf("-Xfatal-warnings")
//}


 tasks.named("compileScala").configure {
     dependsOn("compileKotlin")

     doFirst {
         println("Adding Kotlin output to Scala classpath")
         val kotlinOutputDir = tasks.named("compileKotlin").get().outputs.files

         val scalaCompileTask = this as ScalaCompile
         scalaCompileTask.classpath = scalaCompileTask.classpath.plus(kotlinOutputDir)
     }
 }


dependencies {

    // Kotlin
    // implementation(kotlin("stdlib"))

    // Scala
    // implementation("org.scala-lang:scala3-library_3:3.3.6")
    // add("scalaCompilerPlugin", "org.scala-lang:scala3-compiler_3:3.3.6")

    implementation(files("/Users/fabio/.ivy2/local/app.fmgp/did_3/0.1.0-M26+46-56f47b23+20250706-2122-SNAPSHOT/jars/did_3.jar"))
    implementation(files("/Users/fabio/.ivy2/local/app.fmgp/did-method-prism_3/0.1.0-M26+46-56f47b23+20250706-2122-SNAPSHOT/jars/did-method-prism_3.jar"))
    implementation(files("/Users/fabio/.ivy2/local/app.fmgp/multiformats_3/0.1.0-M26+46-56f47b23+20250706-2122-SNAPSHOT/jars/multiformats_3.jar"))
    implementation("com.bloxbean.cardano:cardano-client-backend-blockfrost:0.6.4")
    implementation("com.bloxbean.cardano:cardano-client-lib:0.6.4")
    // implementation("com.github.poslegm:munit-zio_3:0.4.0")
    implementation("com.google.crypto.tink:tink:1.17.0")
    implementation("com.google.protobuf:protobuf-java:4.29.5:")
    implementation("com.nimbusds:nimbus-jose-jwt:10.3")
    implementation("com.thesamet.scalapb:scalapb-runtime_3:0.11.17")
    implementation("dev.zio:zio-http_3:3.3.3")
    implementation("io.bullet:borer-core_3:1.16.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.80")
    implementation("org.bouncycastle:bcprov-jdk18on:1.80")
    implementation("org.hyperledger.identus.apollo:apollo-jvm:1.4.5")
    // implementation("pkg:maven/org.scalameta/munit_3@1.1.1")



    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.zaxxer:HikariCP:6.3.0")
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.postgresql:postgresql:42.7.3")
    testImplementation("org.testcontainers:postgresql:1.19.6")
    testImplementation("org.testcontainers:junit-jupiter:1.19.6")
    // testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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