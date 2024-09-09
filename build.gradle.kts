import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
version = "0.0.0-develop"

val repoProperties = Properties()
val repoFile = file("credentials.properties")
if (repoFile.exists())
  repoProperties.load(repoFile.inputStream())
val repoUsername: String = (repoProperties["username"] ?: System.getenv("REPOSITORY_USERNAME")).toString()
val repoPassword: String = (repoProperties["password"] ?: System.getenv("REPOSITORY_PASSWORD")).toString()

repositories {
  mavenLocal()
  mavenCentral()
  maven {
    name = "cakemc-nexus"
    url = URI.create("https://repo.cakemc.net/repository/maven-releases")
    credentials {
      username = repoUsername
      password = repoPassword
    }
    isAllowInsecureProtocol = true
  }
}

dependencies {}

val jdkVersion = JavaVersion.VERSION_21
val jdkVersionString = jdkVersion.toString()

java {
  toolchain.languageVersion = JavaLanguageVersion.of(jdkVersionString)
  withSourcesJar()
}

tasks.withType<JavaCompile> {
  sourceCompatibility = jdkVersionString
  targetCompatibility = jdkVersionString
  options.encoding = StandardCharsets.UTF_8.toString()
}

tasks.withType<Jar> {
  manifest.attributes["Main-Class"] = "net.cakemc.system.cloudflare.Cloudflare"
}

tasks.withType<AbstractArchiveTask> {
  isReproducibleFileOrder = true
  isPreserveFileTimestamps = false
}

tasks.withType<ShadowJar> {
  configurations = listOf(project.configurations.shadow.get())
  isZip64 = true
}

configurations.shadow { isTransitive = false }

publishing {
  publications {
    create<MavenPublication>("mavenCakeNexus") {
      from(components["java"])
      artifact(tasks.shadowJar)

      groupId = "net.cakemc.cloud"
      artifactId = "impl"
      version = version

      pom {
        name.set("CakeMc system cloudflare")
        description.set("The cloudflare-sync for our server.")
        url.set("https://github.com/CakeMC-Network/system-protocols")
        licenses {
          license {
            name.set("Apache-2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0")
          }
        }
        developers {
          developer {
            id.set("CakeMcNET")
            name.set("CakeMc")
          }
        }
        scm {
          connection = "scm:git:git://github.com/CakeMC-Network/weewoo.git"
          developerConnection = "scm:git:ssh://github.com/CakeMC-Network/weewoo.git"
          url = "github.com/CakeMC-Network/weewoo"
        }
      }
    }
  }
  repositories {
    maven {
      name = "cakemc-nexus"
      url = uri("https://repo.cakemc.net/")
      credentials {
        username = repoUsername
        password = repoPassword
      }
      isAllowInsecureProtocol = true
    }
  }
}
