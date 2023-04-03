package com.tabnine.userSettings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import com.tabnine.config.Config
import com.tabnine.general.DependencyContainer
import com.tabnine.general.StaticConfig
import com.tabnine.inline.DebounceUtils.isFixedDebounceConfigured
import org.jdesktop.swingx.JXTextField
import java.awt.Color
import javax.swing.JColorChooser
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Supports creating and managing a [JPanel] for the Settings Dialog.
 */
class AppSettingsComponent {
    val panel: JPanel
    private val suggestionsModeService = DependencyContainer.instanceOfSuggestionsModeService()
    private val logFilePathComponent = JBTextField()
    private val logLevelComponent = JBTextField()
    private val debounceTimeComponent = JBTextField()
    private val colorChooser = JColorChooser()
    private val useDefaultColorCheckbox = JBCheckBox("Use Default Color")
    private val colorChooserLabel = JBLabel("Inline Hint Color:", UIUtil.ComponentStyle.LARGE)
    private val autoImportCheckbox =
        JBCheckBox("Enable auto-importing packages when selecting Tabnine suggestions", true)
    private val binariesFolderOverrideComponent = JXTextField(StaticConfig.getDefaultBaseDirectory().toString())
    private val cloud2UrlComponent = JBTextField()
    private val useIJProxySettingsCheckBox = JBCheckBox("Use proxy settings for Tabnine (requires restart)", true)

    val preferredFocusedComponent: JComponent
        get() = colorChooser

    var useDefaultColor: Boolean
        get() = useDefaultColorCheckbox.isSelected
        set(value) {
            useDefaultColorCheckbox.isSelected = value
        }
    var chosenColor: Int
        get() = colorChooser.color.rgb
        set(colorRGB) {
            colorChooser.color = Color(colorRGB)
        }
    var logFilePath: String
        get() = logFilePathComponent.text
        set(value) {
            logFilePathComponent.text = value
        }
    var logLevel: String
        get() = logLevelComponent.text
        set(value) {
            logLevelComponent.text = value
        }
    var debounceTime: String
        get() = debounceTimeComponent.text
        set(value) {
            debounceTimeComponent.text = value
        }
    var autoImportEnabled: Boolean
        get() = autoImportCheckbox.isSelected
        set(value) {
            autoImportCheckbox.isSelected = value
        }
    var binariesFolderOverride: String
        get() = binariesFolderOverrideComponent.text
        set(value) {
            binariesFolderOverrideComponent.text = value
            binariesFolderOverrideComponent.prompt = value
        }
    var cloud2Url: String
        get() = cloud2UrlComponent.text
        set(value) {
            cloud2UrlComponent.text = value
        }
    var useIJProxySettings: Boolean
        get() = useIJProxySettingsCheckBox.isSelected
        set(value) {
            useIJProxySettingsCheckBox.isSelected = value
        }

    init {
        if (!suggestionsModeService.getSuggestionMode().isInlineEnabled) {
            colorChooser.isEnabled = false
            useDefaultColorCheckbox.isEnabled = false
            colorChooserLabel.isEnabled = false
            debounceTimeComponent.isEnabled = false
        }

        val panelBuilder = FormBuilder.createFormBuilder()
            .addLabeledComponent("Log File Path (requires restart): ", logFilePathComponent, 1, false)
            .addLabeledComponent("Log level (requires restart): ", logLevelComponent, 1, false)

        if (Config.IS_ON_PREM) {
            panelBuilder.addLabeledComponent("Tabnine Enterprise URL (requires restart): ", cloud2UrlComponent, 1, false)
        }
        if (!isFixedDebounceConfigured()) {
            panelBuilder
                .addLabeledComponent(
                    "Delay to suggestion preview ms (requires restart): ",
                    debounceTimeComponent,
                    1,
                    false
                )
        }
        panelBuilder
            .addLabeledComponent(colorChooserLabel, colorChooser, 1, true)
            .addComponent(useDefaultColorCheckbox, 1)
            .addComponent(autoImportCheckbox, 1)
            .addComponent(useIJProxySettingsCheckBox, 1)
            .addLabeledComponent(
                "Binaries Location absolute path (requires restart): ",
                binariesFolderOverrideComponent,
                1,
                false
            )
            .addComponentFillVertically(JPanel(), 0)

        panel = panelBuilder.panel
    }
}
