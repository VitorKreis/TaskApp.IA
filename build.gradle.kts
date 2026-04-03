// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
}

fun resolveAdbPath(rootDir: File): String {
    val localProps = Properties().apply {
        rootDir.resolve("local.properties").inputStream().use { load(it) }
    }

    val sdkDirRaw = localProps.getProperty("sdk.dir")
        ?: error("sdk.dir not found in local.properties")

    val sdkDir = sdkDirRaw.replace("\\\\", "\\")
    val adbName = if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
        "adb.exe"
    } else {
        "adb"
    }

    val adbFile = File(sdkDir, "platform-tools${File.separator}$adbName")
    check(adbFile.exists()) { "adb not found at ${adbFile.absolutePath}" }
    return adbFile.absolutePath
}

tasks.register("runDebug") {
    group = "application"
    description = "Instala o app debug e abre a MainActivity"
    dependsOn(":app:installDebug")

    doLast {
        val adbPath = resolveAdbPath(rootProject.projectDir)
        exec {
            commandLine(
                adbPath,
                "shell",
                "am",
                "start",
                "-n",
                "com.example.myapplication/.MainActivity"
            )
        }
    }
}

tasks.register("runFreshDebug") {
    group = "application"
    description = "Remove o app e reinstala debug para evitar erro de assinatura"

    doFirst {
        val adbPath = resolveAdbPath(rootProject.projectDir)
        exec {
            isIgnoreExitValue = true
            commandLine(adbPath, "uninstall", "com.example.myapplication")
        }
    }

    dependsOn("runDebug")
}