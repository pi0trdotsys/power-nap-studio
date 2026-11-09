package pl.powernap.studio

import kotlin.math.*
import kotlin.random.Random

/** Original procedural audio. No samples, network or third-party recordings. */
class Synth(seed: Int = 42) {
    private val random = Random(seed)
    private var low = 0.0
    private var deep = 0.0
    private var tick = 0L
    fun sample(scene: Scene, alarm: Boolean = false, heavy: Boolean = false): Float {
        val t = tick++ / 22050.0
        val noise = random.nextDouble(-1.0, 1.0)
        low = .97 * low + .03 * noise
        deep = .998 * deep + .002 * noise
        val swell = .5 + .5 * sin(2 * PI * t / 9)
        val value = if (alarm) {
            if (heavy) {
                val gate = if (t % 1.0 < .7) .85 else .15
                gate * (sin(2*PI*880*t) * .55 + sin(2*PI*1174.66*t) * .35)
            } else {
                val env = exp(-2.0 * (t % 3))
                env * (.4*sin(2*PI*523.25*t) + .2*sin(2*PI*784.0*t))
            }
        } else when(scene) {
            Scene.WHITE -> noise * .24
            Scene.BROWN -> deep * 5
            Scene.RAIN -> noise * .18 + low * .9
            Scene.WIND -> low * (1.0 + swell) + deep * 1.2
            Scene.OCEAN -> low * (1 + 2*swell) + noise * .07*swell
            Scene.STORM -> low * (1.8 + 2*swell) + deep*2 + noise*.05
            Scene.PLANE -> low * 1.6 + deep*2 + .035*sin(2*PI*83*t) + .02*sin(2*PI*126*t)
            Scene.TRAIN -> low*1.5 + .06*sin(2*PI*58*t) + noise*.25*exp(-25*(t%.68))
            Scene.FIRE -> low + noise*.5*exp(-70*(t%.731)) + noise*.25*exp(-90*(t%1.173))
            Scene.THUNDER -> noise*.12 + low + deep*6*max(0.0,sin(2*PI*t/19)).pow(6)
            Scene.FOREST -> low*.9 + .055*sin(2*PI*(1600*t + 15*sin(2*PI*t*2)))*max(0.0,sin(2*PI*t/7)).pow(14)
            Scene.MEADOW -> low*.55 + .035*sin(2*PI*3200*t)*max(0.0,sin(2*PI*7*t))*max(0.0,sin(2*PI*t/4))
        }
        return tanh(value).toFloat()
    }
}
