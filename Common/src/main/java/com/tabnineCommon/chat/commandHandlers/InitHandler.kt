import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.project.Project
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.config.Config
import com.tabnineCommon.general.StaticConfig
import java.awt.Color
import javax.swing.UIManager

data class InitPayload(
    private val ide: String,
    private val isDarkTheme: Boolean,
    private val colors: MutableMap<String, String>,
    private val fontSize: Int,
    private val isTelemetryEnabled: Boolean,
    private val serverUrl: String?
)

class InitHandler(gson: Gson) : ChatMessageHandler<Unit, InitPayload>(gson) {
    override fun handle(payload: Unit?, project: Project): InitPayload {
        val colorPalette = readColorPalette()
        val isDarkTheme = EditorColorsManager.getInstance().isDarkEditor
        val font = EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.PLAIN).size
        val serverUrl = if (Config.IS_SELF_HOSTED) {
            getServerUrl()
        } else {
            null
        }
        return InitPayload("ij", isDarkTheme, colorPalette, font, isTelemetryEnabled(), serverUrl)
    }

    private fun isTelemetryEnabled(): Boolean {
        return CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA)
    }

    private fun getServerUrl(): String? {
        return StaticConfig.getTabnineEnterpriseHost().orElse(null)
    }

    private fun readColorPalette(): MutableMap<String, String> {
        val colorPalette = mutableMapOf<String, String>()

        for (key in UIManager.getDefaults().keys()) {
            val value = UIManager.get(key)
            if (value is Color) {
                colorPalette[key as String] = Integer.toHexString(value.rgb)
            }
        }
        return colorPalette
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}
