package com.dinh.myfirstkmm.android.ui.webrtc

import android.util.Log
import com.dinh.myfirstkmm.datasource.Signal
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject
import org.webrtc.AddIceObserver
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import java.nio.ByteBuffer
import java.util.concurrent.Executors


class PeerConnectionClient(
    val onGetRemoteStream: (Array<out MediaStream>) -> Unit,
    private val videoTrack: VideoTrack?,
    private val signal: Signal,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val roomName: String,
    private val userName:String,
) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }

    val executor = Executors.newSingleThreadExecutor()


    val myIdentity = buildJsonObject {
        put("username", userName)
        put("room", roomName)
    }

    private var peerConnection: PeerConnection? = null
    private var channel: DataChannel? = null
    var isReady = false
    val iceServers: List<PeerConnection.IceServer> = listOf(


        PeerConnection.IceServer.builder("turn:relay11.expressturn.com:3478")
            .setUsername("efWHQU8DBSR5PQCAB6").setPassword("QAC1iNJtnUOtSE4C").createIceServer()

    )

    init {


        val myIdentityxx = JSONObject().apply {
            put("username", userName)
            put("room", roomName)

        }

        signal.setOnListenConnected {
            Log.d("SOCKETIO", "CONNECT + Join")
            signal.emit("join", myIdentity)
        }
        signal.setOnListenClosed {
            Log.d("SOCKETIO", "Close---->")
        }
        signal.setOnListenData { data ->
            Log.d("SOCKETIO", "onData----> ${data}")
            val jsonData = JSONObject(data)
            val type = jsonData.get("type")
            when (type) {
                "answer" -> {
                    val sessionDescription = SessionDescription(
                        SessionDescription.Type.ANSWER, jsonData.getString("sdp")
                    )
                    peerConnection?.setRemoteDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {
//                            TODO("Not yet implemented")
                        }

                        override fun onSetSuccess() {
                            //  TODO("Not yet implemented")
                        }

                        override fun onCreateFailure(p0: String?) {
                            //TODO("Not yet implemented")
                        }

                        override fun onSetFailure(p0: String?) {
                            // TODO("Not yet implemented")
                        }
                    }, sessionDescription)

                }

                "candidate" -> {

                    executor.execute {
                        val candidateJson = JSONObject(jsonData.getString("candidate"))
                        val v1 = candidateJson.getString("sdpMid")
                        val v2 = candidateJson.getInt("sdpMLineIndex")
                        val v3 = candidateJson.getString("candidate")
                        val candidate = IceCandidate(
                            v1, v2, v3
                        )
                        peerConnection?.addIceCandidate(candidate, object : AddIceObserver {
                            override fun onAddSuccess() {
                                Log.d("SOCKETIO", "addIce candidate success")
                            }

                            override fun onAddFailure(p0: String?) {
                                Log.e("SOCKETIO", "addIce candidate failure")
                            }
                        })

                    }

                }
            }


        }
        signal.setOnListenReady {
            Log.d("SOCKETIO", "onReady---->")
            if (!isReady) {
                isReady = true
                signal.emit("ready", myIdentity)
                initializePeerConnection()
                createOffer()
            }
        }


    }


    fun initializePeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            this.iceTransportsType = PeerConnection.IceTransportsType.ALL
            this.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = peerConnectionFactory!!.createPeerConnection(rtcConfig,
            object : PeerConnection.Observer {
                override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
                    Log.d("SOCKETIO_WEBRTC", "onSignalingChange")
                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    Log.d("SOCKETIO_WEBRTC", "onIceConnectionChange ${p0?.toString()}")
                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {
                    Log.d("SOCKETIO_WEBRTC", "onIceConnectionReceivingChange")

                }

                override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
                    Log.d("SOCKETIO_WEBRTC", "onIceGatheringChange: " + p0.toString())

                }

                override fun onIceCandidate(candidate: IceCandidate?) {
                    Log.d("SOCKETIO_WEBRTC", " onIceCandidate " + candidate.toString())
                    candidate?.let {
                        val candidateJson = buildJsonObject {
                            put("sdpMid", candidate.sdpMid)
                            put("sdpMLineIndex", candidate.sdpMLineIndex)
                            put("candidate", candidate.sdp)
                        }

                        val myCandidateData = buildJsonObject {
                            put("type", "candidate")
                            put("candidate", candidateJson)
                        }

                        val data = buildJsonObject {
                            put("username", userName)
                            put("room", roomName)
                            put("data", myCandidateData)
                        }

                        signal.emit("data", data)

                    }
                }

                override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
                    Log.d("SOCKETIO_WEBRTC", "onIceCandidatesRemoved")

                }

                override fun onAddStream(p0: MediaStream?) {
                    Log.d("SOCKETIO_WEBRTC", "onAddStream")

                }

                override fun onRemoveStream(p0: MediaStream?) {
                    Log.d("SOCKETIO_WEBRTC", "onRemoveStream")

                }

                override fun onDataChannel(p0: DataChannel?) {
                    p0?.let {
                        channel = it
                    }

                    channel?.registerObserver(object : DataChannel.Observer {
                        override fun onBufferedAmountChange(p0: Long) {
                            Log.d("SOCKETIO", "onBufferedAmountChange---->")
                        }

                        override fun onStateChange() {
                            Log.d("SOCKETIO", "onStateChange---->")
                        }

                        override fun onMessage(buffer: DataChannel.Buffer?) {
                            buffer?.let {
                                val message = if (buffer.binary) {
                                    // Handle binary message
                                    String(buffer.data.array(), Charsets.UTF_8)
                                } else {
                                    try {
                                        val byteBuffer = buffer.data
                                        val bytes = ByteArray(byteBuffer.remaining())
                                        byteBuffer.get(bytes)
                                        val result = String(bytes, Charsets.UTF_8)

                                        Log.d("SOCKETIO", "onMessage---->" + result)
                                    } catch (e: Exception) {
                                        e.toString()
                                    }
                                }
                            }
                        }
                    })

                    Log.d("SOCKETIO_WEBRTC", "onDataChannel" + p0.toString())
                }

                override fun onAddTrack(
                    receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?
                ) {
                    mediaStreams?.let { streams ->

                        onGetRemoteStream.invoke(streams)
                        // remoteStream.update { streams }

                    }
                }

                override fun onTrack(transceiver: RtpTransceiver?) {
                    val receiver = transceiver?.receiver
                    val track = receiver?.track()
                    if (track != null) {
                        if (track.kind() == MediaStreamTrack.VIDEO_TRACK_KIND) {
                            Log.d("SOCKETIO_WEBRTC", "Video track added: ${track.id()}")
                        } else if (track.kind() == MediaStreamTrack.AUDIO_TRACK_KIND) {
                            Log.d("SOCKETIO_WEBRTC", "Audio track added: ${track.id()}")
                        }
                    }
                }

                override fun onRenegotiationNeeded() {
                    Log.d("SOCKETIO_WEBRTC", "onRenegotiationNeeded")
                }
            })!!


        val dataChannelInit = DataChannel.Init()
        dataChannelInit.ordered = true // Ensure the messages are sent in order
        dataChannelInit.maxRetransmits = -1 // Unlimited retransmissions
        channel = peerConnection?.createDataChannel("commands", dataChannelInit)
    }

    fun createOffer() {
        executor.execute {
            val sdpConstraint = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            }
            try {
                peerConnection?.createOffer(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {
                        peerConnection?.setLocalDescription(object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {
                                Log.d("SOCKETIO_WEBRTC", "create SDP  success")
                            }

                            override fun onSetSuccess() {
                                val offer = buildJsonObject {
                                    put("type", "offer")
                                    put("sdp", p0?.description)
                                }
                                val data = buildJsonObject {
                                    put("username", userName)
                                    put("room", roomName)
                                    put("data", offer)
                                }
                                // Emit the offer to the remote peer
                                Log.d("SOCKETIO_WEBRTC", "send offer")
                                signal.emit("data", data)
                            }

                            override fun onCreateFailure(p0: String?) {
                                Log.e("SOCKETIO_WEBRTC", "create SDP failure")
                            }

                            override fun onSetFailure(p0: String?) {
                                Log.e("SOCKETIO_WEBRTC", "set SDP failure" + p0)
                            }
                        }, p0)
                    }

                    override fun onSetSuccess() {
                        Log.d("SOCKETIO_WEBRTC", "create SDP  success")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.d("SOCKETIO_WEBRTC", "create SDP  success")
                    }

                    override fun onSetFailure(p0: String?) {
                        Log.d("SOCKETIO_WEBRTC", "create SDP  success")
                    }
                }, sdpConstraint)

            } catch (e: Exception) {
                Log.e("SOCKETIO_WEBRTC", "create offer  error ${e.toString()}")
            }


        }


    }

    fun sendPayload(payload: String) {
        channel?.send(DataChannel.Buffer(ByteBuffer.wrap(payload.toByteArray()), false))
    }

    fun cleanupResource() {
        peerConnection?.close()
        channel?.close()
    }
}