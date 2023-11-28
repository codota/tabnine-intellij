package com.tabnineCommon.chat

import com.intellij.ide.BrowserUtil
import com.tabnineCommon.binary.requests.login.LoginRequest
import com.tabnineCommon.general.DependencyContainer
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener

const val FEATURE_MISSING = """<html>
                <body style="padding: 20px;">
                    <div style="font-size: 12px; margin-bottom: 8px"><b>Tabnine Chat is currently in Beta</b></div>
                    <div style="font-size: 10px;">
                        <p style="margin-bottom: 8px">We understand that waiting for this awesome feature isn’t easy, but we guarantee it will be worth it.</p>
                        <p style="margin-bottom: 8px">Tabnine Chat will soon be available to all users, and we'll make sure to keep you informed. Thank you for your patience! <a href="https://www.tabnine.com/#ChatSection"> Learn more</a></p>
                    </div>
                </body>
            </html>
        """

const val PLEASE_LOGIN = """<html>
                <body style="padding: 20px;">
                    <div style="font-size: 12px; margin-bottom: 8px"><b>Tabnine Chat is currently in Beta</b></div>
                    <div style="font-size: 10px;">
                        <p style="margin-bottom: 8px">We understand that waiting for this awesome feature isn’t easy, but we guarantee it will be worth it.</p>
                        <p style="margin-bottom: 8px">Tabnine Chat will soon be available to all users, and we'll make sure to keep you informed. Thank you for your patience! <a href="https://www.tabnine.com/#ChatSection"> Learn more</a></p>
                        <p>If you already have access to chat, please <a href="https://app.tabnine.com/signin">sign in</a></p>
                    </div>
                </body>
            </html>
        """

const val NEED_TO_BE_PART_OF_A_TEAM = """<html>
                <body style="padding: 20px;">
                    <div style="font-size: 12px; margin-bottom: 8px"><b>Tabnine Chat</b></div>
                    <div style="font-size: 10px;">
                        <p style="margin-bottom: 8px">To use Tabnine chat please make sure you are part of a team.</p>
                    </div>
                </body>
            </html>
        """

fun createChatDisabledJPane(chatDisabledReason: ChatDisabledReason): JEditorPane {
    val htmlContent = when (chatDisabledReason) {
        ChatDisabledReason.AUTHENTICATION_REQUIRED -> PLEASE_LOGIN
        ChatDisabledReason.FEATURE_REQUIRED -> FEATURE_MISSING
        ChatDisabledReason.PART_OF_A_TEAM_REQUIRED -> NEED_TO_BE_PART_OF_A_TEAM
    }

    val pane = JEditorPane(
        "text/html",
        htmlContent.trimIndent()
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
