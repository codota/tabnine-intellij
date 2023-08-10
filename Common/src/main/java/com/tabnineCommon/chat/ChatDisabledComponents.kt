package com.tabnineCommon.chat

import java.awt.Desktop
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.SwingConstants

val CHAT_DISABLED_PAGE = """
            <html>
                <body style="padding: 20px;">
                    <div style="font-size: 12px; margin-bottom: 8px"><b>Tabnine Chat is currently in Beta</b></div>
                    <div style="font-size: 10px;">
                        <p style="margin-bottom: 8px">We understand that waiting for this awesome feature isn’t easy, but we guarantee it will be worth it.</p>
                        <p style="margin-bottom: 8px">Tabnine Chat will soon be available to all users, and we'll make sure to keep you informed. Thank you for your patience!</p>
                        <a href="">Learn more</a>
                    </div>
                </body>
            </html>
""".trimIndent()

fun createChatDisabledJLabel(): JLabel {
    val label = JLabel(
        CHAT_DISABLED_PAGE, SwingConstants.CENTER
    )

    label.border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
    label.addMouseListener(NaiveOpenLinkListener("https://www.tabnine.com/#ChatSection"))

    return label
}

class NaiveOpenLinkListener(private val link: String) : MouseListener {
    override fun mouseClicked(e: MouseEvent?) {
        try {
            Desktop.getDesktop().browse(URI(link))
            return
        } catch (e1: IOException) {
            e1.printStackTrace()
        } catch (e1: URISyntaxException) {
            e1.printStackTrace()
        }
    }

    override fun mousePressed(e: MouseEvent?) {
    }

    override fun mouseReleased(e: MouseEvent?) {
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }
}
