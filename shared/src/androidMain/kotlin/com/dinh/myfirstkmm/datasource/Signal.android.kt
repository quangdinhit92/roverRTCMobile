package com.dinh.myfirstkmm.datasource

import kotlinx.serialization.json.JsonObject

actual interface Signal {
    actual fun send(data: String)
    actual fun setOnListenConnected(onConnected: () -> Unit)
    actual fun setOnListenClosed(onClosed: () -> Unit)
    actual fun setOnListenData(onData: (data: String) -> Unit)
    actual fun setOnListenReady(onReady: (fromId: String) -> Unit)
    actual fun initConnection()
    actual fun emit(event: String, data: JsonObject)
}