package com.tabnine.binary

import com.intellij.openapi.diagnostic.Logger
import com.tabnine.binary.exceptions.TabNineDeadException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

open class BinaryProcessGateway {
    private var process: Process? = null
    private var reader: BufferedReader? = null

    @Throws(IOException::class)
    open fun init(command: List<String?>?) {
        Logger.getInstance(javaClass).info(String.format("Spawning binary with command: %s", command?.joinToString(separator = " ")))
        val createdProcess = ProcessBuilder(command).start()

        process = createdProcess
        reader = BufferedReader(InputStreamReader(createdProcess.inputStream, StandardCharsets.UTF_8))
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
}
