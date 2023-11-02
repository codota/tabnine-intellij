import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.tabnineCommon.capabilities.CapabilitiesService
import com.tabnineCommon.capabilities.Capability
import com.tabnineCommon.chat.commandHandlers.ChatMessageHandler
import com.tabnineCommon.chat.commandHandlers.utils.getServerUrl
import java.awt.Color
import javax.swing.UIManager

val COLOR_KEYS = arrayOf("DefaultTabs.background")

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
        return InitPayload(
            "ij",
            true,
            mutableMapOf(),
            14,
            isTelemetryEnabled(),
            getServerUrl()
        )
    }

    private fun isTelemetryEnabled(): Boolean {
        return CapabilitiesService.getInstance().isCapabilityEnabled(Capability.ALPHA)
    }

    private fun readColorPalette(): MutableMap<String, String> {
        val colorPalette = mutableMapOf<String, String>()
        for (key in COLOR_KEYS) {
            val value = UIManager.get(key)
            if (value is Color) {
                colorPalette[key] = Integer.toHexString(value.rgb)
            } else {
                Logger.getInstance(InitHandler::class.java).warn("The background color is not a color: $key")
            }
        }
        return colorPalette
    }

    override fun deserializeRequest(data: JsonElement?) {
    }
}
