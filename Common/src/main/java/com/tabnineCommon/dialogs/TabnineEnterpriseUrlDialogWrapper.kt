package com.tabnineCommon.dialogs

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.util.io.HttpRequests
import com.intellij.util.ui.FormBuilder
import com.tabnineCommon.general.Utils
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.runAsync
import java.awt.BorderLayout
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

data class ValidationState(val text: String, val validationInfo: ValidationInfo)

class TabnineEnterpriseUrlDialogWrapper(existingUrl: String? = null) : DialogWrapper(true) {
    private var validationState: ValidationState? = null

    private val input = JBTextField(existingUrl)
    val inputData: String
        get() = input.text

    init {
        title = "Tabnine Enterprise URL"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val title = JTextArea(
            """
            Please insert your Tabnine Enterprise URL.
            Without entering the right URL, Tabnine won't start.
            You can also change it from Tabnine settings by clicking the Tabnine icon.
            """.trimIndent()
        )
        title.isEditable = false
        val panel = JPanel(BorderLayout())
        panel.add(FormBuilder().addLabeledComponent(title, input, 1, true).panel)
        return panel
    }

    override fun doValidate(): ValidationInfo? {
        val url = Utils.trimEndSlashAndWhitespace(input.text)
        if (url.isNullOrBlank()) {
            validationState = null
            return null
        }
        val validationInfo = getValidationInfoFor(url)
        validationInfo?.let {
            validationState = ValidationState(url, it)
        }

        return validationInfo ?: super.doValidate()
    }

    private fun getValidationInfoFor(cloud2Url: String): ValidationInfo? {
        if (validationState?.text == cloud2Url) {
            return validationState?.validationInfo
        }

        return try {
            // validate url string by trying to obtain a URI from it
            URL(cloud2Url).toURI()
            tryHealthRequest("$cloud2Url/health").blockingGet(1000)
        } catch (e: URISyntaxException) {
            invalidUrl(cloud2Url)
        } catch (e: MalformedURLException) {
            invalidUrl(cloud2Url)
        }
    }

    private fun tryHealthRequest(url: String): Promise<ValidationInfo?> {
        return runAsync {
            try {
                HttpRequests.request(url).connectTimeout(3000).readTimeout(3000).readString()
            } catch (e: HttpRequests.HttpStatusException) {
                return@runAsync ValidationInfo("Invalid URL: Health check failed - GET '$url' returned status code ${e.statusCode}")
            } catch (e: Throwable) {
                Logger.getInstance(javaClass).warn("Failed to validate url '$url'", e)
                return@runAsync ValidationInfo("Invalid URL: Health check failed")
            }
            return@runAsync null
        }
    }
}

private fun invalidUrl(url: String): ValidationInfo = ValidationInfo("Invalid input: '$url' is not a valid URL")
