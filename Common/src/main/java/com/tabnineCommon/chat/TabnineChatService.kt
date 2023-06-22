package com.tabnineCommon.chat

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.io.readText
import com.jetbrains.cef.JCefAppConfig
import com.tabnineCommon.general.StaticConfig
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.network.CefRequest
import java.nio.file.Paths

class TabnineChatService {
    private lateinit var webViewBrowser: JBCefBrowser
    private lateinit var postMessageListener: JBCefJSQuery
    private var messageRouter = ChatMessagesRouter()

    fun getBrowser(project: Project): JBCefBrowser {
        if (this::webViewBrowser.isInitialized) return webViewBrowser
        val browser = createBrowser()
        val postMessageListener = JBCefJSQuery.create(browser)

        postMessageListener.addHandler {
            handleIncomingMessage(it, project, browser)
            return@addHandler null
        }

        browser.jbCefClient.addLoadHandler(
            cefLoadHandler(postMessageListener, browser),
            browser.cefBrowser
        )

        loadChatHtml(browser)

        this.webViewBrowser = browser
        this.postMessageListener = postMessageListener

        return this.webViewBrowser
    }

    private fun loadChatHtml(browser: JBCefBrowser) {
        val destination = Paths.get(StaticConfig.getBaseDirectory().toString(), "chat")
        ChatBundleExtractor.extractBundle(destination)
        val text = Paths.get(destination.toString(), "index.html").readText()
        val textReplaced = text.replace("/static/js/", "$destination/static/js/")
        browser.loadHTML(textReplaced)
    }

    private fun handleIncomingMessage(
        it: String,
        project: Project,
        browser: JBCefBrowser
    ) {
        Logger.getInstance(javaClass).warn("Received message: $it")

        ApplicationManager.getApplication().invokeLater {
            val response = messageRouter.handleRawMessage(it, project)
            Logger.getInstance(javaClass).warn("Sending response: $response")
            browser.cefBrowser.executeJavaScript("window.postMessage($response, '*')", "", 0)
        }
    }

    private fun cefLoadHandler(
        postMessageListener: JBCefJSQuery,
        browser: JBCefBrowser
    ) = object : CefLoadHandler {
        override fun onLoadingStateChange(p0: CefBrowser?, p1: Boolean, p2: Boolean, p3: Boolean) {
        }

        override fun onLoadStart(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest.TransitionType?) {
        }

        override fun onLoadEnd(p0: CefBrowser?, frame: CefFrame?, p2: Int) {
            if (frame != null && frame.isMain) {
                val script =
                    "window.postPluginMessage = function(e) { ${postMessageListener.inject("JSON.stringify(e)")} }"
                browser.cefBrowser.executeJavaScript(script, "", 0)
                browser.openDevtools()
            }
        }

        override fun onLoadError(
            p0: CefBrowser?,
            p1: CefFrame?,
            p2: CefLoadHandler.ErrorCode?,
            p3: String?,
            p4: String?
        ) {
        }
    }

    private fun createBrowser(): JBCefBrowser {
        val settings = JCefAppConfig.getInstance()
        settings.cefSettings.command_line_args_disabled = false
        settings.appArgsAsList.add("--disable-web-security")
        val cefApp = JBCefApp.getInstance()
        return JBCefBrowser(cefApp.createClient(), "")
    }
}
