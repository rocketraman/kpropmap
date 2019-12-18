import org.jetbrains.dokka.gradle.DokkaTask

plugins {
  kotlin("jvm") version "1.3.61"
  id("org.jetbrains.dokka") version "0.10.0"
  `maven-publish`
}

group = "org.kpropmap"
version = "0.0.2"

repositories {
  jcenter()
}

dependencies {
  implementation(kotlin("stdlib", "1.3.61"))
  implementation(kotlin("reflect", "1.3.61"))

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
  classifier = "javadoc"
  // dependsOn(dokka) not needed; dependency automatically inferred by from(dokka)
  from(dokka)
}

// Create sources Jar from main kotlin sources
val sourcesJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles sources JAR"
  classifier = "sources"
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
/*
publishing {
  publications {
    create("default", MavenPublication::class.java) {
      from(components["java"])
      artifact(sourcesJar)
      artifact(dokkaJar)
    }
  }
  repositories {
    maven {
      url = uri("$buildDir/repository")
    }
  }
}
*/
