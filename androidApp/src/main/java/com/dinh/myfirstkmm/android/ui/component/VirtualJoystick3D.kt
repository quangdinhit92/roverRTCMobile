package com.compamy.robortc.util

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VirtualJoystick3D(
    baseRadius: Dp = 50.dp,
    knobRadius: Dp = 30.dp,
    onJoystickMoved: (Int, Int) -> Unit,
    isAllowRelease: Boolean = true
) {
    var knobPosition by remember { mutableStateOf(Offset.Zero) }
    val baseRadiusPx = with(LocalDensity.current) { 1.5f * baseRadius.toPx() }
    val knobRadiusPx = with(LocalDensity.current) { 0.8f * knobRadius.toPx() }
    val maxDistance = baseRadiusPx
    var phiAngle by remember {
        mutableStateOf(0)
    }
    var thetaAngle by remember {
        mutableStateOf(0)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newPosition = knobPosition + Offset(dragAmount.x, dragAmount.y)
                        val distanceFromCenter = newPosition.getDistance()
                        val angle = atan2(newPosition.y, newPosition.x)
                        val limitedPosition = if (distanceFromCenter > maxDistance) {
                            Offset(
                                x = maxDistance * cos(angle),
                                y = maxDistance * sin(angle)
                            )
                        } else newPosition
                        knobPosition = limitedPosition

                        val standardPoint = Offset(knobPosition.x, -knobPosition.y)
                        val rPrime =
                            standardPoint.getDistance() / maxDistance * if (knobPosition.y < 0) 1 else -1
                        val thetaDegree = ((Math.PI / 2 - asin(rPrime)) * 180 / Math.PI).toInt()
                        val phiDegree = abs(angle * 180 / Math.PI).toInt()

                        Log.d("MOVE-----", "theta = $thetaDegree")
                        Log.d("MOVE-----", "phi = $phiDegree")

                        phiAngle = phiDegree
                        thetaAngle =thetaDegree
                        onJoystickMoved(
                            phiDegree,
                            thetaDegree
                        )
                    },
                    onDragEnd = {
                        if (isAllowRelease) {
                            knobPosition = Offset.Zero
                            // Notify that the joystick is idle (0, 0)
                            onJoystickMoved(
                                0,
                                0
                            )
                        }
                    }
                )
            }
    ) {
        Column(){
            Text(text = "Phi ${phiAngle}")
            Text(text = "Theta $thetaAngle")
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            drawJoystick3D(center, baseRadiusPx, knobRadiusPx, knobPosition)
        }
    }
}

private fun DrawScope.drawJoystick3D(
    center: Offset,
    baseRadius: Float,
    knobRadius: Float,
    knobPosition: Offset
) {
    drawCircle(
        brush = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(
                Color.LightGray, // Light side (bottom-right)
                Color.Gray,      // Middle tone
                Color.DarkGray   // Dark side (top-left)
            ),
            center = center + Offset(baseRadius * 0.4f, baseRadius * 0.4f), // Light source at bottom-right
            radius = baseRadius
        ),
        radius = baseRadius,
        center = center
    )


    // Draw the movable knob with a gradient for 3D effect
    drawCircle(
        color = Color.White, // Sclera (outer part of the eye)
        radius = knobRadius,
        center = center + knobPosition
    )
    drawCircle(
        color = Color(0xFF00008B), // Dark Blue iris (you can replace this hex code with another color)
        radius = knobRadius * 0.7f,  // Iris is smaller than the sclera
        center = center + knobPosition
    )
    drawCircle(
        color = Color.Black,  // Pupil (black center)
        radius = knobRadius * 0.3f,  // Pupil is smaller than the iris
        center = center + knobPosition
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),  // Reflection of light
        radius = knobRadius * 0.1f,  // Size of the highlight
        center = center + knobPosition + Offset(knobRadius * 0.2f, knobRadius * 0.2f)
    )


    // Add a shadow under the knob for depth
//    drawCircle(
//        color = Color.Gray,
//        radius = baseRadius,
//        center = center
//    )
//    // Draw the movable knob
//    drawCircle(
//        color = Color.Blue,
//        radius = knobRadius,
//        center = center + knobPosition
//    )
//
//
    drawLine(
        color = Color.Blue,
        start = center,
        end = Offset(center.x,200f),
        strokeWidth = 12f
    )

    drawLine(
        color = Color.Blue,
        start = center,
        end = center + knobPosition,
        strokeWidth = 12f
    )

    drawLine(
        color = Color.Red,
        start = center,
        end = center + knobPosition,
        strokeWidth = 6f
    )

    drawLine(
        color = Color.Red,
        start = center,
        end = Offset(1500f,center.y),
        strokeWidth = 6f
    )

}

@Preview
@Composable
fun PreviewVirtualJoystick3D() {
    VirtualJoystick3D(onJoystickMoved = { _, _ -> })
}
