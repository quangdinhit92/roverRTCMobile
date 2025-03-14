package com.dinh.myfirstkmm.android.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.dinh.myfirstkmm.android.ui.component.BaseViewModel
import com.dinh.myfirstkmm.android.ui.webrtc.PeerConnectionClient
import com.dinh.myfirstkmm.datasource.Signal
import com.dinh.myfirstkmm.datasource.SignalSocketIo
import com.dinh.myfirstkmm.domain.RoboHeadTurnCommandPayload
import com.dinh.myfirstkmm.domain.RoboMoveCommandPayload
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.webrtc.Camera1Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.EglBase
import org.webrtc.MediaStream
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import javax.inject.Inject


sealed class UserAction() {
    data class leftJoystickMove(val x: Float, val y: Float, val r: Float) : UserAction()
    data class rightJoystickMove(val phi: Int, val theta: Int) : UserAction()
    object onStopControl : UserAction()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context, private val moshi: Moshi
) : BaseViewModel() {

    private var roomName = "123"
    private var userName = "webrtcAndroid"

    fun setUserAndRoom(room:String,user:String)
    {
        roomName =room
        userName=user

    }


    private var peerConnectionClient: PeerConnectionClient? = null
    private var signal: Signal? = null

    private var _remoteStream = MutableStateFlow<Array<out MediaStream>>(emptyArray())
    val remoteStream: StateFlow<Array<MediaStream>> = _remoteStream.map {
        it.filterNotNull().toTypedArray()
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = emptyArray()
    )

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var rootEglBase: EglBase? = null

    private val lock = Any()


    fun setupPeer(peerConnectionFactory: PeerConnectionFactory, rootEglBase: EglBase) {
        synchronized(lock) {
            if (null != this.peerConnectionFactory) return
            this.peerConnectionFactory = peerConnectionFactory
            this.rootEglBase = rootEglBase

        }

        val videoSource: VideoSource = this.peerConnectionFactory!!.createVideoSource(false)
        val surfaceTextureHelper = SurfaceTextureHelper.create(
            Thread.currentThread().name, this.rootEglBase!!.eglBaseContext
        )
        val videoCapturer: VideoCapturer? = createCameraCapturer(Camera1Enumerator(false))

        var localVideoTrack: VideoTrack? = null
        videoCapturer?.let {
            videoCapturer.initialize(
                surfaceTextureHelper, context, videoSource.getCapturerObserver()
            )
            videoCapturer.startCapture(1000, 1000, 30)
            localVideoTrack = peerConnectionFactory.createVideoTrack("102", videoSource)
            localVideoTrack?.setEnabled(true)


        }

        signal = SignalSocketIo().apply {
            peerConnectionClient = PeerConnectionClient(
                onGetRemoteStream = {
                    viewModelScope.launch {
                        _remoteStream.value = it
                    }
                },
                localVideoTrack,
                signal = this,
                peerConnectionFactory,
                roomName,
                userName
            )
            this.initConnection()
        }
    }

//    fun init(peerConnectionFactory: PeerConnectionFactory, rootEglBase: EglBase) {
//        synchronized(lock) {
//            if (null != this.peerConnectionFactory) return
//            this.peerConnectionFactory = peerConnectionFactory
//            this.rootEglBase = rootEglBase
//
//        }
//
//        Log.d("WEBRTC_VIEWMODEL", "init with peerconnection")
//        val videoSource: VideoSource = this.peerConnectionFactory!!.createVideoSource(false)
//        val surfaceTextureHelper = SurfaceTextureHelper.create(
//            Thread.currentThread().name, this.rootEglBase!!.eglBaseContext
//        )
//        val videoCapturer: VideoCapturer? = createCameraCapturer(Camera1Enumerator(false))
//
//        var localVideoTrack: VideoTrack? = null
//        videoCapturer?.let {
//            videoCapturer.initialize(
//                surfaceTextureHelper, context, videoSource.getCapturerObserver()
//            )
//            videoCapturer.startCapture(1000, 1000, 30)
//            localVideoTrack = peerConnectionFactory.createVideoTrack("102", videoSource)
//            localVideoTrack?.setEnabled(true)
////            peerConnectionClient.addVideoTrack(localVideoTrack)
//
//        }
//
//        signal = SignalSocketIo().apply {
//            peerConnectionClient = PeerConnectionClient(
//                _remoteStream, localVideoTrack, viewModelScope, signal = this, peerConnectionFactory
//            )
//            this.initConnection()
//        }
//    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.getDeviceNames();

        // Trying to find a front facing camera!
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // We were not able to find a front cam. Look for other cameras
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    fun handleAction(userAction: UserAction) {
        when (userAction) {
            is UserAction.leftJoystickMove -> {

                val payload = RoboMoveCommandPayload(
                    "LEFT", userAction.x, userAction.y, userAction.r

                )
                val jsonPayload = moshi.adapter(RoboMoveCommandPayload::class.java).toJson(payload)
                peerConnectionClient?.sendPayload(jsonPayload)
            }

            is UserAction.rightJoystickMove -> {

                val payload =
                    RoboHeadTurnCommandPayload(phi = userAction.phi, theta = userAction.theta)

                val jsonPayload =
                    moshi.adapter(RoboHeadTurnCommandPayload::class.java).toJson(payload)
                Log.d("JOYSTICK_3D", "${jsonPayload}")
                peerConnectionClient?.sendPayload(jsonPayload)
            }

            UserAction.onStopControl -> {
                peerConnectionClient?.cleanupResource()
            }
        }
    }


}