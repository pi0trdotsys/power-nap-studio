package pl.powernap.studio

import kotlin.math.ceil

enum class Phase { IDLE, NAPPING, PAUSED, WAKING, TESTING }
enum class Scene(val label: Int, val hint: Int) {
    RAIN(R.string.rain, R.string.rain_hint), OCEAN(R.string.ocean, R.string.ocean_hint),
    FOREST(R.string.forest, R.string.forest_hint), MEADOW(R.string.meadow, R.string.meadow_hint),
    PLANE(R.string.plane, R.string.plane_hint), TRAIN(R.string.train, R.string.train_hint),
    THUNDER(R.string.thunder, R.string.thunder_hint), STORM(R.string.storm, R.string.storm_hint),
    FIRE(R.string.fire, R.string.fire_hint), WIND(R.string.wind, R.string.wind_hint),
    WHITE(R.string.white, R.string.white_hint), BROWN(R.string.brown, R.string.brown_hint)
}
data class Session(
    val phase: Phase = Phase.IDLE,
    val end: Long = 0,
    val remaining: Long = 0,
    val snoozed: Boolean = false,
    val token: Long = 0,
) {
    fun seconds(now: Long, minutes: Int): Int = when (phase) {
        Phase.IDLE -> minutes * 60
        Phase.NAPPING -> ceil((end - now).coerceAtLeast(0) / 1000.0).toInt()
        Phase.PAUSED -> ceil(remaining / 1000.0).toInt()
        else -> 0
    }
    fun pause(now: Long) = if (phase == Phase.NAPPING) copy(phase = Phase.PAUSED, remaining = (end - now).coerceAtLeast(0), end = 0) else this
    fun resume(now: Long) = if (phase == Phase.PAUSED) copy(phase = Phase.NAPPING, end = now + remaining) else this
}

/** Counts active scroll minutes, not event volume. No screen contents are retained. */
class ScrollWindow {
    private val minutes = sortedSetOf<Long>()
    private var lastEvent = 0L
    fun clear() { minutes.clear(); lastEvent = 0 }
    fun record(now: Long, threshold: Int): Boolean {
        if (lastEvent != 0L && (now - lastEvent > 5 * 60_000 || now < lastEvent)) clear()
        lastEvent = now
        val minute = now / 60_000
        minutes.add(minute)
        minutes.removeAll { it < minute - 60 }
        return minutes.size >= threshold && minutes.count { it >= minute - 4 } >= 3
    }
}

fun maySuggestBreak(enabled: Boolean, phase: Phase, quiet: Boolean, hour: Int, now: Long, mutedUntil: Long): Boolean =
    enabled && phase == Phase.IDLE && (!quiet || hour in 8..21) && now >= mutedUntil
