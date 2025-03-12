package com.dinh.myfirstkmm.datasource

import kotlinx.serialization.json.JsonObject


expect interface Signal {
    fun send(data: String)
    fun setOnListenConnected(onConnected: () -> Unit)
    fun setOnListenClosed(onClosed: () -> Unit)
    fun setOnListenData(onData: (data: String) -> Unit)
    fun setOnListenReady(onReady: (fromId: String) -> Unit)
    fun initConnection()
    fun emit(event: String, data: JsonObject)
}