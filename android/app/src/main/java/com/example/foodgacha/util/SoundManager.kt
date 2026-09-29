package com.example.foodgacha.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.example.foodgacha.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(audioAttributes)
        .build()

    private val soundMap = ConcurrentHashMap<Int, Int>()
    private val loadedSounds = ConcurrentHashMap.newKeySet<Int>()

    var isMuted: Boolean = false

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            }
        }
        preloadSounds()
    }

    private fun preloadSounds() {
        loadSound(R.raw.csgo_ui_crate_open)
        loadSound(R.raw.csgo_ui_crate_item_scroll)
        loadSound(R.raw.item_reveal3_rare)
        loadSound(R.raw.item_reveal4_mythical)
        loadSound(R.raw.item_reveal5_legendary)
        loadSound(R.raw.item_reveal6_ancient)
    }

    private fun loadSound(@RawRes resId: Int): Int {
        return soundMap.computeIfAbsent(resId) {
            soundPool.load(context, resId, 1)
        }
    }

    fun playSound(@RawRes resId: Int, volume: Float = 0.8f) {
        if (isMuted) return
        val soundId = soundMap[resId] ?: loadSound(resId)
        if (loadedSounds.contains(soundId)) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun playCrateOpen() {
        playSound(R.raw.csgo_ui_crate_open, volume = 0.9f)
    }

    fun playItemScroll() {
        playSound(R.raw.csgo_ui_crate_item_scroll, volume = 0.6f)
    }

    fun playReveal(rarity: Int = 0) {
        val soundRes = when (rarity) {
            0 -> R.raw.item_reveal3_rare
            1 -> R.raw.item_reveal4_mythical
            2 -> R.raw.item_reveal5_legendary
            else -> R.raw.item_reveal6_ancient
        }
        playSound(soundRes, volume = 0.9f)
    }

    fun release() {
        soundPool.release()
    }
}
