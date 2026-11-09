package pl.powernap.studio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.min
import java.util.concurrent.atomic.AtomicBoolean

class AudioEngine {
    private var running = AtomicBoolean(false)
    @Volatile var volume = .5f
    @Volatile var scene = Scene.RAIN
    private var worker: Thread? = null

    fun start(alarm: Boolean, heavy: Boolean, onFailure: () -> Unit) {
        close()
        val active = AtomicBoolean(true)
        running = active
        worker = thread(name = "Drzemka audio") {
            var track: AudioTrack? = null
            try {
                val format = AudioFormat.Builder().setSampleRate(22050).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build()
                track = AudioTrack.Builder().setAudioFormat(format)
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(if (alarm) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setBufferSizeInBytes(maxOf(4096, AudioTrack.getMinBufferSize(22050, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)*2)).build()
                check(track.state == AudioTrack.STATE_INITIALIZED)
                track.play()
                val synths = Scene.entries.associateWith { Synth(42 + it.ordinal) }
                val alarmSynth = Synth()
                val buffer = ShortArray(512)
                var gain = 0f
                var played = 0L
                var oldScene = scene
                var newScene = scene
                var mix = 1f
                while (active.get()) {
                    if (newScene != scene && mix >= 1f) { oldScene = newScene; newScene = scene; mix = 0f }
                    for (i in buffer.indices) {
                        val fade = min(1f, played++ / (22050f * if (alarm) { if (heavy) 2f else 10f } else 8f))
                        val target = volume * fade
                        gain += (target - gain).coerceIn(-1f/22050,1f/22050)
                        mix = min(1f, mix + 1f/44100)
                        val sample = if (alarm) alarmSynth.sample(newScene, true, heavy) else {
                            val next = synths.getValue(newScene).sample(newScene)
                            if (mix < 1f) synths.getValue(oldScene).sample(oldScene)*(1-mix)+next*mix else next
                        }
                        buffer[i] = (sample * gain * 30000).toInt().coerceIn(-32767,32767).toShort()
                    }
                    if (track.write(buffer,0,buffer.size) < 0) error("Audio write failed")
                }
                // Short click-free shutdown, including explicit alarm dismissal.
                for (i in buffer.indices) buffer[i] = (buffer[i] * (1f-i.toFloat()/buffer.size)).toInt().toShort()
                track.write(buffer,0,buffer.size)
            } catch (_: Exception) { if (active.get()) onFailure() }
            finally { runCatching { track?.stop() }; track?.release() }
        }
    }
    fun close() { running.set(false); worker?.join(1500); worker = null }
}
