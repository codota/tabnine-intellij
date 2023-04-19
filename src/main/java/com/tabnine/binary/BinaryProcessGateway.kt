package com.tabnine.binary

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.net.HttpConfigurable
import com.intellij.util.proxy.CommonProxy
import com.tabnine.binary.exceptions.TabNineDeadException
import com.tabnine.general.StaticConfig
import com.tabnine.userSettings.AppSettingsState.Companion.instance
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Proxy
import java.net.URL
import java.nio.charset.StandardCharsets

open class BinaryProcessGateway {
    private var process: Process? = null
    private var reader: BufferedReader? = null

    @Throws(IOException::class)
    open fun init(command: List<String?>?) {
        val processBuilder = ProcessBuilder(command)
        if (instance.useIJProxySettings) {
            var httpConfigurable: HttpConfigurable =
                ApplicationManager.getApplication().getComponent(HttpConfigurable::class.java)

            if (httpConfigurable == null) {
                httpConfigurable = HttpConfigurable.getInstance()
            }
            setProxyEnvironmentVariables(processBuilder, httpConfigurable)
        }
        val createdProcess = processBuilder.start()

        process = createdProcess
        reader = BufferedReader(InputStreamReader(createdProcess.inputStream, StandardCharsets.UTF_8))
    }

    private fun setProxyEnvironmentVariables(processBuilder: ProcessBuilder, httpConfigurable: HttpConfigurable) {
        if (!httpConfigurable.USE_HTTP_PROXY) {
            return
        }

        val env = processBuilder.environment()
        setNoProxyEnvironmentVariable(env, httpConfigurable)
        inheritIDEProxySettings(env, httpConfigurable)
    }

    private fun setNoProxyEnvironmentVariable(
        env: MutableMap<String, String>,
        httpConfigurable: HttpConfigurable
    ) {
        var noProxyList = httpConfigurable.PROXY_EXCEPTIONS?.split(",")?.map { it.trim() } ?: listOf()

        val serverUrl = StaticConfig.getBundleServerUrl()

        if (!serverUrl.isEmpty()) {
            val serverUrlURL = URL(serverUrl.get())
            if (CommonProxy.getInstance().select(serverUrlURL).any { it.type() == Proxy.Type.DIRECT }) {

                if (!noProxyList.contains(serverUrlURL.host)) {
                    noProxyList += serverUrlURL.host
                }
                if (noProxyList.isEmpty()) {
                    noProxyList = listOf(serverUrlURL.host)
                }
            }
        }
        if (noProxyList.isNotEmpty()) {
            env["NO_PROXY"] = noProxyList.joinToString(",")
            env["no_proxy"] = noProxyList.joinToString(",")
        }
    }

    private fun inheritIDEProxySettings(env: MutableMap<String, String>, httpConfigurable: HttpConfigurable) {
        val auth = if (httpConfigurable.proxyLogin?.isNotEmpty() == true) {
            "${httpConfigurable.proxyLogin}:${httpConfigurable.getPlainProxyPassword()}@"
        } else {
            ""
        }
        val proxy = "${auth}${httpConfigurable.PROXY_HOST}:${httpConfigurable.PROXY_PORT}"
        val httpProxy = "http://$proxy"
        val httpsProxy = "https://$proxy"
        env["HTTP_PROXY"] = httpProxy
        env["http_proxy"] = httpProxy
        env["HTTPS_PROXY"] = httpsProxy
        env["https_proxy"] = httpsProxy
    }

    @Throws(IOException::class, TabNineDeadException::class)
    open fun readRawResponse(): String {
        return reader?.readLine() ?: throw TabNineDeadException("End of stream reached")
    }

    @Throws(IOException::class)
    open fun writeRequest(request: String) {
        process?.outputStream?.write(request.toByteArray(StandardCharsets.UTF_8))
        process?.outputStream?.flush()
    }

    open val isDead: Boolean
        get() = process?.isAlive?.not() ?: true

    open fun destroy() {
        process?.destroy()
    }

    fun pid(): Long? {
        return process?.pid()
    }
}
