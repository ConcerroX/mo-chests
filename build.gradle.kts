import net.neoforged.moddevgradle.dsl.RunModel
import org.slf4j.event.Level

plugins {
    id("idea")
    id("maven-publish")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.modDevGradle)
}

val modId = "example"
val modName = "Example"
version = "1.0.0"
group = "concerrox.$modId"
base.archivesName = "$modId-neoforge-${libs.versions.minecraft.get()}"

java.toolchain.languageVersion = JavaLanguageVersion.of(21)
sourceSets.main.get().resources { srcDir("src/generated/resources") }

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

neoForge {

    version = libs.versions.neoForge.get()

    parchment {
        minecraftVersion = libs.versions.minecraft.get()
        mappingsVersion = libs.versions.parchment.get()
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        create("client", Action<RunModel> {
            client()
        })
        create("server", Action<RunModel> {
            server()
        })
        create("data", Action<RunModel> {
            data()
            programArguments.addAll(
                "--mod", modId, "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
            )
        })
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("terminal.ansi", "true")
            logLevel = Level.DEBUG
        }
    }

}

val localRuntime by configurations.creating
configurations {
    runtimeClasspath.get().extendsFrom(localRuntime)
}

repositories {
    maven("https://thedarkcolour.github.io/KotlinForForge") // Kotlin for Forge
    maven("https://maven.terraformersmc.com") // EMI
    maven("https://api.modrinth.com/maven") // Kotlin for Forge (Runtime)
}

dependencies {
    jarJar(libs.kotlinForForge.runtime)
    localRuntime(libs.kotlinForForge.neoForge)
    localRuntime(libs.emi.neoForge)
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version" to libs.versions.minecraft.get(),
        "minecraft_version_range" to "[${libs.versions.minecraft.get()},)",
        "neo_version" to "[21.1,)",
        "loader_version_range" to "[4,)",
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to "MIT",
        "mod_version" to version,
        "mod_authors" to "ConcerroX",
        "mod_description" to "Lorem ipsum dolor amet. ",
        "kff_version_range" to "[${libs.kotlinForForge.neoForge.get().version},)",
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
    filesMatching("$modId.mixins.json") { expand(replaceProperties) }
}

sourceSets.main.get().resources.srcDir(generateModMetadata)
neoForge.ideSyncTask(generateModMetadata)

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modId
        }
    }
    repositories {
        maven {
            url = uri(File(project.projectDir, "repo"))
        }
    }
}

tasks.build {
    dependsOn(tasks.publishToMavenLocal)
}