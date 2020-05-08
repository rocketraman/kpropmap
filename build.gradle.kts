import org.jetbrains.dokka.gradle.DokkaTask

plugins {
  kotlin("jvm") version "1.3.71"
  id("org.jetbrains.dokka") version "0.10.0"
  signing
  `maven-publish`
}

group = "com.github.rocketraman"
version = "0.0.2"

repositories {
  jcenter()
}

dependencies {
  implementation(kotlin("stdlib", "1.3.71"))
  implementation(kotlin("reflect", "1.3.71"))

  // testing
  testImplementation("com.natpryce:hamkrest:1.7.0.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}

// Configure existing Dokka task to output HTML to typical Javadoc directory
val dokka by tasks.getting(DokkaTask::class) {
  outputFormat = "html"
  outputDirectory = "$buildDir/javadoc"
}

// Create dokka Jar task from dokka task output
val dokkaJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles Kotlin docs with Dokka"
  archiveClassifier.set("javadoc")
  // dependsOn(dokka) not needed; dependency automatically inferred by from(dokka)
  from(dokka)
}

// Create sources Jar from main kotlin sources
val sourcesJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles sources JAR"
  archiveClassifier.set("sources")
  from(sourceSets["main"].allSource)
}

artifacts {
  add("archives", sourcesJar)
  add("archives", dokkaJar)
}

tasks {
  "test"(Test::class) {
    useJUnitPlatform()
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "com.github.rocketraman"
      from(components["java"])
      artifact(sourcesJar)
      artifact(dokkaJar)
      pom {
        name.set("kpropmap")
        description.set("Type-safe (ish) maps in Kotlin")
        url.set("https://github.com/rocketraman/kpropmap")
        licenses {
          license {
            name.set("MIT License")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("rocketraman")
            name.set("Raman Gupta")
            email.set("rocketraman@gmail.com")
          }
        }
        scm {
          connection.set("scm:git:git@github.com:rocketraman/kpropmap.git")
          developerConnection.set("scm:git:ssh://github.com:rocketraman/kpropmap.git")
          url.set("https://github.com/rocketraman/kpropmap")
        }
      }
    }
  }
  repositories {
    maven {
      name = "bintray"
      val bintrayUsername = "rocketraman"
      val bintrayRepoName = "maven"
      val bintrayPackageName = "kpropmap"
      setUrl("https://api.bintray.com/content/$bintrayUsername/$bintrayRepoName/$bintrayPackageName/${project.version};publish=0;override=1")
      credentials {
        username = project.findProperty("bintray_user") as String?
        password = project.findProperty("bintray_api_key") as String?
      }
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications["maven"])
}
