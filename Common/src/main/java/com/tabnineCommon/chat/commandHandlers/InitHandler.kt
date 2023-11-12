import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.chat.commandHandlers.utils.getServerUrl

data class InitPayload(
    private val ide: String,
    private val isDarkTheme: Boolean,
    private val colors: MutableMap<String, String>,
    private val fontSize: Int,
    private val isTelemetryEnabled: Boolean,
    private val serverUrl: String?,
)

class InitHandler(gson: Gson) : ChatMessageHandler<Unit, InitPayload>(gson) {
    override fun handle(payload: Unit?, project: Project): InitPayload {
        return InitPayload(
            "ij",
            EditorColorsManager.getInstance().isDarkEditor,
            readColorPalette(),
            EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.PLAIN).size,
            isTelemetryEnabled(),
            getServerUrl(),
        )
    }

    private fun isTelemetryEnabled(): Boolean {
        return CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA)
    }

    private fun readColorPalette(): MutableMap<String, String> {
        val colorPalette = mutableMapOf<String, String>()
        colorPalette["DefaultTabs.background"] = Integer.toHexString(JBUI.CurrentTheme.DefaultTabs.background().rgb)
        return colorPalette
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}
