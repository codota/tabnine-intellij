package com.tabnine.binary.fetch

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.tabnine.general.StaticConfig
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

val ONCE = AtomicBoolean(false)
val targetDir: Path = Paths.get(
    System.getProperty(StaticConfig.USER_HOME_PATH_PROPERTY),
    StaticConfig.TABNINE_FOLDER_NAME,
    "TabnineEnterprise",
    Executables::class.java.getResource("/binaries/version").openStream().reader().readText()
)

object Executables {
    object Names {
        val tabnine = "TabNine".exe()
        val deep = "TabNine-deep-cloud".exe()
        val watchDog = "WD-TabNine".exe()
    }

    object Bundled {
        private val tabnine: URL = binariesPathUrl(Names.tabnine)
        private val deep: URL = binariesPathUrl(Names.deep)
        private val watchDog = binariesPathUrl(Names.watchDog)

        fun toList() = listOf(tabnine, deep, watchDog)
    }

    object Target {
        val tabnine = File(targetDir.toFile(), Names.tabnine)
        private val deep = File(targetDir.toFile(), Names.deep)
        private val watchDog = File(targetDir.toFile(), Names.watchDog)

        fun toList() = listOf(tabnine, deep, watchDog)
    }
}

fun tabninePath(): Path {
    copyOnce()
    return Executables.Target.tabnine.toPath()
}

private fun copyOnce() {
    if (ONCE.get()) return

    Logger.getInstance(Executables.javaClass).info("Copying tabnine binaries to $targetDir")
    targetDir.toFile().mkdirs()

    val bundles = Executables.Bundled.toList()
    val targets = Executables.Target.toList()

    bundles.zip(targets).forEach { (bundle, target) ->
        if (!target.exists()) {
            Files.copy(
                bundle.openStream(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }

    targets.forEach { it.setExecutable(true) }

    ONCE.set(true)
}

fun String.exe() = if (SystemInfo.isWindows) "$this.exe" else this

fun binariesPathUrl(executable: String) =
    Executables::class.java.getResource("/binaries/${StaticConfig.TARGET_NAME}/$executable")!!
