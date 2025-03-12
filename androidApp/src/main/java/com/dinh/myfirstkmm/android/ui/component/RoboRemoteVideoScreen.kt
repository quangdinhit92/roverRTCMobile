package com.dinh.myfirstkmm.android.ui.component

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.compamy.robortc.jetpackrtc.VideoRenderer
import com.compamy.robortc.jetpackrtc.VideoScalingType
import com.dinh.myfirstkmm.android.ui.MainViewModel
import org.webrtc.AudioTrack
import org.webrtc.EglBase
import org.webrtc.MediaStream
import org.webrtc.PeerConnectionFactory
import org.webrtc.RendererCommon.RendererEvents
import org.webrtc.VideoTrack


@Composable
fun RoboRemoteVideoContent() {

}


@Composable
fun RoboRemoteVideoScreen(
    modifier: Modifier,
    eglBase: EglBase,
    rendererEvents: RendererEvents,
    remoteStream: Array<out MediaStream>,
    onInitPere: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onInitPere.invoke()
    }

    val size = remember { mutableStateOf(IntSize(0, 0)) } // To store the size of the view
    var previousOffset = remember { mutableStateOf(Offset(0f, 0f)) }
    var pointerOffset = remember { mutableStateOf(Offset(0f, 0f)) }

    if (remoteStream.size > 0) {
        remoteStream.get(0)?.let {
            var remoteVideoTrack: VideoTrack? = null
            if (!it.videoTracks.isEmpty()) {
                remoteVideoTrack = it.videoTracks.get(0) ?: null
            }
            var remoteAudioTrack: AudioTrack? = null
            if (!it.audioTracks.isEmpty()) {
                remoteAudioTrack = it.audioTracks.get(0) ?: null

            }
            remoteAudioTrack?.let {
                it.setEnabled(true)
            }

            remoteVideoTrack?.let { videoTrack ->
                Box(modifier = modifier
                    .fillMaxSize()
//                    .background(Color.Red)
                    .onGloballyPositioned { coordinates ->
                        // Get the size of the Box (composable)
                        size.value = coordinates.size
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(onDragEnd = {

                        }) { change, dragAmount ->

                            val centerX = size.value.width / 2f
                            val centerY = size.value.height / 2f
                            val newOffset = change.position.copy(
                                x = change.position.x - centerX, y = change.position.y - centerY
                            )
                            pointerOffset.value = newOffset
                            // Get the center of the Box
                            val currentOffset = change.position

                            // Check if we have a previous offset to compare
                            previousOffset.value?.let {
                                // Calculate the angle between the previous and current offset
                                val angle = calculateAngle(it, currentOffset)

                                // Determine the direction based on the angle
                                val direction = when {
                                    angle in -45f..45f -> "Right"   // 0 to 45 degrees, or -45 to 0 (right movement)
                                    angle in 135f..180f || angle in -180f..-135f -> "Left" // 135 to 180 or -180 to -135 (left movement)
                                    angle in 45f..135f -> "Down" // 45 to 135 degrees (down movement)
                                    else -> "Up" // Otherwise (up movement)
                                }
                                Log.d("SOCKETIO_WEBRTC", direction)
                            }

                            // Update the previous offset for the next drag event
                            previousOffset.value = currentOffset

                        }


                    }) {

                    VideoRenderer(
                        modifier = modifier,
                        videoTrack = remoteVideoTrack,
                        videoScalingType = VideoScalingType.SCALE_ASPECT_FIT,
                        eglBaseContext = eglBase.eglBaseContext,
                        rendererEvents = rendererEvents
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Calculate the center of the Box
                        val centerX = size.value.width / 2f
                        val centerY = size.value.height / 2f

                        // Draw a circle at the current drag position, adjusted to the center
                        drawCircle(
                            color = Color.Red, // Circle color
                            radius = 30f, // Circle size
                            center = Offset(
                                centerX + pointerOffset.value.x, centerY + pointerOffset.value.y
                            ) // Draw the circle relative to the center
                        )
                        drawCircle(
                            color = Color.Blue, // Circle color
                            radius = 30f, // Circle size
                            center = Offset(
                                centerX, centerY
                            ) // Draw the circle relative to the center
                        )
                    }
                }
            }
        }
    }

}

fun calculateAngle(start: Offset, end: Offset): Float {
    val deltaX = end.x - start.x
    val deltaY = end.y - start.y
    val angle = Math.toDegrees(Math.atan2(deltaY.toDouble(), deltaX.toDouble()))
    return angle.toFloat()
}
