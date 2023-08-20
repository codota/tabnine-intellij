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

val CHAT_DISABLED_PAGE = """""".trimIndent()

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
