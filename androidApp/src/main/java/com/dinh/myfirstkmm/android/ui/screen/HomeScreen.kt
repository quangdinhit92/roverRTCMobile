package com.dinh.myfirstkmm.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.compamy.robortc.util.VerticalWheelPicker
import com.compamy.robortc.util.VirtualJoystick
import com.compamy.robortc.util.VirtualJoystick3D
import com.dinh.myfirstkmm.android.ui.MainViewModel
import com.dinh.myfirstkmm.android.ui.UserAction
import com.dinh.myfirstkmm.android.ui.component.RoboRemoteVideoScreen
import com.ronin.horizontal_wheel_picker.HorizontalWheelPicker
import org.webrtc.EglBase
import org.webrtc.MediaStream
import org.webrtc.PeerConnectionFactory
import org.webrtc.RendererCommon.RendererEvents

@Composable
fun HomeScreen(
    modifier: Modifier,
    rootEglBase: EglBase,
    peerConnectionFactory: PeerConnectionFactory,
    viewModel: MainViewModel = hiltViewModel(),
) {
    viewModel.setUserAndRoom("123", "webrtcAndroid")
    val remoteStream by viewModel.remoteStream.collectAsState()

    val onLeftJoystickMove: (Float, Float, Float) -> Unit = { x, y, r ->
        viewModel.handleAction(UserAction.leftJoystickMove(x, y, r))
    }
    val onRightJoystickMove: (Int, Int) -> Unit = { phi, theta ->
        viewModel.handleAction(UserAction.rightJoystickMove(phi, theta))
    }

    HomeLandscape(
        modifier,
        rootEglBase,
        peerConnectionFactory,
        onLeftJoystickMove,
        onRightJoystickMove,
        viewModel,
        onInitPere = {
            viewModel.setupPeer(peerConnectionFactory, rootEglBase)
        },
        remoteStream = remoteStream
    )
}


@Composable
fun HomeLandscape(
    modifier: Modifier,
    rootEglBase: EglBase,
    peerConnectionFactory: PeerConnectionFactory,
    onLeftJoystickMove: (Float, Float, Float) -> Unit,
    onRightJoystickMove: (Int, Int) -> Unit,
    viewModel: MainViewModel,
    onInitPere: () -> Unit,
    remoteStream: Array<out MediaStream>,
) {

    var phi by remember {
        mutableStateOf(0)
    }
    var theta by remember {
        mutableStateOf(0)
    }
    Row(
        Modifier
            .fillMaxSize()
        //  .background(Color.Green)
    ) {
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .background(Color.DarkGray)
        ) {
            VirtualJoystick(
                baseRadius = 50.dp, knobRadius = 20.dp, onJoystickMoved = onLeftJoystickMove
            )
        }
        Box(
            modifier = Modifier
                .weight(6f)
                .fillMaxHeight()
                .background(Color.Gray)
        ) {
            RoboRemoteVideoScreen(
                modifier,
                rootEglBase,
                object : RendererEvents {
                    override fun onFirstFrameRendered() {
                    }

                    override fun onFrameResolutionChanged(p0: Int, p1: Int, p2: Int) {
                    }
                },
                onInitPere = onInitPere,
                remoteStream = remoteStream
            )
        }
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .background(Color.DarkGray)
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    HorizontalWheelPicker(
                        modifier
                            .fillMaxSize()
                        // .background(Color.Green)
                        ,
                        totalItems = 180,
                        initialSelectedItem = 90,
                        onItemSelected = { selectedIndex ->
                            phi = selectedIndex
                            onRightJoystickMove(phi, theta)
                        })
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    VerticalWheelPicker(modifier = Modifier
                        // .background(Color.Red)
                        .fillMaxSize(),
                        totalItems = 180,
                        initialSelectedItem = 90,
                        onItemSelected = { selectedIndex ->
                            // Handle item selection
                            theta = selectedIndex
                            theta = 180 - theta
                            // Log.d("JOYSTICK_3D"," | $theta")
                            onRightJoystickMove(phi, theta)
                        })
                }


            }


        }
    }
}

@Composable
fun HomeContent(
    modifier: Modifier,
    rootEglBase: EglBase,
    peerConnectionFactory: PeerConnectionFactory,
    viewModel: MainViewModel,
    onInitPere: () -> Unit,
    remoteStream: Array<MediaStream>,
) {

    HomePortrait(
        modifier,
        rootEglBase,
        peerConnectionFactory,
        onLeftJoystickMove = { _, _, _ -> },
        onRightJoystickMove = { _, _ -> },
        viewModel,
        onInitPere,
        remoteStream = remoteStream
    )
}


@Composable
fun HomePortrait(
    modifier: Modifier,
    rootEglBase: EglBase,
    peerConnectionFactory: PeerConnectionFactory,
    onLeftJoystickMove: (Float, Float, Float) -> Unit,
    onRightJoystickMove: (Int, Int) -> Unit,
    viewModel: MainViewModel,
    onInitPere: () -> Unit,
    remoteStream: Array<MediaStream>,
) {
    Column(
        Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .weight(8f)
                .fillMaxWidth()
                .background(Color.Gray)
        ) {
            RoboRemoteVideoScreen(
                modifier,
                rootEglBase,
                object : RendererEvents {
                    override fun onFirstFrameRendered() {
                    }

                    override fun onFrameResolutionChanged(p0: Int, p1: Int, p2: Int) {
                    }
                },
                onInitPere = onInitPere,
                remoteStream = remoteStream
            )

        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .background(Color.DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {

                VirtualJoystick(
                    baseRadius = 50.dp, knobRadius = 20.dp, onJoystickMoved = onLeftJoystickMove
                )

            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                VirtualJoystick3D(
                    baseRadius = 50.dp,
                    knobRadius = 20.dp,
                    onJoystickMoved = onRightJoystickMove,
                    isAllowRelease = false
                )

            }


        }

    }

}

