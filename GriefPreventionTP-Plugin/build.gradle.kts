import proguard.gradle.ProGuardTask

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.2.1")
    }
}

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("dev.badbird.java-conventions")
}
group = "dev.badbird"

val jarName = "GriefPreventionTP-${version}"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "papermc-repo"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "jitpack-repo"
        url = uri("https://jitpack.io")
    }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://jitpack.io") }

}
val impl by configurations.creating
dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.github.TechFortress:GriefPrevention:16.18")

    impl("net.badbird5907:bLib-Bukkit:2.1.7-REL")
    impl("net.kyori:adventure-text-minimessage:4.10.1")
    impl("net.octopvp:Commander-Bukkit:0.0.3-REL")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    testCompileOnly("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    implementation("net.kyori:adventure-platform-bukkit:4.2.0")
    impl.dependencies.forEach {
        implementation(it)
    }
}
tasks {
    assemble {

    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveBaseName.set(jarName + "-original.jar")
    }
    /*
        obfuscate(type: ProGuardTask) {
        injars "build/libs/" + jarName + ".jar"
        outjars "build/libs/" + jarName + "-obf.jar"
        verbose

        //configuration(file("proguard-rules.pro"))
        configurationFiles.add(file("proguard-rules.pro"))
        configurationFiles.add(file("proguard-obf.pro"))

        //libraryjars "${System.getProperty("java.home")}/lib/rt.jar" //java 8
        libraryjars "libs"
    }
     */
    shadowJar {
        relocate("net.badbid5907.blib", "dev.badbird.griefpreventiontp.dependencies")
        relocate("net.octopvp", "dev.badbird.griefpreventiontp.dependencies")
        //relocate("net.kyori", "dev.badbird.griefpreventiontp.dependencies")
        //relocate("org", "dev.badbird.griefpreventiontp.dependencies")
        //relocate("javax", "dev.badbird.griefpreventiontp.dependencies.javax")
        //relocate("javaassist", "dev.badbird.griefpreventiontp.dependencies.javaassist")
        //relocate("com.google", "dev.badbird.griefpreventiontp.dependencies")
        archiveFileName.set(jarName + ".jar")
    }
    register<ProGuardTask>("obfuscate") {
        injars("build/libs/$jarName.jar")
        outjars("build/libs/$jarName-obf.jar")
        // verbose.set(true)

        // configuration(file("proguard-rules.pro"))
        configurationFiles.add(file("proguard-rules.pro"))
        configurationFiles.add(file("proguard-obf.pro"))

        // libraryjars("${System.getProperty("java.home")}/lib/rt.jar") //java 8
        libraryjars("libs")
    }
}
/*
val genDict by tasks.registering {
    val dict = file("names-v1.txt")
    val maxChars = 10
    val lines = 20000
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray()
    val out = PrintStream(dict)
    for (i in 0 until lines) {
        val name = StringBuilder()
        for (j in 0 until maxChars) {
            name.append(chars[(Math.random() * chars.size).toInt()])
        }
        //println(name.toString())
        out.println(name.toString())
    }
    out.close()
}
 */

val copyPlugin by tasks.registering(Copy::class) {
    from("build/libs/$jarName-obf.jar")
    into("run/plugins")
    dependsOn("shadowJar")
}

val runDev by tasks.registering(JavaExec::class) {
    classpath = files("run/paper.jar")
    //run the serverJar, main from MANIFEST.MF
    //args("nogui")
    workingDir = file("run")
}

tasks.named("obfuscate") {
    //  dependsOn(genDict)
}
tasks.named("runDev") {
    dependsOn(copyPlugin)
}
tasks.named("jar") {
    dependsOn("shadowJar")
    finalizedBy("obfuscate")
}
