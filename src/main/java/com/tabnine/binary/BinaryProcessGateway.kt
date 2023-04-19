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
import java.util.Optional

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
        val serverUrl = StaticConfig.getBundleServerUrl()
        if (serverUrl.isPresent) {
            setNoProxyForTabnineServerUrl(serverUrl, env)
        }
        inheritIDEProxySettings(env, httpConfigurable)
    }

    private fun setNoProxyForTabnineServerUrl(
        serverUrl: Optional<String>,
        env: MutableMap<String, String>
    ) {
        val serverUrlURL = URL(serverUrl.get())
        val tabnineServerProxy = CommonProxy.getInstance().select(serverUrlURL).firstOrNull()
        if (tabnineServerProxy?.type() == Proxy.Type.DIRECT) {
            env["NO_PROXY"] = serverUrlURL.host
            env["no_proxy"] = serverUrlURL.host
        }
    }

    private fun inheritIDEProxySettings(env: MutableMap<String, String>, httpConfigurable: HttpConfigurable) {
        val httpProxy = "http://${httpConfigurable.PROXY_HOST}:${httpConfigurable.PROXY_PORT}"
        val httpsProxy = "https://${httpConfigurable.PROXY_HOST}:${httpConfigurable.PROXY_PORT}"
        if (httpProxy.isNotBlank()) {
            env["HTTP_PROXY"] = httpProxy
            env["http_proxy"] = httpProxy
        }
        if (httpsProxy.isNotBlank()) {
            env["HTTPS_PROXY"] = httpsProxy
            env["https_proxy"] = httpsProxy
        }
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
