import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

plugins {
  id("idea")

  id("java")
  id("java-library")

  id("maven-publish")
  id("com.gradleup.shadow") version "8.3.0"
}

group = "net.cakemc.system"
version = "0.0.0-develop" // TODO: ... | add proper versioning

val repoProperties = Properties()
repoProperties.load(file("credentials.properties").inputStream())

val repoUsername: String = (repoProperties["username"] ?: System.getenv("REPOSITORY_USERNAME")).toString()
val repoPassword: String = (repoProperties["username"] ?: System.getenv("REPOSITORY_PASSWORD")).toString()

repositories {
  mavenLocal()
  mavenCentral()
  maven {
    name = "cakemc"
    url = URI.create("http://cakemc.net:8081/repository/maven-releases")
    credentials {
      username = repoUsername
      password = repoPassword
    }
    isAllowInsecureProtocol = true
  }
}

dependencies {}

tasks.withType<JavaCompile> {
  sourceCompatibility = JavaVersion.VERSION_21.toString()
  targetCompatibility = JavaVersion.VERSION_21.toString()
  options.encoding = StandardCharsets.UTF_8.toString()
}

tasks.withType<AbstractArchiveTask> {
  isReproducibleFileOrder = true
  isPreserveFileTimestamps = false
}
