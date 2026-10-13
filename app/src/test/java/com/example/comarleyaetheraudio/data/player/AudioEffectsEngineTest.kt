package com.example.comarleyaetheraudio.data.player

import org.junit.Assert.*
import org.junit.Test

class AudioEffectsEngineTest {
    private open class Handle : EffectHandle {
        var failEnable = false
        var controlled = true
        var releases = 0
        var failRelease = false
        private var active = false
        override val hasControl get() = controlled
        override var enabled: Boolean
            get() = active
            set(value) { if (value && failEnable) error("Native enable failure"); active = value }
        override fun release() { releases++; active = false; if (failRelease) error("Native release failure") }
    }
    private class Eq : Handle(), EqualizerEffect {
        override val minLevelMb = -1500
        override val maxLevelMb = 1500
        override val frequenciesHz = listOf(60, 1000, 14000)
        val levels = mutableListOf(500, 500, 500)
        var failWrite = false
        override fun level(band: Int) = levels[band]
        override fun setLevel(band: Int, levelMb: Int) {
            if (failWrite) error("Native write failure")
            levels[band] = levelMb
        }
    }
    private class Strength : Handle(), StrengthEffect {
        private var rounded = 700
        override var strength: Int
            get() = rounded
            set(value) { rounded = value / 100 * 100 }
    }
    private class Factory(val supportEq: Boolean = true, val supportBass: Boolean = true,
        val supportVirtualizer: Boolean = true) : AudioEffectsFactory {
        val eqs = mutableListOf<Eq>()
        val basses = mutableListOf<Strength>()
        val virtualizers = mutableListOf<Strength>()
        override fun equalizer(sessionId: Int): EqualizerEffect {
            check(supportEq); return Eq().also { eqs += it }
        }
        override fun bass(sessionId: Int): StrengthEffect {
            check(supportBass); return Strength().also { basses += it }
        }
        override fun virtualizer(sessionId: Int): StrengthEffect {
            check(supportVirtualizer); return Strength().also { virtualizers += it }
        }
    }

    @Test fun noSessionDoesNotAllocateOrEnableEffects() {
        val factory = Factory()
        val engine = AudioEffectsEngine(factory)
        engine.attach(0); engine.setEnabled(true)
        assertFalse(engine.state.hasEffects); assertFalse(engine.state.enabled)
        assertTrue(factory.eqs.isEmpty())
    }

    @Test fun partialSupportKeepsSupportedEffectUsable() {
        val engine = AudioEffectsEngine(Factory(supportEq = false, supportVirtualizer = false))
        engine.attach(10); engine.setEnabled(true)
        assertTrue(engine.state.enabled); assertTrue(engine.state.bassAvailable)
        assertTrue(engine.state.bands.isEmpty()); assertFalse(engine.state.virtualizerAvailable)
    }

    @Test fun defaultsAreNeutralAndAdjustmentsCannotImplicitlyEnable() {
        val engine = AudioEffectsEngine(Factory())
        engine.attach(10); engine.setBandLevel(0, 1000); engine.setBassStrength(1000)
        assertFalse(engine.state.enabled)
        assertTrue(engine.state.bands.all { it.levelMb == 0 })
        assertEquals(0, engine.state.bassStrength); assertEquals(0, engine.state.virtualizerStrength)
    }

    @Test fun clampsNativeRangesAndPublishesRoundedReadback() {
        val engine = AudioEffectsEngine(Factory())
        engine.attach(10); engine.setEnabled(true)
        engine.setBandLevel(0, Int.MAX_VALUE); engine.setBandLevel(1, Int.MIN_VALUE)
        engine.setBassStrength(2000); engine.setVirtualizerStrength(-1)
        assertEquals(1500, engine.state.bands[0].levelMb)
        assertEquals(-1500, engine.state.bands[1].levelMb)
        assertEquals(1000, engine.state.bassStrength); assertEquals(0, engine.state.virtualizerStrength)
        engine.setBassStrength(123)
        assertEquals(100, engine.state.bassStrength)
        engine.setBandLevel(99, 500)
        assertEquals(3, engine.state.bands.size)
    }

    @Test fun resetDisablesAllAndRestoresNeutralValues() {
        val factory = Factory()
        val engine = AudioEffectsEngine(factory)
        engine.attach(10); engine.setEnabled(true); engine.setBandLevel(0, 800)
        engine.setBassStrength(600); engine.reset()
        assertFalse(engine.state.enabled); assertTrue(engine.state.bands.all { it.levelMb == 0 })
        assertEquals(0, engine.state.bassStrength)
        assertFalse(factory.eqs.single().enabled); assertFalse(factory.basses.single().enabled)
    }

    @Test fun newSessionReleasesOldAndStartsDisabledWithoutLosingOnSameSession() {
        val factory = Factory()
        val engine = AudioEffectsEngine(factory)
        engine.attach(10); engine.setEnabled(true); engine.setBandLevel(0, 500)
        engine.attach(10)
        assertTrue(engine.state.enabled); assertEquals(1, factory.eqs.size)
        engine.attach(11)
        assertEquals(1, factory.eqs.first().releases); assertEquals(11, engine.state.sessionId)
        assertFalse(engine.state.enabled); assertTrue(engine.state.bands.all { it.levelMb == 0 })
    }

    @Test fun partialEnableFailureReleasesAllInsteadOfShowingActive() {
        val factory = Factory()
        val engine = AudioEffectsEngine(factory)
        engine.attach(10); factory.basses.single().failEnable = true; engine.setEnabled(true)
        assertFalse(engine.state.enabled); assertFalse(engine.state.hasEffects)
        assertNotNull(engine.state.errorMessage)
        assertEquals(1, factory.eqs.single().releases); assertEquals(1, factory.virtualizers.single().releases)
    }

    @Test fun writeFailureReleasesEffectsAndRetryStartsNeutral() {
        val factory = Factory()
        val engine = AudioEffectsEngine(factory)
        engine.attach(10); engine.setEnabled(true); factory.eqs.single().failWrite = true
        engine.setBandLevel(0, 100)
        assertNotNull(engine.state.errorMessage); assertFalse(engine.state.enabled)
        engine.attach(10, force = true)
        assertNull(engine.state.errorMessage); assertTrue(engine.state.hasEffects); assertFalse(engine.state.enabled)
    }

    @Test fun lostControlReleasesAndReportsFailure() {
        val factory = Factory()
        val engine = AudioEffectsEngine(factory)
        engine.attach(10); engine.setEnabled(true); factory.eqs.single().controlled = false
        engine.reconcile()
        assertFalse(engine.state.enabled); assertNotNull(engine.state.errorMessage)
        assertEquals(1, factory.eqs.single().releases)
    }

    @Test fun releaseIsIdempotentAndReleasesEveryHandle() {
        val factory = Factory()
        val engine = AudioEffectsEngine(factory)
        engine.attach(10); engine.setEnabled(true); engine.release(); engine.release()
        assertEquals(1, factory.eqs.single().releases); assertEquals(1, factory.basses.single().releases)
        assertEquals(1, factory.virtualizers.single().releases); assertFalse(engine.state.hasEffects)
    }

    @Test fun failedNeutralInitializationReleasesThePartiallyCreatedEffect() {
        val broken = Eq().apply { failWrite = true }
        val factory = object : AudioEffectsFactory {
            override fun equalizer(sessionId: Int) = broken
            override fun bass(sessionId: Int): StrengthEffect = error("Unsupported")
            override fun virtualizer(sessionId: Int): StrengthEffect = error("Unsupported")
        }
        val engine = AudioEffectsEngine(factory)
        engine.attach(10)
        assertEquals(1, broken.releases); assertFalse(engine.state.hasEffects)
        assertFalse(engine.state.enabled)
    }

    @Test fun oneReleaseFailureDoesNotSkipOtherHandles() {
        val factory = Factory()
        val engine = AudioEffectsEngine(factory)
        engine.attach(10); factory.eqs.single().failRelease = true; engine.release()
        assertEquals(1, factory.basses.single().releases)
        assertEquals(1, factory.virtualizers.single().releases); assertFalse(engine.state.hasEffects)
    }
}
