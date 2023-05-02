package integration.uninstall

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.tabnine.general.StaticConfig
import org.jdom.Element
import java.io.File
import java.util.Date

class PluginDescriptorMock(private val versionMock: String, private val pluginIdMock: PluginId = StaticConfig.TABNINE_PLUGIN_ID) : IdeaPluginDescriptor {
    override fun getPluginId(): PluginId {
        return pluginIdMock
    }

    override fun getPluginClassLoader(): ClassLoader {
        throw RuntimeException("test mock!")
    }

    override fun getPath(): File {
        throw RuntimeException("test mock!")
    }

    override fun getDescription(): String? {
        throw RuntimeException("test mock!")
    }

    override fun getChangeNotes(): String {
        throw RuntimeException("test mock!")
    }

    override fun getName(): String {
        throw RuntimeException("test mock!")
    }

    override fun getProductCode(): String? {
        throw RuntimeException("test mock!")
    }

    override fun getReleaseDate(): Date? {
        throw RuntimeException("test mock!")
    }

    override fun getReleaseVersion(): Int {
        throw RuntimeException("test mock!")
    }

    override fun getDependentPluginIds(): Array<PluginId> {
        throw RuntimeException("test mock!")
    }

    override fun getOptionalDependentPluginIds(): Array<PluginId> {
        throw RuntimeException("test mock!")
    }

    override fun getVendor(): String {
        throw RuntimeException("test mock!")
    }

    override fun getVersion(): String {
        return versionMock
    }

    override fun getResourceBundleBaseName(): String {
        throw RuntimeException("test mock!")
    }

    override fun getCategory(): String {
        throw RuntimeException("test mock!")
    }

    override fun getActionDescriptionElements(): MutableList<Element>? {
        throw RuntimeException("test mock!")
    }

    override fun getVendorEmail(): String {
        throw RuntimeException("test mock!")
    }

    override fun getVendorUrl(): String {
        throw RuntimeException("test mock!")
    }

    override fun getUrl(): String {
        throw RuntimeException("test mock!")
    }

    override fun getVendorLogoPath(): String {
        throw RuntimeException("test mock!")
    }

    override fun getUseIdeaClassLoader(): Boolean {
        throw RuntimeException("test mock!")
    }

    override fun getSinceBuild(): String {
        throw RuntimeException("test mock!")
    }

    override fun getUntilBuild(): String {
        throw RuntimeException("test mock!")
    }

    override fun allowBundledUpdate(): Boolean {
        throw RuntimeException("test mock!")
    }

    override fun isImplementationDetail(): Boolean {
        throw RuntimeException("test mock!")
    }

    override fun isEnabled(): Boolean {
        throw RuntimeException("test mock!")
    }

    override fun setEnabled(enabled: Boolean) {
        throw RuntimeException("test mock!")
    }
}
