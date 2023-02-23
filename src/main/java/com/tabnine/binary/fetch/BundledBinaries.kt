package com.tabnine.binary.fetch

import com.intellij.openapi.util.SystemInfo
import com.tabnine.general.StaticConfig
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicBoolean

val targetDir: Path = Paths.get(System.getProperty(StaticConfig.USER_HOME_PATH_PROPERTY), StaticConfig.TABNINE_FOLDER_NAME, "TabnineEnterprise")

// might not be relevant
val version = "1.2.3" // yoni find how to get the right version from within the jar after the CI downloaded some version like 4.4.253
object Executables {
    object Names {
        val tabnine = "TabNine".exe()
        val deep = "TabNine-deep-cloud".exe()
        val watchDog = "WD-TabNine".exe()
    }
    object Bundled {
        val tabnine: URL = Executables::class.java.getResource("/binaries/${StaticConfig.TARGET_NAME}/${Names.tabnine}")!!
        val deep: URL = Executables::class.java.getResource("/binaries/${StaticConfig.TARGET_NAME}/${Names.deep}")!!
        val watdg: URL = Executables::class.java.getResource("/binaries/${StaticConfig.TARGET_NAME}/${Names.watchDog}")!!
    }
    object Target {
        val tabnine = targetDir.append(Names.tabnine)
        val deep = targetDir.append(Names.deep)
        val watchDog = targetDir.append(Names.watchDog)
    }
}

fun String.exe(): String {
    return if (SystemInfo.isWindows) "$this.exe" else this
}

fun Path.append(component: String): File {
    return File(this.toFile(), component)
}

fun tabninePath(): Path {
    copyOnce()
    return Executables.Target.tabnine.toPath()
}

val once = AtomicBoolean(false)

// will copy the plugin bundled binaries to the local directory path where they will be run from
// You can call this function multiple times and it will only copy the contents once
fun copyOnce() {
    if (once.get()) { return }
    once.compareAndSet(false, true)

    //  ~/.tabnine/4.4.229/aarch64-apple-darwin/
    // TABNINE_FOLDER_NAME => .tabnine
    // , StaticConfig.TARGET_NAME -> i686 / x86 / aarch64 ...
    // https://stackoverflow.com/questions/10308221/how-to-copy-file-inside-jar-to-outside-the-jar
    targetDir.toFile().mkdirs()

    Files.copy(Executables.Bundled.tabnine.openStream(), Executables.Target.tabnine.toPath(), StandardCopyOption.REPLACE_EXISTING)
    Files.copy(Executables.Bundled.deep.openStream(), Executables.Target.deep.toPath(), StandardCopyOption.REPLACE_EXISTING)
    Files.copy(Executables.Bundled.watdg.openStream(), Executables.Target.watchDog.toPath(), StandardCopyOption.REPLACE_EXISTING)

    Executables.Target.tabnine.setExecutable(true)
    Executables.Target.deep.setExecutable(true)
    Executables.Target.watchDog.setExecutable(true)
}
