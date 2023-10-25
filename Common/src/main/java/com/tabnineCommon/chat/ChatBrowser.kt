package com.tabnineCommon.chat

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.io.readText
import com.intellij.util.net.HttpConfigurable
import com.jetbrains.cef.JCefAppConfig
import com.tabnineCommon.general.StaticConfig
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.network.CefRequest
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

class ChatBrowser private constructor(project: Project) {
    var jbCefBrowser: JBCefBrowser
    private val chatAppStartupListeners = mutableMapOf<String, () -> Unit>()
    private val isChatAppAlive = AtomicBoolean(false)

    init {
        this.jbCefBrowser = initializeBrowser(project)
    }

    companion object {
        fun getInstance(project: Project): ChatBrowser {
            val existingBrowser = project.getUserData(Consts.BROWSER_PROJECT_KEY)
            if (existingBrowser != null) {
                return existingBrowser
            }
            val newBrowser = ChatBrowser(project)
            project.putUserData(Consts.BROWSER_PROJECT_KEY, newBrowser)
            return newBrowser
        }
    }

    private fun initializeBrowser(
        project: Project,
    ): JBCefBrowser {
        val browser = createBrowser()
        val postMessageListener = JBCefJSQuery.create(browser)
        val copyCodeListener = JBCefJSQuery.create(browser)
        postMessageListener.addHandler {
            ApplicationManager.getApplication().invokeLater {
                handleIncomingMessage(it, project, browser)
            }
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
        loadChatOnto(browser, project)

        return browser
    }

    fun isChatAppAlive(): Boolean {
        return isChatAppAlive.get()
    }

    fun registerChatAppStartupListener(id: String, project: Project, listener: () -> Unit) {
        Logger.getInstance(javaClass).debug("Registering chat app startup $id for project ${project.name}")
        chatAppStartupListeners[id] = listener
    }

    private fun loadChatOnto(browser: JBCefBrowser, project: Project) {
        val devServerUrl = System.getenv("TABNINE_CHAT_DEV_SERVER_URL")

        if (devServerUrl != null) {
            Logger.getInstance(javaClass).debug("Running Tabnine Chat on dev server $devServerUrl")
            browser.loadURL(devServerUrl)
            return
        }

        Logger.getInstance(javaClass).info("Running Tabnine Chat for project ${project.name}")

        try {
            val destination = Paths.get(StaticConfig.getBaseDirectory().toString(), "chat")
            ChatBundleExtractor.extractBundle(destination)

            browser.loadHTML(Paths.get(destination.toString(), "index.html").readText())
        } catch (e: IllegalStateException) {
            Logger.getInstance(javaClass).error("Failed to extract bundle", e)
        }
    }

    private fun handleIncomingMessage(
        it: String,
        project: Project,
        browser: JBCefBrowser,
    ) {
        if (!isChatAppAlive.getAndSet(true)) {
            Logger.getInstance(javaClass).debug("Chat app has started for project ${project.name}, invoking chat app startup listeners")
            chatAppStartupListeners.forEach {
                Logger.getInstance(javaClass).debug("Running chat app startup listener '${it.key}' on project ${project.name}")
                it.value()
            }
        }
        Logger.getInstance(javaClass).trace("Received message: $it")
        val response = ChatMessagesRouter.handleRawMessage(it, project)

        Logger.getInstance(javaClass).trace("Sending response: $response")
        browser.cefBrowser.executeJavaScript("window.postMessage($response, '*')", "", 0)
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

        val proxyServer = getProxySettings()
        if (proxyServer != null) {
            settings.appArgsAsList.add("--proxy-server=$proxyServer")
        }

        val cefApp = JBCefApp.getInstance()
        return JBCefBrowser(cefApp.createClient(), "")
    }
}

fun getProxySettings(): String? {
    val httpConfigurable = HttpConfigurable.getInstance()

    return if (httpConfigurable.USE_HTTP_PROXY) {
        val auth = if (httpConfigurable.PROXY_AUTHENTICATION && httpConfigurable.proxyLogin?.isNotEmpty() == true) {
            "${httpConfigurable.proxyLogin}:${httpConfigurable.plainProxyPassword}@"
        } else {
            ""
        }
        "http=${auth}${httpConfigurable.PROXY_HOST}:${httpConfigurable.PROXY_PORT}"
    } else {
        null
    }
}
