package com.tabnine.logging

import com.google.gson.JsonObject
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PermanentInstallationID
import com.intellij.openapi.diagnostic.Logger
import com.tabnine.config.Config
import com.tabnine.general.Utils
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients

class TabnineLogDispatcher(private val loggerDelegate: Logger) {
    private val baseRequestBody = buildRequestBody()

    private fun buildRequestBody(): JsonObject {
        val body = JsonObject()
        body.addProperty("appName", "tabnine-plugin-JB")
        body.addProperty("category", "extensions")
        body.addProperty("ide", ApplicationInfo.getInstance().versionName)
        body.addProperty("ideVersion", ApplicationInfo.getInstance().fullVersion)
        body.addProperty("pluginVersion", Utils.cmdSanitize(Utils.getTabNinePluginVersion()))
        body.addProperty("os", System.getProperty("os.name"))
        body.addProperty("channel", Config.CHANNEL)
        body.addProperty("userId", PermanentInstallationID.get())
        return body
    }

    @Throws(Exception::class)
    fun dispatchLog(level: String, message: String) {
        ApplicationManager.getApplication()
            .executeOnPooledThread {
                try {
                    val postRequest = HttpPost(String.format("%s/logs/%s", Config.LOGGER_HOST, level))
                    val requestBody = baseRequestBody.deepCopy()
                    requestBody.addProperty("message", message)

                    postRequest.entity = StringEntity(requestBody.toString())
                    postRequest.setHeader("Content-Type", "application/json")
                    val httpClient = HttpClients.createDefault()
                    httpClient.execute(postRequest)
                } catch (e: Throwable) {
                    loggerDelegate.warn("Tabnine log dispatcher failed to send logs: ", e)
                }
            }
    }
}
