package com.tabnineCommon.chat

import com.intellij.ide.BrowserUtil
import com.tabnineCommon.binary.requests.login.LoginRequest
import com.tabnineCommon.general.DependencyContainer
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener

const val PREVIEW_ENDED_MESSAGE = """<html>
                <body style="padding: 20px;font-family: sans-serif;">
                    <div style="font-size: 12px; margin-bottom: 8px"><b>Tabnine Chat - Preview Period Ended</b></div>
                    <div style="font-size: 10px;">
                        <p style="margin-bottom: 8px">The preview period for Tabnine Chat has now ended. We hope you found our plugin valuable for your coding projects and enjoyed using it.</p>
                        <p style="margin-bottom: 8px">To continue enjoying Tabnine Chat and explore its full range of features, we invite you to consider subscribing to one of our plans. Detailed information about our pricing and the additional benefits of a subscription can be found on our <a href="https://www.tabnine.com/pricing">pricing page</a>.</p>
                        <p style="margin-bottom: 8px">If you have any questions or need assistance, our support team is always ready to help. Thank you for your understanding and support!</p>
                    </div>
                </body>
            </html>
        """

const val FEATURE_MISSING = """<html>
                <body style="padding: 20px;font-family: sans-serif;">
                    <div style="font-size: 12px; margin-bottom: 8px"><b>Tabnine Chat is currently in Beta</b></div>
                    <div style="font-size: 10px;">
                        <p style="margin-bottom: 8px">We understand that waiting for this awesome feature isn’t easy, but we guarantee it will be worth it.</p>
                        <p style="margin-bottom: 8px">Tabnine Chat will soon be available to all users, and we'll make sure to keep you informed. Thank you for your patience! <a href="https://www.tabnine.com/#ChatSection"> Learn more</a></p>
                    </div>
                </body>
            </html>
        """

const val PLEASE_LOGIN = """<html>
                <body style="padding: 20px;font-family: sans-serif;">
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
                <body style="padding: 20px;font-family: sans-serif;">
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
        ChatDisabledReason.PREVIEW_ENDED -> PREVIEW_ENDED_MESSAGE
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
