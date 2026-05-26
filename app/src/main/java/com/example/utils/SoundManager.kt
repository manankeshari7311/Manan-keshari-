package com.example.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundManager {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(Dispatchers.Default)
    var isEnabled: Boolean = true

    fun playTone(frequency: Double, durationMs: Int, volume: Float = 0.5f) {
        if (!isEnabled) return
        scope.launch {
            try {
                val numSamples = (durationMs * SAMPLE_RATE / 1000)
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)
                
                for (i in 0 until numSamples) {
                    sample[i] = sin(2.0 * Math.PI * i / (SAMPLE_RATE / frequency))
                }
                
                var idx = 0
                for (doubleVal in sample) {
                    // scale to maximum amplitude
                    val valShort = (doubleVal * 32767).toInt().toShort()
                    // in 16-bit wav, PCM, first byte is the low order, second is the high order
                    generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(generatedSnd.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.setVolume(volume)
                audioTrack.play()
                
                // Keep playing up to duration then cleanup
                kotlinx.coroutines.delay(durationMs + 100L)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Ignore transient exceptions during stop
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playClick() {
        playTone(540.0, 70, 0.4f)
    }

    fun playMatch() {
        playTone(620.0, 120, 0.5f)
        scope.launch {
            kotlinx.coroutines.delay(80L)
            playTone(920.0, 180, 0.5f)
        }
    }

    fun playWrong() {
        playTone(180.0, 180, 0.6f)
    }

    fun playWin() {
        playTone(523.25, 100, 0.5f)
        scope.launch {
            kotlinx.coroutines.delay(80L)
            playTone(659.25, 100, 0.5f)
            kotlinx.coroutines.delay(80L)
            playTone(783.99, 100, 0.5f)
            kotlinx.coroutines.delay(80L)
            playTone(1046.5, 250, 0.5f)
        }
    }

    fun playPowerup() {
        playTone(880.0, 60, 0.4f)
        scope.launch {
            kotlinx.coroutines.delay(50L)
            playTone(1108.0, 60, 0.4f)
            kotlinx.coroutines.delay(50L)
            playTone(1318.0, 60, 0.4f)
            kotlinx.coroutines.delay(50L)
            playTone(1760.0, 120, 0.4f)
        }
    }

    fun playError() {
        playTone(120.0, 200, 0.5f)
    }
}
