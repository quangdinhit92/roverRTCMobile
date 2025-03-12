package com.dinh.myfirstkmm.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
actual class RoboMoveCommandPayload actual constructor(
    @field: Json(name = "joystick") val joystick: String,
    @field: Json(name = "x") val x: Float,
    @field: Json(name = "y") val y: Float,
    @field: Json(name = "rasdius") val r: Float
)

@JsonClass(generateAdapter = true)
actual class RoboHeadTurnCommandPayload actual constructor(
    @field: Json(name = "joystick") val joystick: String,
    @field: Json(name = "p") val phi: Int,
    @field: Json(name = "t") val theta: Int
)