package com.presidentsimulator.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

/**
 * Thread-safe game audio engine.
 *
 * - BGM: dual [MediaPlayer] pool with fade crossfades
 * - SFX: [SoundPool] (max 8 streams), preloaded when `R.raw.*` assets exist
 *
 * Missing raw resources are skipped safely (no crash during asset staging).
 */
class GameAudioManager private constructor(
    appContext: Context,
) {
    private val appContext = appContext.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lock = Any()

    private var soundPool: SoundPool? = null
    private val sfxIds = mutableMapOf<SfxType, Int>()
    private val loadedSfx = mutableSetOf<SfxType>()

    private var playerA: MediaPlayer? = null
    private var playerB: MediaPlayer? = null
    private var activePlayerIsA = true
    private var currentTrack = AtomicReference(BgmTrack.NONE)
    private var fadeRunnable: Runnable? = null

    @Volatile
    var musicVolume: Float = 0.55f
        set(value) {
            field = value.coerceIn(0f, 1f)
            applyMusicVolumeToActivePlayer()
            log("Music volume -> ${"%.2f".format(field)}")
        }

    @Volatile
    var sfxVolume: Float = 0.80f
        set(value) {
            field = value.coerceIn(0f, 1f)
            log("SFX volume -> ${"%.2f".format(field)}")
        }

    @Volatile
    var musicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                pauseBgm()
            } else {
                resumeBgm()
            }
            log("Music enabled -> $field")
        }

    @Volatile
    var sfxEnabled: Boolean = true
        set(value) {
            field = value
            log("SFX enabled -> $field")
        }

    private val diagnosticLog = ConcurrentLinkedQueue<String>()
    private val released = AtomicBoolean(false)

    val diagnostics: List<String>
        get() = diagnosticLog.toList()

    val loadedSfxHandles: Map<SfxType, Int>
        get() = synchronized(lock) { sfxIds.toMap() }

    val currentBgmTrack: BgmTrack
        get() = currentTrack.get()

    init {
        initializeSoundPool()
        preloadSfx()
        log("GameAudioManager initialized")
    }

    // ── SFX ──────────────────────────────────────────────────────────────────

    private fun initializeSoundPool() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_SFX_STREAMS)
            .setAudioAttributes(attributes)
            .build()
            .also { pool ->
                pool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        synchronized(lock) {
                            sfxIds.entries.find { it.value == sampleId }?.key?.let { type ->
                                loadedSfx.add(type)
                                log("SFX loaded: $type (id=$sampleId)")
                            }
                        }
                    } else {
                        log("SFX load failed for sampleId=$sampleId status=$status")
                    }
                }
            }
    }

    private fun preloadSfx() {
        val pool = soundPool ?: return
        SfxType.entries.forEach { type ->
            val resId = resolveRawId(type.rawName)
            if (resId == 0) {
                log("SFX missing resource: ${type.rawName} (skipped)")
                return@forEach
            }
            val soundId = pool.load(appContext, resId, 1)
            if (soundId != 0) {
                synchronized(lock) {
                    sfxIds[type] = soundId
                }
                log("SFX queued: ${type.name} -> handle $soundId")
            } else {
                log("SFX load returned 0 for ${type.rawName}")
            }
        }
    }

    fun playSfx(type: SfxType) {
        if (released.get() || !sfxEnabled) return
        val pool = soundPool ?: return
        val soundId = synchronized(lock) {
            if (type !in loadedSfx) return
            sfxIds[type]
        } ?: return
        val volume = sfxVolume
        pool.play(soundId, volume, volume, 1, 0, 1f)
    }

    // ── BGM ──────────────────────────────────────────────────────────────────

    /**
     * Selects BGM from geopolitical pressure:
     * war > high coup risk > peacetime.
     */
    fun updateBgmForGameState(isActiveWar: Boolean, coupRisk: Int) {
        if (released.get() || !musicEnabled) return
        val desired = when {
            isActiveWar -> BgmTrack.WAR
            coupRisk > 50 -> BgmTrack.CRISIS
            else -> BgmTrack.PEACE
        }
        if (desired == currentTrack.get()) return
        crossfadeTo(desired)
    }

    private fun crossfadeTo(track: BgmTrack) {
        synchronized(lock) {
            if (released.get()) return
            val resId = resolveRawId(track.rawName)
            if (resId == 0) {
                // Mark track as active so we do not spam retries while assets are staged.
                currentTrack.set(track)
                log("BGM missing resource: ${track.rawName} (skipped)")
                return
            }

            val incoming = try {
                MediaPlayer.create(appContext, resId)
            } catch (error: Exception) {
                currentTrack.set(track)
                log("BGM create failed for ${track.rawName}: ${error.message}")
                null
            } ?: return

            incoming.isLooping = true
            incoming.setVolume(0f, 0f)
            try {
                incoming.start()
            } catch (error: Exception) {
                log("BGM start failed: ${error.message}")
                incoming.release()
                return
            }

            val outgoing = if (activePlayerIsA) playerA else playerB
            if (activePlayerIsA) {
                playerB = incoming
            } else {
                playerA = incoming
            }
            activePlayerIsA = !activePlayerIsA
            currentTrack.set(track)
            log("BGM crossfade -> ${track.name}")

            startCrossfade(outgoing, incoming)
        }
    }

    private fun startCrossfade(outgoing: MediaPlayer?, incoming: MediaPlayer) {
        fadeRunnable?.let { mainHandler.removeCallbacks(it) }
        val steps = FADE_STEPS
        val stepMs = FADE_DURATION_MS / steps
        var step = 0
        val targetVolume = musicVolume

        val runnable = object : Runnable {
            override fun run() {
                if (released.get()) return
                step++
                val t = step.toFloat() / steps.toFloat()
                val inVol = targetVolume * t
                val outVol = targetVolume * (1f - t)
                try {
                    incoming.setVolume(inVol, inVol)
                    outgoing?.setVolume(max(0f, outVol), max(0f, outVol))
                } catch (_: Exception) {
                    // Player may already be released.
                }
                if (step < steps) {
                    mainHandler.postDelayed(this, stepMs)
                } else {
                    releasePlayer(outgoing)
                    if (outgoing === playerA) playerA = null
                    if (outgoing === playerB) playerB = null
                    applyMusicVolumeToActivePlayer()
                }
            }
        }
        fadeRunnable = runnable
        mainHandler.post(runnable)
    }

    private fun applyMusicVolumeToActivePlayer() {
        val active = if (activePlayerIsA) playerA else playerB
        val volume = if (musicEnabled) musicVolume else 0f
        try {
            active?.setVolume(volume, volume)
        } catch (_: Exception) {
            // Ignore if player is in an invalid state.
        }
    }

    private fun pauseBgm() {
        synchronized(lock) {
            try {
                playerA?.takeIf { it.isPlaying }?.pause()
                playerB?.takeIf { it.isPlaying }?.pause()
            } catch (_: Exception) {
            }
        }
    }

    private fun resumeBgm() {
        synchronized(lock) {
            val active = if (activePlayerIsA) playerA else playerB
            try {
                if (active != null && !active.isPlaying) {
                    active.start()
                    applyMusicVolumeToActivePlayer()
                } else if (active == null) {
                    // Kick peacetime track if nothing is loaded yet.
                    currentTrack.set(BgmTrack.NONE)
                    updateBgmForGameState(isActiveWar = false, coupRisk = 0)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun stopBgm() {
        synchronized(lock) {
            fadeRunnable?.let { mainHandler.removeCallbacks(it) }
            fadeRunnable = null
            releasePlayer(playerA)
            releasePlayer(playerB)
            playerA = null
            playerB = null
            currentTrack.set(BgmTrack.NONE)
            log("BGM stopped")
        }
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    fun release() {
        if (!released.compareAndSet(false, true)) return
        synchronized(lock) {
            fadeRunnable?.let { mainHandler.removeCallbacks(it) }
            fadeRunnable = null
            releasePlayer(playerA)
            releasePlayer(playerB)
            playerA = null
            playerB = null
            soundPool?.release()
            soundPool = null
            sfxIds.clear()
            loadedSfx.clear()
            log("GameAudioManager released")
        }
    }

    private fun releasePlayer(player: MediaPlayer?) {
        if (player == null) return
        try {
            if (player.isPlaying) player.stop()
        } catch (_: Exception) {
        }
        try {
            player.reset()
        } catch (_: Exception) {
        }
        try {
            player.release()
        } catch (_: Exception) {
        }
    }

    private fun resolveRawId(rawName: String): Int {
        return appContext.resources.getIdentifier(rawName, "raw", appContext.packageName)
    }

    private fun log(message: String) {
        val line = "[${System.currentTimeMillis() % 100_000}] $message"
        diagnosticLog.add(line)
        while (diagnosticLog.size > MAX_DIAGNOSTIC_LINES) {
            diagnosticLog.poll()
        }
        Log.d(TAG, message)
    }

    companion object {
        private const val TAG = "GameAudioManager"
        private const val MAX_SFX_STREAMS = 8
        private const val FADE_DURATION_MS = 900L
        private const val FADE_STEPS = 12
        private const val MAX_DIAGNOSTIC_LINES = 40

        @Volatile
        private var instance: GameAudioManager? = null

        fun getInstance(context: Context): GameAudioManager {
            return instance ?: synchronized(this) {
                instance ?: GameAudioManager(context.applicationContext).also { instance = it }
            }
        }

        fun releaseInstance() {
            synchronized(this) {
                instance?.release()
                instance = null
            }
        }
    }
}

enum class SfxType(val rawName: String) {
    CLICK("sfx_click"),
    BUILD_SUCCESS("sfx_build_success"),
    CRISIS_ALERT("sfx_crisis_alert"),
    WAR_DECLARED("sfx_war_declared"),
    COUP_GAME_OVER("sfx_coup_game_over"),
}

enum class BgmTrack(val rawName: String) {
    NONE(""),
    PEACE("bgm_peace"),
    WAR("bgm_war"),
    CRISIS("bgm_crisis"),
}
