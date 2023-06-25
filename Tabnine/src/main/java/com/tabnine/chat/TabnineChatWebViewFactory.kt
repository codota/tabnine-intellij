package com.tabnine.chat

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.io.readText
import com.jetbrains.cef.JCefAppConfig
import com.tabnine.chat.actions.TabnineActionsGroup
import com.tabnineCommon.general.StaticConfig
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.network.CefRequest
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.nio.file.Paths

class TabnineChatWebViewFactory(private val messageRouter: ChatMessagesRouter) : ToolWindowFactory, Disposable {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val browser = getBrowser(project)

        val ourGroup = TabnineActionsGroup.create(browser)
        val createActionToolbar =
            ActionManager.getInstance().createActionToolbar("Tabnine chat", ourGroup, true)
        createActionToolbar.setTargetComponent(browser.component)

        toolWindow.component.add(createActionToolbar.component, BorderLayout.NORTH)
        toolWindow.component.add(browser.component)
    }

    private fun getBrowser(project: Project): JBCefBrowser {
        val browser = createBrowser()
        val postMessageListener = JBCefJSQuery.create(browser)
        val copyCodeListener = JBCefJSQuery.create(browser)

        postMessageListener.addHandler {
            handleIncomingMessage(it, project, browser)
            return@addHandler null
        }
        copyCodeListener.addHandler {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(it), null)
            return@addHandler null
        }

        browser.jbCefClient.addLoadHandler(
            cefLoadHandler(browser, postMessageListener, copyCodeListener),
            browser.cefBrowser
        )

        loadChatOnto(browser)

        return browser
    }

    private fun loadChatOnto(browser: JBCefBrowser) {
        val devServerUrl = System.getenv("TABNINE_CHAT_DEV_SERVER_URL")

        if (devServerUrl != null) {
            Logger.getInstance(javaClass).debug("Running Tabnine Chat on dev server $devServerUrl")
            browser.loadURL(devServerUrl)
            browser.openDevtools()
            return
        }

        Logger.getInstance(javaClass).info("Running Tabnine Chat")

        try {
            val destination = Paths.get(StaticConfig.getBaseDirectory().toString(), "chat")
            ChatBundleExtractor.extractBundle(destination)

            val text = Paths.get(destination.toString(), "index.html").readText()
            val textReplaced = text.replace("/static/js/", "$destination/static/js/")

            browser.loadHTML(textReplaced)
        } catch (e: IllegalStateException) {
            Logger.getInstance(javaClass).error("Failed to extract bundle", e)
        }
    }

    private fun handleIncomingMessage(
        it: String,
        project: Project,
        browser: JBCefBrowser,
    ) {
        Logger.getInstance(javaClass).debug("Received message: $it")

        ApplicationManager.getApplication().invokeLater {
            val response = messageRouter.handleRawMessage(it, project)
            Logger.getInstance(javaClass).debug("Sending response: $response")
            browser.cefBrowser.executeJavaScript("window.postMessage($response, '*')", "", 0)
        }
    }

    private fun cefLoadHandler(
        browser: JBCefBrowser,
        postMessageListener: JBCefJSQuery,
        copyCodeListener: JBCefJSQuery,
    ) = object : CefLoadHandler {
        override fun onLoadingStateChange(p0: CefBrowser?, p1: Boolean, p2: Boolean, p3: Boolean) {
        }

        override fun onLoadStart(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest.TransitionType?) {
        }

        override fun onLoadEnd(p0: CefBrowser?, frame: CefFrame?, p2: Int) {
            if (frame != null && frame.isMain) {
                val postPluginMessageScript =
                    "window.postPluginMessage = function(e) { ${postMessageListener.inject("JSON.stringify(e)")} };"
                val copyToClipboardScript =
                    "window.navigator.clipboard.writeText = function(text) { ${copyCodeListener.inject("text")} };"
                browser.cefBrowser.executeJavaScript(
                    String.format(
                        "%s\n%s",
                        postPluginMessageScript,
                        copyToClipboardScript
                    ),
                    "", 0
                )
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

    override fun dispose() {}
}
