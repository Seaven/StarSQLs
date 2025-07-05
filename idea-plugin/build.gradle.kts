plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "com.starsqls"
version = "1.0"

repositories {
  maven("https://maven.aliyun.com/repository/public/")
  maven("https://maven.aliyun.com/nexus/content/groups/public/")
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
  intellijPlatform {
    create("IC", "2024.3")
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

    // Add necessary plugin dependencies for compilation here, example:
    // bundledPlugin("com.intellij.java")
  }

  implementation(files("../core/target/starsqls-core-1.0.jar"))
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = "243"
    }
    changeNotes = file("../CHANGELOG.md").readText()
  }
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    options.release = 17
    options.encoding = "UTF-8"
  }
  val createOpenApiSourceJar by registering(Jar::class) {
     // Java sources
     from(sourceSets.main.get().java) {
       include("**/com/starsqls/idea/**/*.java")
       include("../core/src/main/java/com/starsqls/**/*.java")
     }
     destinationDirectory.set(layout.buildDirectory.dir("libs"))
     archiveClassifier.set("src")
   }

  buildPlugin {
    dependsOn(createOpenApiSourceJar)
    from(createOpenApiSourceJar) { into("lib/src") }
    // Include CHANGELOG.md in the plugin package
    from("../CHANGELOG.md") { into(".") }
  }
}
