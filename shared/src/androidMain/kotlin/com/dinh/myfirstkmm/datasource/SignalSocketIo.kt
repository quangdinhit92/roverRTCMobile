package com.dinh.myfirstkmm.datasource

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.serialization.json.JsonObject
import org.json.JSONObject

class SignalSocketIo : Signal {

    private lateinit var onConnected: () -> Unit
    private lateinit var onClosed: () -> Unit
    private lateinit var onReady: (String) -> Unit
    private lateinit var onData: (String) -> Unit

    val options = IO.Options().apply {
        reconnection = true
        transports = arrayOf("websocket")
    }

    private val mSocket = IO.socket("https://robortc.el.r.appspot.com", options)

    init {
        mSocket.on(Socket.EVENT_CONNECT_ERROR, object : Emitter.Listener {
            override fun call(vararg args: Any?) {
                args.toString()
            }
        })
        mSocket.on("connect", object : Emitter.Listener {
            override fun call(vararg args: Any?) {
                onConnected.invoke()
            }
        })
        mSocket.on("disconnect", object : Emitter.Listener {
            override fun call(vararg args: Any?) {
                onClosed.invoke()
            }
        })
        mSocket.on("ready", object : Emitter.Listener {
            override fun call(vararg args: Any?) {
                val data = args[0] as JSONObject
                onReady.invoke(data.toString())
            }
        })
        mSocket.on("data", object : Emitter.Listener {
            override fun call(vararg args: Any?) {
                val data = args[0] as JSONObject
                onData.invoke(data.toString())

            }
        })

    }


    override fun send(data: String) {
        mSocket.emit(data)
    }

    override fun setOnListenConnected(onConnected: () -> Unit) {
        this.onConnected = onConnected
    }

    override fun setOnListenClosed(onClosed: () -> Unit) {
        this.onClosed = onClosed
    }

    override fun setOnListenData(onData: (data: String) -> Unit) {
        this.onData = onData
    }

    override fun setOnListenReady(onReady: (fromId: String) -> Unit) {
        this.onReady = onReady
    }

    override fun initConnection() {
        mSocket.connect()
    }

    override fun emit(event: String, data: JsonObject) {

        try {
            val jsonData = JSONObject(data.toString())
            mSocket.emit(event, jsonData)
        }
        catch (e:Exception)
        {
            Log.d(" SOCKETIO","error: "+e.toString())
        }

    }
}