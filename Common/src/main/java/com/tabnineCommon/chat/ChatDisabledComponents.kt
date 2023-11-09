package com.tabnineCommon.chat

import com.intellij.ide.BrowserUtil
import com.tabnineCommon.binary.requests.login.LoginRequest
import com.tabnineCommon.general.DependencyContainer
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener

val LOGIN_BUTTON = "<p>If you already have access to chat, please <a href=\"https://app.tabnine.com/signin\">sign in</a></p>".trimIndent()

fun createChatDisabledJPane(isLoggedIn: Boolean = false): JEditorPane {
    val pane = JEditorPane(
        "text/html",
        """<html>
                <body style="padding: 20px;">
                    <div style="font-size: 12px; margin-bottom: 8px"><b>Tabnine Chat is currently in Beta</b></div>
                    <div style="font-size: 10px;">
                        <p style="margin-bottom: 8px">We understand that waiting for this awesome feature isn’t easy, but we guarantee it will be worth it.</p>
                        <p style="margin-bottom: 8px">Tabnine Chat will soon be available to all users, and we'll make sure to keep you informed. Thank you for your patience! <a href="https://www.tabnine.com/#ChatSection"> Learn more</a></p>
                        ${if (!isLoggedIn) LOGIN_BUTTON else ""}
                    </div>
                </body>
            </html>
        """.trimIndent()
    ).apply {
        isEditable = false
        isOpaque = false
    }
    pane.addHyperlinkListener(
        HyperlinkListener { it ->
            if (it.eventType !== HyperlinkEvent.EventType.ACTIVATED) {
                return@HyperlinkListener
            }
            if (it.url.toString() == "https://www.tabnine.com/#ChatSection") {
                BrowserUtil.browse(it.url.toString())
            } else if (it.url.toString() == "https://app.tabnine.com/signin") {
                DependencyContainer.instanceOfBinaryRequestFacade().executeRequest(LoginRequest {})
            }
        }
    )

    return pane
}
