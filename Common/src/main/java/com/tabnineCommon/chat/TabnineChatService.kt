package com.tabnineCommon.chat

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import com.tabnineCommon.general.Utils
import java.util.concurrent.TimeUnit

class TabnineChatService {
    private lateinit var webViewBrowser: JBCefBrowser
    private lateinit var postMessageListener: JBCefJSQuery
    private var messageRouter = ChatMessagesRouter()

    fun getBrowser(project: Project): JBCefBrowser {
        if (this::webViewBrowser.isInitialized) return webViewBrowser

        val browser = JBCefBrowser()
        val postMessageListener = JBCefJSQuery.create(browser)
        postMessageListener.addHandler {
            Logger.getInstance(javaClass).warn("Received message: $it")

            messageRouter.handleMessage(it, project)?.let { response ->
                browser.cefBrowser.executeJavaScript("window.postMessage($response, '*')", "", 0)
            }

            return@addHandler null
        }

        browser.loadURL("http://localhost:3000/")

        this.webViewBrowser = browser
        this.postMessageListener = postMessageListener

        Utils.executeThread(
            {
                val script =
                    "window.postPluginMessage = function(e) { ${postMessageListener.inject("JSON.stringify(e)")} }"

                browser.cefBrowser.executeJavaScript(script, "", 0)
                browser.openDevtools()
            },
            4, TimeUnit.SECONDS
        )

        return this.webViewBrowser
    }
}
