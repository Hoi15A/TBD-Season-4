import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import java.io.BufferedReader

val commitHash = Runtime
    .getRuntime()
    .exec(arrayOf("git", "rev-parse", "--short", "HEAD"))
    .let { process ->
        process.waitFor()
        val output = process.inputStream.use {
            it.bufferedReader().use(BufferedReader::readText)
        }
        process.destroy()
        output.trim()
    }

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.5"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0" // Generates plugin.yml based on the Gradle config
    id("com.apollographql.apollo3") version "3.8.2" // GraphQL
}

group = "net.tbdsmp"
version = "Build-$commitHash"
description = "Chicken Jockey!!!!"

bukkitPluginYaml {
    main = "SeasonFourPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors = listOf(
        "Austin Albrecht",
        "Byrt",
        "derNiklaas",
        "Sebiann"
    )
    apiVersion = "1.21.8"
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        javaParameters = true
    }
}

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "noxcrewMavenPublic"
        url = uri("https://maven.noxcrew.com/public")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-annotations:2.0.0")
    implementation("org.incendo:cloud-kotlin-extensions:2.0.0")
    kapt("org.incendo:cloud-annotations:2.0.0")

    implementation("org.spongepowered:configurate-yaml:4.2.0")
    implementation("org.spongepowered:configurate-extra-kotlin:4.2.0")
    implementation("fr.mrmicky:fastboard:2.1.5")

    implementation("com.noxcrew.interfaces:interfaces:2.0.1-SNAPSHOT")

    implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")
}

apollo {
    service("mcc") {
        packageName.set("net.tbdsmp.tbdseason4")
        schemaFile.set(file("src/main/graphql/schema.graphqls"))
        srcDir("src/main/graphql")
    }
}

tasks {
    compileJava {
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    shadowJar {
        val shadowPkg = "net.tbdsmp.tbdseason4.shade"

        relocate("org.incendo", "${shadowPkg}.org.incendo")
        relocate("org.spongepowered", "${shadowPkg}.org.spongepowered")
        relocate("fr.mrmicky", "${shadowPkg}.fr.mrmicky")

        mergeServiceFiles()
    }
}

