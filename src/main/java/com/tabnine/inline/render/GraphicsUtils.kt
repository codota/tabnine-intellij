package com.tabnine.inline.render

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.ui.JBColor
import com.tabnine.userSettings.AppSettingsState
import java.awt.Color
import java.awt.Font
import java.awt.font.TextAttribute
import kotlin.math.abs
import kotlin.math.sqrt

object GraphicsUtils {
    fun getFont(editor: Editor, deprecated: Boolean): Font {
        val font = editor.colorsScheme.getFont(EditorFontType.ITALIC)
        if (!deprecated) {
            return font
        }
        val attributes: MutableMap<TextAttribute, Any?> = HashMap(font.attributes)
        attributes[TextAttribute.STRIKETHROUGH] = TextAttribute.STRIKETHROUGH_ON
        return Font(attributes)
    }

    val color: Color
        get() {
            return Color(AppSettingsState.instance.inlineHintColor)
        }

    val niceContrastColor: Color
        get() {
            val averageBrightness = (getBrightness(JBColor.background()) + getBrightness(JBColor.foreground())) / 2.0
            var currentResult = Color.lightGray
            var bestResult = currentResult
            var distance = Double.MAX_VALUE
            var currentBrightness = getBrightness(currentResult)
            val minBrightness = getBrightness(Color.darkGray)

            while (currentBrightness > minBrightness) {
                if (abs(currentBrightness - averageBrightness) < distance) {
                    distance = abs(currentBrightness - averageBrightness)
                    bestResult = currentResult
                }
                currentResult = currentResult.darker()
                currentBrightness = getBrightness(currentResult)
            }
            return bestResult
        }

    private fun getBrightness(color: Color): Double {
        return sqrt(
            (color.red * color.red * 0.241) +
                (color.green * color.green * 0.691) +
                (color.blue * color.blue * 0.068)
        )
    }
}
