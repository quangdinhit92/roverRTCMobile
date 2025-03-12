package com.compamy.robortc.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    baseRadius: Dp = 50.dp,
    knobRadius: Dp = 30.dp,
    onJoystickMoved: (Float, Float, Float) -> Unit,
    isAllowRelease: Boolean = true
) {


    var knobPosition by remember { mutableStateOf(Offset.Zero) }
    val baseRadiusPx = with(LocalDensity.current) { 1.5f * baseRadius.toPx() }
    val knobRadiusPx = with(LocalDensity.current) { 1.5f * knobRadius.toPx() }
    val maxDistance = baseRadiusPx - knobRadiusPx

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {

                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newPosition = knobPosition + Offset(dragAmount.x, dragAmount.y)
                        val distanceFromCenter = newPosition.getDistance()
                        val limitedPosition = if (distanceFromCenter > maxDistance) {
                            val angle = atan2(newPosition.y, newPosition.x)
                            Offset(
                                x = maxDistance * cos(angle),
                                y = maxDistance * sin(angle)
                            )
                        } else newPosition

                        knobPosition = limitedPosition
                        // Send normalized coordinates (-1 to 1) for X and Y
                        onJoystickMoved(
                            (100 * limitedPosition.x / maxDistance).roundToInt() / 100f,
                            (100 * limitedPosition.y / maxDistance).roundToInt() / 100f,
                            1f
                        )
                    },
                    onDragEnd = {
                        if (isAllowRelease) {
                            knobPosition = Offset.Zero
                            // Notify that the joystick is idle (0, 0)
                            onJoystickMoved(0f, 0f, 1f)
                        }

                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            drawJoystick(center, baseRadiusPx, knobRadiusPx, knobPosition)
        }
    }

}

private fun Offset.getDistance(): Float = sqrt(x * x + y * y)

private fun DrawScope.drawJoystick(
    center: Offset,
    baseRadius: Float,
    knobRadius: Float,
    knobPosition: Offset
) {
    // Draw the base circle
    drawCircle(
        color = Color.Gray,
        radius = baseRadius,
        center = center
    )
    // Draw the movable knob
    drawCircle(
        color = Color.Blue,
        radius = knobRadius,
        center = center + knobPosition
    )
}


@Preview
@Composable
fun prevewiVirtualJoystick() {
    VirtualJoystick(onJoystickMoved = { _, _, _ ->

    })
}