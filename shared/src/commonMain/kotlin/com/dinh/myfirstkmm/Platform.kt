package com.dinh.myfirstkmm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform