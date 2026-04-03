plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
}

version = property("mod_version") as String
group = property("maven_group") as String

base {
    archivesName.set(property("archives_base_name") as String)
}

val cobblemonJar: File = listOf(
    file("../../production_srv/_/mods/Cobblemon-fabric-1.7.3+1.21.1.jar"),
    file("../production_srv/_/mods/Cobblemon-fabric-1.7.3+1.21.1.jar")
).firstOrNull { it.exists() }
    ?: error("Missing Cobblemon jar. Place Cobblemon-fabric-1.7.3+1.21.1.jar in the mods folder.")

repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    modCompileOnly(files(cobblemonJar))
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
