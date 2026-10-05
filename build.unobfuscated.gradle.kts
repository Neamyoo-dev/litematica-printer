@file:Suppress("UnstableApiUsage")

import java.util.zip.ZipFile

plugins {
    id("mod-plugin")
    id("maven-publish")
    id("net.fabricmc.fabric-loom")
}

sourceSets {
    main {
        java.setSrcDirs(listOf(rootProject.file("src/main/java")))
        resources.setSrcDirs(listOf(rootProject.file("src/main/resources")))
    }
    test {
        java.setSrcDirs(listOf(rootProject.file("src/test/java")))
    }
}

version = fullProjectVersion
group = modMavenGroup
base {
    archivesName.set(modArchivesBaseName)
}

repositories {
    mavenLocal()
    maven("https://maven.fabricmc.net") { name = "FabricMC" }
    maven("https://maven.fallenbreath.me/releases") { name = "FallenBreath" }
    maven("https://api.modrinth.com/maven") { name = "Modrinth" }
    maven("https://www.cursemaven.com") { name = "CurseMaven" }
    maven("https://maven.terraformersmc.com/releases") { name = "TerraformersMC" }
    maven("https://maven.nucleoid.xyz") { name = "Nucleoid" }
    maven("https://masa.dy.fi/maven") { name = "Masa" }
    maven("https://masa.dy.fi/maven/sakura-ryoko") { name = "SakuraRyoko" }
    maven("https://maven.kyrptonaught.dev") { name = "Kyrptonaught" }
    maven("https://jitpack.io") { name = "Jitpack" }
}

// 锁定依赖版本防冲突
configurations.all {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:$fabricLoaderVersion")
        force("com.terraformersmc:modmenu:${prop("modmenu")}")
        force("maven.modrinth:malilib:${prop("malilib_dependency")}")
        force("maven.modrinth:litematica:${prop("litematica_dependency")}")
        force("maven.modrinth:tweakeroo:${prop("tweakeroo_dependency")}")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation("com.belerweb:pinyin4j:${prop("pinyin_version")}")?.let { include(it) }
    implementation("com.terraformersmc:modmenu:${prop("modmenu")}")

    // Use the public release's 26.2 jar so building does not require upstream GitHub Packages credentials.
    val remoteVersion = prop("remote_inventory_version").toString()
    val remoteJarName = "remote-inventory-next-mc26.2-$remoteVersion.jar"
    val remoteJar = rootProject.file("libs/$remoteJarName")
    if (!remoteJar.exists()) {
        val releaseJar = downloadDependencyMod(
            "https://github.com/BiliXWhite/remote-inventory-next/releases/download/$remoteVersion/remote-inventory-next-multi-$remoteVersion.jar"
        ) ?: throw GradleException("Unable to download Remote Inventory Next $remoteVersion")
        ZipFile(releaseJar).use { zip ->
            val entry = zip.getEntry("META-INF/jars/$remoteJarName")
                ?: throw GradleException("Remote Inventory Next release does not contain $remoteJarName")
            zip.getInputStream(entry).use { input -> remoteJar.outputStream().use { input.copyTo(it) } }
        }
    }
    implementation(files(remoteJar))

    // Masa
    implementation("fi.dy.masa.malilib:${prop("malilib")}:${prop("malilib_dependency")}")
    implementation("fi.dy.masa.litematica:${prop("litematica")}:${prop("litematica_dependency")}")
    implementation("fi.dy.masa.tweakeroo:${prop("tweakeroo")}:${prop("tweakeroo_dependency")}")

    // 快捷潜影盒
    val quickshulkerUrl = prop("quickshulker").toString()
    if (quickshulkerUrl.isNotEmpty()) {
        val quickshulkerFile = downloadDependencyMod(quickshulkerUrl)
        if (quickshulkerFile != null && quickshulkerFile.exists()) {
            implementation(files(quickshulkerFile))
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

loom {
    val commonVmArgs = listOf("-Dmixin.debug.export=true", "-Dmixin.debug.verbose=true", "-Dmixin.env.remapRefMap=true")
    val programArgs = listOf("--width", "1280", "--height", "720", "--username", "PrinterTest")
    runs {
        named("client") {
            generateRunConfig.set(true)
            jvmArguments.set(commonVmArgs)
            programArguments.set(programArgs)
            runDirectory.dir("../../run/client")
        }
    }
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }
    register<Copy>("buildAndCollect") {
        description = "Build and collect the jar to the root project build directory"
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod_version")}"))
        dependsOn("build")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modId
            version = modVersion
        }
    }
    repositories {
        mavenLocal()
        maven {
            url = uri("$rootDir/publish")
        }
    }
}
