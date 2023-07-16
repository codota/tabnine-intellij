package com.tabnineCommon.chat.commandHandlers.chatSettings

import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent

private const val CHAT_SETTINGS_KEY = "com.tabnine.chat.settings"

data class ChatSettingsProps(val isTelemetryEnabled: Boolean? = null)

class ChatSettings(private val gson: Gson) {
    fun save(settings: ChatSettingsProps) {
        PropertiesComponent.getInstance().setValue(CHAT_SETTINGS_KEY, gson.toJson(settings))
    }

    fun get(): ChatSettingsProps {
        return PropertiesComponent.getInstance().getValue(CHAT_SETTINGS_KEY)?.let { gson.fromJson(it, ChatSettingsProps::class.java) } ?: ChatSettingsProps()
    }
}
