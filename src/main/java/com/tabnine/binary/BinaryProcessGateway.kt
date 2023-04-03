package com.tabnine.binary

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
        val pb = ProcessBuilder(command)
        if (instance.useIJProxySettings) {
            setProxyEnvironmentVariables(pb)
        }
        val createdProcess = pb.start()

        process = createdProcess
        reader = BufferedReader(InputStreamReader(createdProcess.inputStream, StandardCharsets.UTF_8))
    }

    private fun setProxyEnvironmentVariables(pb: ProcessBuilder) {
        val env = pb.environment()
        val serverUrl = StaticConfig.getBundleServerUrl()
        if (serverUrl.isPresent) {
            val serverUrlString = serverUrl.get()
            val tabnineServerProxy = CommonProxy.getInstance().select(URL(serverUrlString)).firstOrNull()
            if (tabnineServerProxy?.type() == Proxy.Type.DIRECT) {
                env["NO_PROXY"] = serverUrlString
                env["no_proxy"] = serverUrlString
            }
        }
        if (!HttpConfigurable.getInstance().USE_HTTP_PROXY) {
            return
        }
        val proxyString = "${HttpConfigurable.getInstance().PROXY_HOST}:${HttpConfigurable.getInstance().PROXY_PORT}"

        if (proxyString.isNotBlank()) {
            env["HTTPS_PROXY"] = proxyString
            env["https_proxy"] = proxyString
            env["HTTP_PROXY"] = proxyString
            env["http_proxy"] = proxyString
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
