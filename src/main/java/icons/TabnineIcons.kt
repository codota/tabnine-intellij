package icons

import com.intellij.openapi.util.IconLoader

/**
 * This class must be under `icons` package in order to use it in `plugin.xml` as an `icon` attribute:
 * `icon="TabnineIcons.PrimaryIcon"`
 */
object TabnineIcons {
    @JvmField
    val PrimaryIcon = IconLoader.findIcon("/icons/tabnine-icon-13px.png")
}
