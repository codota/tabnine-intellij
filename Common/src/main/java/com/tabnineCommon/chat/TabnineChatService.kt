package com.tabnineCommon.chat

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import com.tabnineCommon.general.Utils
import com.tabnineCommon.lifecycle.LifeCycleHelper
import java.util.concurrent.TimeUnit

class TabnineChatService {
    var webViewBrowser: JBCefBrowser
    var postMessageListener: JBCefJSQuery
    var currE: String? = null

    init {
        val browser = JBCefBrowser()
        this.postMessageListener = JBCefJSQuery.create(browser)
        this.postMessageListener.addHandler {
            currE = it
            Logger.getInstance(javaClass).warn("Received message: $it")
            browser.cefBrowser.executeJavaScript("window.postMessage('yair ha gever vegam kaki pipi', '*')", "", 0)
            return@addHandler null
        }

        browser.loadURL("http://localhost:3000/")

        this.webViewBrowser = browser
        Utils.executeThread(
            {
                val script =
                    "window.postPluginMessage = function(e) { ${postMessageListener.inject("JSON.stringify(e)")} }"

                browser.cefBrowser.executeJavaScript(script, "", 0)
                browser.openDevtools()
            },
            4, TimeUnit.SECONDS
        )

        Disposer.register(LifeCycleHelper.getInstance(), browser)
    }
}
