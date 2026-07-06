plugins {
    java
    `maven-publish`
    signing
    id("com.gradleup.shadow") version "9.4.3"
    id("io.freefair.lombok") version "9.5.0"
}
group = "dev.badbird"
version="3.5.0"

val jarName = "GriefPreventionTP-${version}"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "jitpack-repo"
        url = uri("https://jitpack.io")
    }
    maven("https://jitpack.io")
    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.octomc.dev/public/")
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.TechFortress:GriefPrevention:16.18.4")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("org.geysermc.floodgate:api:2.2.0-SNAPSHOT")

    implementation("net.badbird5907:bLib-Bukkit:2.2.0-REL")
    implementation("net.octopvp:Commander-Bukkit:0.0.12-REL")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.3")
}

val targetJavaVersion = 25
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
        options.release.set(targetJavaVersion)
    }
}
tasks.withType<ProcessResources> {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}
tasks.create<Copy>("copyPlugin") {
    from("build/libs/GriefPreventionTP-$version.jar")
    into("run/plugins")
}
tasks.getByName("copyPlugin").dependsOn(tasks.getByName("shadowJar"))
tasks.create<JavaExec>("runDev") {
    standardInput = System.`in`
    classpath = files("run/paper.jar")
    workingDir = file("run")
    args = listOf("nogui")
}
tasks.getByName("runDev").dependsOn(tasks.getByName("copyPlugin"))
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}
val javaComponent: SoftwareComponent = components["java"]
tasks {
    shadowJar {
        archiveBaseName.set("GriefPreventionTP")
        archiveClassifier.set("")

        relocate("net.badbird5907.blib", "dev.badbird.griefpreventiontp.relocate.blib")
        relocate("net.octopvp.commander", "dev.badbird.griefpreventiontp.relocate.commander")
        relocate("com.cryptomorin.xseries", "dev.badbird.griefpreventiontp.relocate.xseries")

        exclude("*.txt")
        exclude("*.md")
        exclude("*.md")
        exclude("LICENSE")
        exclude("AUTHORS")
    }
    build {
        dependsOn(shadowJar)
    }
}