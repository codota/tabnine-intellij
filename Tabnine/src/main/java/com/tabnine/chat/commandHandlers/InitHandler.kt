import com.google.gson.Gson
import com.google.gson.JsonElement
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.Project
import com.tabnine.chat.commandHandlers.ChatMessageHandler
import java.awt.Color
import javax.swing.UIManager

data class InitPayload(
    private val ide: String,
    private val isDarkTheme: Boolean,
    private val colors: MutableMap<String, String>
)

class InitHandler(gson: Gson) : ChatMessageHandler<Unit, InitPayload>(gson) {
    override fun handle(payload: Unit?, project: Project): InitPayload {
        val colorPalette = readColorPalette()
        val isDarkTheme = EditorColorsManager.getInstance().isDarkEditor

        return InitPayload("ij", isDarkTheme, colorPalette)
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
