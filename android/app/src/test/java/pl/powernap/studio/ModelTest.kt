package pl.powernap.studio

import org.junit.Assert.*
import org.junit.Test

class ModelTest {
    @Test fun remindersRespectOptOutQuietHoursSessionAndCooldown() {
        assertTrue(maySuggestBreak(true,Phase.IDLE,true,8,100,100))
        assertFalse(maySuggestBreak(false,Phase.IDLE,false,12,100,0))
        assertFalse(maySuggestBreak(true,Phase.IDLE,true,22,100,0))
        assertFalse(maySuggestBreak(true,Phase.IDLE,true,7,100,0))
        assertFalse(maySuggestBreak(true,Phase.NAPPING,false,12,100,0))
        assertFalse(maySuggestBreak(true,Phase.WAKING,false,12,100,0))
        assertFalse(maySuggestBreak(true,Phase.IDLE,false,12,100,101))
    }
    @Test fun timerUsesDeadlineAndClampsAfterTimeJump() {
        val s=Session(Phase.NAPPING,end=900000)
        assertEquals(900,s.seconds(0,15))
        assertEquals(1,s.seconds(899999,15))
        assertEquals(0,s.seconds(950000,15))
    }
    @Test fun pausePreservesTimeAndResumeCreatesNewDeadline() {
        val s=Session(Phase.NAPPING,end=900000).pause(300000)
        assertEquals(Phase.PAUSED,s.phase)
        assertEquals(600,s.seconds(9000000,15))
        val resumed=s.resume(1000000)
        assertEquals(1600000L,resumed.end)
        assertEquals(Phase.NAPPING,resumed.phase)
        assertEquals(resumed,resumed.resume(1500000))
    }
    @Test fun eventFloodDoesNotCountAsMinutes() {
        val w=ScrollWindow()
        repeat(1000) {assertFalse(w.record(100000L+it,10))}
    }
    @Test fun sustainedScrollTriggersAtThreshold() {
        val w=ScrollWindow()
        repeat(19) {assertFalse(w.record(60000L*(it+1),20))}
        assertTrue(w.record(1200000,20))
        w.clear()
        assertFalse(w.record(1260000,20))
    }
    @Test fun inactivityAndClockReversalResetWindow() {
        val w=ScrollWindow()
        repeat(9) {w.record(60000L*(it+1),10)}
        assertFalse(w.record(1000000,10))
        assertFalse(w.record(1000,10))
    }
    @Test fun everySceneProducesFiniteBoundedDistinctAudio() {
        val fingerprints=mutableSetOf<Long>()
        Scene.entries.forEach {scene ->
            val synth=Synth()
            var energy=0.0
            var fingerprint=0L
            repeat(22050) {
                val v=synth.sample(scene)
                assertTrue(v.isFinite() && v in -1f..1f)
                energy+=v*v
                fingerprint=31*fingerprint+(v*100000).toLong()
            }
            assertTrue("Silent scene $scene",energy>1)
            fingerprints.add(fingerprint)
        }
        assertEquals(Scene.entries.size,fingerprints.size)
    }
    @Test fun heavyAlarmHasMoreEnergyThanGentleChime() {
        fun energy(heavy: Boolean): Double {
            val s=Synth(); var sum=0.0
            repeat(22050*3) {val x=s.sample(Scene.RAIN,true,heavy); sum+=x*x}; return sum
        }
        assertTrue(energy(true)>energy(false)*2)
    }
}
