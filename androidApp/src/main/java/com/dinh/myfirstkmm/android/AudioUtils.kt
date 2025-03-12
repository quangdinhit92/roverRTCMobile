package com.dinh.myfirstkmm.android

import android.content.Context
import android.media.AudioManager

object AudioUtils {
    // Set audio to speaker or earpiece
    fun setAudioToSpeaker(context: Context, enableSpeaker: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (enableSpeaker) {
            audioManager.isSpeakerphoneOn = true
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        } else {
            audioManager.isSpeakerphoneOn = false
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        }
    }
}
