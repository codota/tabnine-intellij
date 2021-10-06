package com.tabnine.logging

import org.apache.log4j.Level.toLevel
import org.apache.log4j.LogManager

fun init() {
    val tabnineLogger = LogManager.getLogger("#com.tabnine")
    tabnineLogger.level = toLevel(System.getenv("LOG_LEVEL"))
    val logsGatewayAppender = LogsGatewayAppender()
    tabnineLogger.addAppender(logsGatewayAppender)
}
