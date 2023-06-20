package com.tabnineCommon.chat

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery

class TabnineChatService {
    private lateinit var webViewBrowser: JBCefBrowser
    private lateinit var postMessageListener: JBCefJSQuery
    private var messageRouter = ChatMessagesRouter()

    fun getBrowser(project: Project): JBCefBrowser {
        if (this::webViewBrowser.isInitialized) return webViewBrowser
//        val settings = JCefAppConfig.getInstance()
//        settings.cefSettings.command_line_args_disabled = false
//        settings.appArgsAsList.add("--enable-media-stream=true")
//        val cefApp = JBCefApp.getInstance()
//        val browser = JBCefBrowser(cefApp.createClient(), "http://localhost:3000")
        val browser = JBCefBrowser()
        val postMessageListener = JBCefJSQuery.create(browser)
        postMessageListener.addHandler {
            Logger.getInstance(javaClass).warn("Received message: $it")

            ApplicationManager.getApplication().invokeLater {
                val response = messageRouter.handleRawMessage(it, project)
                Logger.getInstance(javaClass).warn("Sending response: $response")
                browser.cefBrowser.executeJavaScript("window.postMessage($response, '*')", "", 0)
            }

            return@addHandler null
        }

        browser.loadHTML(
            "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\"/>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>\n" +
                "    <meta name=\"theme-color\" content=\"#000000\"/>\n" +
                "    <link rel=\"apple-touch-icon\" href=\"/logo192.png\"/>\n" +
                "    <link rel=\"manifest\" href=\"/manifest.json\"/>\n" +
                "    <title>React App</title>\n" +
                "    <style>body, div#root, html {\n" +
                "        margin: 0;\n" +
                "        padding: 0;\n" +
                "        border: 0;\n" +
                "        width: 100%;\n" +
                "        height: 100%;\n" +
                "        -webkit-box-sizing: border-box;\n" +
                "        box-sizing: border-box;\n" +
                "        font-family: sans-serif\n" +
                "    }\n" +
                "\n" +
                "    *, ::after, ::before {\n" +
                "        -webkit-box-sizing: inherit;\n" +
                "        box-sizing: inherit\n" +
                "    }\n" +
                "\n" +
                "    ::-webkit-scrollbar {\n" +
                "        width: 6px\n" +
                "    }\n" +
                "\n" +
                "    ::-webkit-scrollbar-track {\n" +
                "        background: 0 0\n" +
                "    }\n" +
                "\n" +
                "    ::-webkit-scrollbar-thumb {\n" +
                "        background: #888\n" +
                "    }\n" +
                "\n" +
                "    ::-webkit-scrollbar-thumb:hover {\n" +
                "        background: #555\n" +
                "    }</style>\n" +
                "<script>window.postPluginMessage = function(e) { ${postMessageListener.inject("JSON.stringify(e)")} }</script>\n" +
                "    <script defer=\"defer\" src=\"/home/yair/workspace/tabnine-intellij/Common/src/main/java/com/tabnineCommon/chat/client/main.de16f24f.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "<noscript>You need to enable JavaScript to run this app.</noscript>\n" +
                "<div id=\"root\"></div>\n" +
                "</body>\n" +
                "</html>",
            "http://localhost:3010/app"
        )
//        val script =
//            "window.postPluginMessage = function(e) { ${postMessageListener.inject("JSON.stringify(e)")} }"
//        browser.cefBrowser.executeJavaScript(script, "", 0)

        this.webViewBrowser = browser
        this.postMessageListener = postMessageListener

//        val client = browser.jbCefClient
//        client.addLoadHandler(
//            object : CefLoadHandler {
//                override fun onLoadingStateChange(browser: CefBrowser?, isLoading: Boolean, canGoBack: Boolean, canGoForward: Boolean) {
//                    if (isLoading) {
//                        Logger.getInstance(javaClass).info("Page is loading...")
//                    } else {
//                        Logger.getInstance(javaClass).info("Page has loaded!")
//                        val script =
//                            "window.postPluginMessage = function(e) { ${postMessageListener.inject("JSON.stringify(e)")} }\n" +
//                                "window.postMessage({isReady: true}, '*')"
//                        browser?.executeJavaScript(script, "", 0)
//                    }
//                }
//
//                override fun onLoadStart(p0: CefBrowser?, p1: CefFrame?, p2: CefRequest.TransitionType?) {
//                    Logger.getInstance(javaClass).info("Load Start")
//                }
//
//                override fun onLoadEnd(p0: CefBrowser?, p1: CefFrame?, p2: Int) {
//                    Logger.getInstance(javaClass).info("Load ended")
//                }
//
//                override fun onLoadError(p0: CefBrowser?, p1: CefFrame?, p2: CefLoadHandler.ErrorCode?, p3: String?, p4: String?) {
//                    Logger.getInstance(javaClass).error("Load failed")
//                }
//            },
//            browser.cefBrowser
//        )

//        Utils.executeThread {
//            while (browser.cefBrowser.client.) {
//                Thread.sleep(500)
//            }
//            val script =
//                "window.postPluginMessage = function(e) { ${postMessageListener.inject("JSON.stringify(e)")} }\n" +
//                    "window.postMessage({isReady: true}, '*')"
//            browser.cefBrowser.executeJavaScript(script, "", 0)
//            browser.openDevtools()
//        }

        return this.webViewBrowser
    }
}
