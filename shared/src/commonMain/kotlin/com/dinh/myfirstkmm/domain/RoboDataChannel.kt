package com.dinh.myfirstkmm.domain


expect class RoboMoveCommandPayload(
    joystick: String, x: Float, y: Float, r: Float
)

expect class RoboHeadTurnCommandPayload(
    joystick: String = "RIGHT", phi: Int, theta: Int
)

