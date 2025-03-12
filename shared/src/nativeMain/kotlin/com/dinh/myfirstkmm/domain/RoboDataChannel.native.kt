package com.dinh.myfirstkmm.domain

actual class RoboMoveCommandPayload actual constructor(
    joystick: String,
    x: Float,
    y: Float,
    r: Float
)

actual class RoboHeadTurnCommandPayload actual constructor(
    joystick: String,
    phi: Int,
    theta: Int
)